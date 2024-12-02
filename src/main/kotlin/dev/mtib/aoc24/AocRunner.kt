package dev.mtib.aoc24

import arrow.core.getOrElse
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import dev.mtib.aoc24.benchmark.BenchmarkProgressPlotter
import dev.mtib.aoc24.benchmark.BenchmarkWindowPlotter
import dev.mtib.aoc24.days.AocDay
import dev.mtib.aoc24.days.PuzzleExecutor
import dev.mtib.aoc24.util.AocLogger
import dev.mtib.aoc24.util.AocLogger.Companion.resultTheme
import dev.mtib.aoc24.util.Results
import dev.mtib.aoc24.util.Results.BenchmarkResult
import dev.mtib.aoc24.util.Results.RunResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private val logger = AocLogger.new {}
private val cleanupScope = CoroutineScope(Dispatchers.IO)

const val BENCHMARK_WINDOW = 100
const val BENCHMARK_TIMEOUT_SECONDS = 10
suspend fun main(args: Array<String>) {
    logger.logSuspend { "starting AoC 2024" }
    AocDay.load()

    val days = buildSet<Int> {
        args.forEach {
            val day = it.toIntOrNull()
            if (day != null) {
                add(day)
            }
            when (it) {
                "all" -> addAll(AocDay.getAll().keys)
                "latest" -> AocDay.getAll().keys.maxOrNull()?.let { add(it) }
            }
        }
    }

    if (days.isEmpty()) {
        logger.error { "no day provided" }
        return
    } else {
        logger.log { "running days: ${days.toList().sorted().joinToString(", ")}" }
    }

    days.forEach { day ->
        runDay(day)
    }

    Results.collect()

    cleanupScope.coroutineContext[Job]?.also {
        logger.log { "post-processing" }
        it.start()
        joinAll(*it.children.toList().toTypedArray())
    }

    logger.log { "done" }
}

suspend fun runDay(day: Int) {
    val aocDay = AocDay.get(day).getOrElse {
        logger.error(day = day) { "not found" }
        return
    }

    mapOf(
        ::runResults to "solving puzzle",
        ::benchmark to "benchmarking",
    ).forEach { (func, message) ->
        aocDay.benchmarking = func == ::benchmark
        logger.log(day) { message }
        mapOf(
            1 to PuzzleExecutor::part1,
            2 to PuzzleExecutor::part2,
        ).forEach { (part, block) ->
            aocDay.partMode = part
            withContext(aocDay.pool) {
                func(day, part) {
                    block(aocDay)
                }
            }
            aocDay.partMode = null
        }
    }

    cleanupScope.launch(start = CoroutineStart.LAZY) {
        BenchmarkProgressPlotter(day).plot()
    }
}

suspend fun runResults(day: Int, part: Int, block: suspend () -> String) {
    try {
        val result = measureTimedValue { block() }
        val knownResult = Results.findVerifiedOrNull(day, part)

        val styledResult =
            resultTheme(if (System.getenv("CI") == null) result.value else "*".repeat(result.value.length))
        if (knownResult != null) {
            if (knownResult.result != result.value) {
                logger.error(
                    e = null,
                    day,
                    part
                ) { "found conflicting solution $styledResult (expected ${resultTheme(knownResult.result ?: "???")}) in ${result.duration}" }
            } else {
                logger.log(
                    day,
                    part
                ) { "found solution $styledResult in ${result.duration} " + TextColors.brightGreen("(verified)") }
            }
        } else {
            logger.log(day, part) { "found solution $styledResult in ${result.duration}" }
        }
        Results.send(RunResult(result.value, day, part))
    } catch (e: NotImplementedError) {
        logger.error(e = null, day, part) { "not implemented" }
    } catch (e: AocDay.DayNotReleasedException) {
        logger.error(e = null, day, part) { "not released yet, releasing in ${e.waitDuration}" }
    }
}

suspend fun benchmark(
    day: Int,
    part: Int,
    block: suspend () -> String,
) {
    try {
        val durations = mutableListOf<Duration>()
        val timeout = BENCHMARK_TIMEOUT_SECONDS.seconds
        val startTime = System.currentTimeMillis()
        val benchmarkDuration = measureTime {
            while (System.currentTimeMillis() - startTime < timeout.inWholeMilliseconds && (durations.size < BENCHMARK_WINDOW * 20 || System.currentTimeMillis() - startTime < 1.seconds.inWholeMilliseconds)) {
                measureTime { block() }.also {
                    durations.add(it)
                }
                yield()
            }
        }
        logger.log(day, part) { "ran ${durations.size} iterations in $benchmarkDuration" }
        val average = durations
            .takeLast(BENCHMARK_WINDOW)
            .let { tail ->
                tail.sumOf { sample -> sample.inWholeMicroseconds.toBigDecimal() } / tail.size.toBigDecimal()
            }
            .toDouble()
            .microseconds
        Results.send(BenchmarkResult(average, durations.size.toLong(), day, part))
        cleanupScope.launch(start = CoroutineStart.LAZY) {
            BenchmarkWindowPlotter(day, part, BENCHMARK_WINDOW, durations).plot()
        }
        val styledCommand =
            (TextStyles.italic + TextColors.blue)("/aoc_benchmark day:$day part:$part time_milliseconds:${average.inWholeMicroseconds / 1000.0}")
        logger.log(
            day,
            part
        ) { "averaged at ${TextColors.brightWhite(average.toString())}, report with: $styledCommand" }
    } catch (e: NotImplementedError) {
        logger.error(e = null, day, part) { "not implemented" }
    } catch (e: AocDay.DayNotReleasedException) {
        logger.error(e = null, day, part) { "not released yet, releasing in ${e.waitDuration}" }
    }
}
