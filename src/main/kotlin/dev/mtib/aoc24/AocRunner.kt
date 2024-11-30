package dev.mtib.aoc24

import arrow.core.getOrElse
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

val logger = KotlinLogging.logger { }

suspend fun main(args: Array<String>) {
    logger.info { "Starting AoC 2024" }

    AocDay.load()
    val day = args.firstOrNull()?.toIntOrNull()
    if (day == null) {
        logger.error { "No day provided" }
        return
    }

    val aocDay = AocDay.get(day).getOrElse {
        logger.error { "Day $day not found" }
        return
    }

    val pool = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher() + CoroutineName("aoc")

    mapOf(
        ::runResults to "Running for results",
        ::benchmark to "Running for benchmarks",
    ).forEach { (func, message) ->
        logger.info { message }
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
        logger.info { "Day $day part $part: \"${result.value}\" in ${result.duration}" }
    } catch (e: NotImplementedError) {
        logger.warn { "Day $day part $part not implemented" }
    } catch (e: AocDay.DayNotReleasedException) {
        logger.warn { e.message }
    }
}

suspend fun benchmark(
    day: Int,
    part: Int,
    block: suspend () -> String,
) {
    try {
        val durations = mutableListOf<Duration>()
        val timeout = 10.seconds
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
            logger.debug { "Day $day part $part finished ${durations.size} iterations in $timeout" }
        }
        val windowSize = 100
        val average = durations
            .takeLast(windowSize)
            .let { tail ->
                tail.sumOf { sample -> sample.inWholeMicroseconds.toBigDecimal() } / tail.size.toBigDecimal()
            }
            .toDouble()
            .microseconds
        BenchmarkWindowPlotter(day, part, windowSize, durations).plot()
        logger.info { "Day $day part $part average of $average (Discord command: `/aoc_benchmark day:$day part:$part time_milliseconds:${average.inWholeMicroseconds / 1000.0}`)" }
    } catch (e: NotImplementedError) {
        logger.debug { "Day $day part $part not implemented" }
    } catch (e: AocDay.DayNotReleasedException) {
        logger.warn { e.message }
    }
}