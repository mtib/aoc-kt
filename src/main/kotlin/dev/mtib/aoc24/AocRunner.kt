package dev.mtib.aoc24

import arrow.core.getOrElse
import dev.mtib.aoc24.Results.BenchmarkResult
import dev.mtib.aoc24.Results.RunResult
import dev.mtib.aoc24.days.AocDay
import dev.mtib.aoc24.days.IAocDay
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private val mainLogger = KotlinLogging.logger { }

const val BENCHMARK_WINDOW = 100
const val BENCHMARK_TIMEOUT_SECONDS = 10

suspend fun main(args: Array<String>) {
    mainLogger.info { "Starting AoC 2024" }
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
        mainLogger.error { "No day provided" }
        return
    } else {
        mainLogger.info { "Running days: ${days.toList().sorted().joinToString(", ")}" }
    }

    days.forEach { day ->
        runDay(day)
    }

    Results.collect()
}

suspend fun runDay(day: Int) {
    val aocDay = AocDay.get(day).getOrElse {
        mainLogger.error { "Day $day not found" }
        return
    }

    val pool = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher() + CoroutineName("aoc")

    mapOf(
        ::runResults to "Running for day $day results",
        ::benchmark to "Running for day $day benchmarks",
    ).forEach { (func, message) ->
        mainLogger.info { message }
        mapOf(
            1 to IAocDay::part1,
            2 to IAocDay::part2,
        ).forEach { (part, block) ->
            withContext(pool) {
                func(day, part) {
                    block(aocDay)
                }
            }
        }
    }
}

suspend fun runResults(day: Int, part: Int, block: suspend () -> String) {
    try {
        val result = measureTimedValue { block() }
        val knownResult = Results.findVerifiedOrNull(day, part)
        if (knownResult != null) {
            if (knownResult.result != result.value) {
                mainLogger.warn { "Day $day part $part: \"${result.value}\" does not match known result \"${knownResult.result}\" in ${result.duration}" }
            } else {
                mainLogger.info { "Day $day part $part: \"${result.value}\" matches known result in ${result.duration}" }
            }
        } else {
            mainLogger.info { "Day $day part $part: \"${result.value}\" in ${result.duration}" }
        }
        Results.send(RunResult(result.value, day, part))
    } catch (e: NotImplementedError) {
        mainLogger.warn { "Day $day part $part not implemented" }
    } catch (e: AocDay.DayNotReleasedException) {
        mainLogger.warn { e.message }
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
        try {
            withTimeout(timeout) {
                while (true) {
                    measureTime { block() }.also {
                        durations.add(it)
                    }
                    yield()
                }
            }
        } catch (e: TimeoutCancellationException) {
            mainLogger.debug { "Day $day part $part finished ${durations.size} iterations in $timeout" }
        }
        val average = durations
            .takeLast(BENCHMARK_WINDOW)
            .let { tail ->
                tail.sumOf { sample -> sample.inWholeMicroseconds.toBigDecimal() } / tail.size.toBigDecimal()
            }
            .toDouble()
            .microseconds
        Results.send(BenchmarkResult(average, day, part))
        BenchmarkWindowPlotter(day, part, BENCHMARK_WINDOW, durations).plot()
        mainLogger.info { "Day $day part $part average of $average (Discord command: `/aoc_benchmark day:$day part:$part time_milliseconds:${average.inWholeMicroseconds / 1000.0}`)" }
    } catch (e: NotImplementedError) {
        mainLogger.debug { "Day $day part $part not implemented" }
    } catch (e: AocDay.DayNotReleasedException) {
        mainLogger.warn { e.message }
    }
}