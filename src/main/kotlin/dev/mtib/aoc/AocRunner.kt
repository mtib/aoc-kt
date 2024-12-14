package dev.mtib.aoc

import arrow.core.Either
import arrow.core.getOrElse
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import dev.mtib.aoc.benchmark.BenchmarkProgressPlotter
import dev.mtib.aoc.benchmark.BenchmarkWindowPlotter
import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.day.PuzzleExecutor
import dev.mtib.aoc.util.*
import dev.mtib.aoc.util.AocLogger.Companion.error
import dev.mtib.aoc.util.Results.BenchmarkResult
import dev.mtib.aoc.util.Results.RunResult
import kotlinx.coroutines.*
import one.profiler.AsyncProfiler
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private val logger = AocLogger.new {}
private val cleanupScope = CoroutineScope(Dispatchers.IO)

const val BENCHMARK_WINDOW = 100
const val BENCHMARK_TIMEOUT_SECONDS = 5
const val BENCHMARK_TIMEOUT_OVERRIDE_ENV = "BENCHMARK_TIMEOUT_SECONDS"

var plotFlag = true
var profileFlag = false

suspend fun main(args: Array<String>) {
    logger.log(AocLogger.Main) { "starting advent of code" }
    AocDay.load()

    val days = buildSet<Day> {
        val years = AocDay.years()
        val mostRecentYear = years.maxByOrNull { it.toInt() } ?: run {
            logger.error { "no years found" }
            return@main
        }
        val yearIdentifiedRegex = Regex("""(\d{4}):(.+)""")
        fun List<AocDay>.days() = map { it.identity }

        args.forEach { arg ->
            val day = arg.toIntOrNull()
            if (day != null) {
                add(mostRecentYear.Day(day))
                return@forEach
            }
            when (arg) {
                "--profile" -> {
                    plotFlag = false
                    profileFlag = true
                }
                "--no-plot" -> plotFlag = false
                "all" -> addAll(AocDay.getAll().days())
                "latest" -> AocDay.getAll(mostRecentYear).days().maxOrNull()?.also { add(it) }
                else -> {
                    if (arg.matches(yearIdentifiedRegex)) {
                        val (_, yearString, selection) = yearIdentifiedRegex.matchEntire(arg)!!.groupValues
                        when (selection) {
                            "all" -> addAll(AocDay.getAll(Year(yearString)).days())
                            "latest" -> AocDay.getAll(Year(yearString)).days().maxOrNull()?.also { add(it) }
                            else -> {
                                val day = selection.toIntOrNull()
                                if (day != null) {
                                    add(Year(yearString).Day(day))
                                } else {
                                    logger.error { "unknown argument: $arg" }
                                    return@main
                                }
                            }
                        }
                    } else {
                        logger.error { "unknown argument: $arg" }
                        return@main
                    }
                }
            }
        }
    }

    val sortedDays = days.toList().sorted()

    if (days.isEmpty()) {
        logger.error { "no day provided" }
        return
    } else {
        logger.log(AocLogger.Main) {
            "running day${if (days.size > 1) "s" else ""}: ${
                sortedDays.joinToString(", ") {
                    "${it.year}:${it.toInt()}"
                }
            }"
        }
    }

    val profiler: AsyncProfiler? = (if(profileFlag) AsyncProfiler.getInstance() else null)?.also {
        it.execute("start,jfr,event=cpu,file=./build/tmp/profile_%p.jfr")
    }

    days.toList().sorted().forEach { day ->
        runDay(day)
    }

    profiler?.execute("stop")

    Results.collect()

    cleanupScope.coroutineContext[Job]?.also {
        try {
            logger.log(AocLogger.Main) { "post-processing" }
            it.start()
            joinAll(*it.children.toList().toTypedArray())
        } catch (e: UnsatisfiedLinkError) {
            AocLogger.Main.error(e = null) { "failed to plot: ${e.message}" }
        }
    }

    logger.log(AocLogger.Main) { "done" }
}

suspend fun runDay(day: Day) {
    val aocDay = AocDay.get(day).getOrElse {
        logger.error(year = day.year, day = day.toInt()) { "not found" }
        return
    }

    if (System.getenv("CI").isNullOrBlank()) {
        try {
            aocDay.createTestFile()
        } catch (e: Exception) {
            logger.error(e = e, year = day.year, day = day.toInt()) { "failed to create test file" }
        }
    }

    mapOf(
        ::runResults to "solving puzzles",
        ::benchmark to "running benchmarks",
    ).forEach { (func, message) ->
        aocDay.benchmarking = func == ::benchmark
        logger.log(day) { message }
        mapOf(
            2 to PuzzleExecutor::part2,
            1 to PuzzleExecutor::part1,
        ).forEach { (part, block) ->
            aocDay.partMode = part
            val puzzle = day.PuzzleIdentity(part)
            val runContext = RunContext(
                puzzle = puzzle,
                block = { block(aocDay) },
                teardown = aocDay::teardown,
                getHint = { result -> aocDay.compareHints(part, result) },
            )
            ciTimeout(puzzle, 10.seconds) {
                withContext(aocDay.pool) {
                    func(runContext)
                }
            }
            aocDay.partMode = null
        }
    }

    if (plotFlag) {
        cleanupScope.launch(start = CoroutineStart.LAZY) {
            BenchmarkProgressPlotter(day).plot()
        }
    }
}

private data class RunContext(
    val puzzle: PuzzleIdentity,
    val block: suspend () -> Any,
    val teardown: suspend () -> Unit,
    val getHint: (String) -> Either<AocDay.Hint.Direction, Unit>,
)

private class IncorrectResultException(val result: String, val knownConflict: AocDay.Hint.Direction) :
    Exception("got $result but earlier hint says this result is $knownConflict")

private suspend fun runResults(
    context: RunContext,
) {
    val (puzzle, block, teardown, getHint) = context
    try {
        val result = measureTimedValue { block().toString() }
        val knownResult = Results.findVerifiedOrNull(puzzle)
        val hint = getHint(result.value)

        hint.onLeft {
            throw IncorrectResultException(result.value, it)
        }
        val styledResult = styleResult(result.value)

        if (knownResult != null) {
            if (knownResult.result != result.value) {
                logger.error(
                    puzzle = puzzle,
                ) { "found conflicting solution $styledResult (expected ${styleResult(knownResult.result ?: "???")}) in ${result.duration}" }
            } else {
                logger.log(
                    puzzle,
                ) { "found solution $styledResult in ${result.duration} " + TextColors.brightGreen("(verified)") }
            }
        } else {
            logger.log(puzzle) { "found solution $styledResult in ${result.duration}" }
        }
        Results.send(RunResult(result = result.value, puzzle = puzzle))
    } catch (e: NotImplementedError) {
        logger.error(e = null, puzzle) { "not implemented" }
    } catch (e: AocDay.DayNotReleasedException) {
        logger.error(e = null, puzzle) { "not released yet, releasing in ${e.waitDuration}" }
    } catch (e: IncorrectResultException) {
        logger.error(puzzle = puzzle) { "got ${styleResult(e.result)} but earlier hint makes this result impossible (${e.knownConflict})" }
    } catch (e: AocDay.CiSkipException) {
        logger.error(e = null, puzzle) { "skipped in CI" }
    } finally {
        teardown()
    }
}

private suspend fun benchmark(
    context: RunContext,
) {
    val (puzzle, block, teardown, getHint) = context
    try {
        val durations = mutableListOf<Duration>()
        val timeout = System.getenv(BENCHMARK_TIMEOUT_OVERRIDE_ENV)?.let { it.toInt().seconds } ?: BENCHMARK_TIMEOUT_SECONDS.seconds
        val startTime = System.currentTimeMillis()
        val benchmarkDuration = measureTime {
            while (System.currentTimeMillis() - startTime < timeout.inWholeMilliseconds && (durations.size < BENCHMARK_WINDOW * 100 || System.currentTimeMillis() - startTime < 1.seconds.inWholeMilliseconds)) {
                measureTime { block() }.also {
                    durations.add(it)
                }
                System.gc()
                teardown()
                yield()
            }
        }
        logger.log(puzzle) { "ran ${durations.size} iterations in $benchmarkDuration" }
        val average = durations
            .takeLast(BENCHMARK_WINDOW)
            .let { tail ->
                tail.sumOf { sample -> sample.inWholeMicroseconds.toBigDecimal() } / tail.size.toBigDecimal()
            }
            .toDouble()
            .microseconds
        val lastBest = Results.getLastSubmittedBenchmarkResult(puzzle)
        Results.send(BenchmarkResult(average, durations.size.toLong(), puzzle))
        if (plotFlag) {
            cleanupScope.launch(start = CoroutineStart.LAZY) {
                BenchmarkWindowPlotter(puzzle, BENCHMARK_WINDOW, durations).plot()
            }
        }
        val styledCommand =
            (TextStyles.italic + TextColors.blue)("/aoc_benchmark day:${puzzle.day} part:${puzzle.part} time_milliseconds:${average.inWholeMicroseconds / 1000.0}")
        val improvementText = lastBest?.let {
            val lastSubmittedDuration = it.timeMs.milliseconds
            if (average < lastSubmittedDuration) {
                val improvement = lastSubmittedDuration - average
                " improved by ${TextColors.brightGreen(improvement.toString())}"
            } else if (average > lastSubmittedDuration) {
                val degradation = average - lastSubmittedDuration
                " degraded by ${TextColors.brightRed(degradation.toString())}"
            } else if (average == lastSubmittedDuration) {
                " ${TextColors.gray("stayed the same")}"
            } else {
                null
            }
        }
        val canReportWithDiscord = puzzle.year == ZonedDateTime.now(ZoneId.of("CET")).year
        logger.log(
            puzzle
        ) {
            buildString {
                append("averaged at ${TextColors.brightWhite(average.toString())}")

                if (improvementText != null) {
                    append(improvementText)
                } else if (canReportWithDiscord) {
                    append(" ${TextColors.brightBlue("new(?)")}")
                }

                if (canReportWithDiscord) {
                    append(", report with: $styledCommand")
                }
            }
        }
    } catch (e: NotImplementedError) {
        logger.error(e = null, puzzle) { "not implemented" }
    } catch (e: AocDay.DayNotReleasedException) {
        logger.error(e = null, puzzle) { "not released yet, releasing in ${e.waitDuration}" }
    } catch (e: AocDay.CiSkipException) {
        logger.error(e = null, puzzle) { "skipped in CI" }
    }
}
