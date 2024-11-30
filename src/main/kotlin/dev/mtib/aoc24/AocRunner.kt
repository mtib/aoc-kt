package dev.mtib.aoc24

import arrow.core.getOrElse
import dev.mtib.aoc24.days.AocDay
import dev.mtib.aoc24.days.IAocDay
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

val logger = KotlinLogging.logger { }
val startTime = System.currentTimeMillis()
val runDataChannel = Channel<RunData>(Channel.UNLIMITED)

sealed class RunData(
    val day: Int,
    val part: Int,
) {
    fun toIdentifiedPart() = IdentifiedPart(day, part)
}

class RunResult(val result: String, day: Int, part: Int) : RunData(day, part)
class BenchmarkResult(val duration: Duration, day: Int, part: Int) : RunData(day, day)

data class IdentifiedPart(val day: Int, val part: Int)

suspend fun main(args: Array<String>) {
    logger.info { "Starting AoC 2024" }
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
        logger.error { "No day provided" }
        return
    } else {
        logger.info { "Running days: ${days.toList().sorted().joinToString(", ")}" }
    }

    days.forEach { day ->
        runDay(day)
    }

    runDataChannel.close()
    runDataChannel.toList().groupBy { it.toIdentifiedPart() }.entries.forEach { (id, data) ->
        val runResult = data.filterIsInstance<RunResult>().firstOrNull()
        val benchmarkResult = data.filterIsInstance<BenchmarkResult>().firstOrNull()

        Results.save(
            Results.Result(
                day = id.day,
                part = id.part,
                benchmarkMicros = benchmarkResult?.duration?.inWholeMicroseconds,
                result = runResult?.result,
                cookie = System.getenv("SESSION"),
                timestamp = startTime,
                verified = false,
            )
        )
    }
}

suspend fun runDay(day: Int) {
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
        val knownResult = Results.findVerifiedOrNull(day, part)
        if (knownResult != null) {
            if (knownResult.result != result.value) {
                logger.warn { "Day $day part $part: \"${result.value}\" does not match known result \"${knownResult.result}\" in ${result.duration}" }
            } else {
                logger.info { "Day $day part $part: \"${result.value}\" matches known result in ${result.duration}" }
            }
        } else {
            logger.info { "Day $day part $part: \"${result.value}\" in ${result.duration}" }
        }
        runDataChannel.send(RunResult(result.value, day, part))
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
        runDataChannel.send(BenchmarkResult(average, day, part))
        BenchmarkWindowPlotter(day, part, windowSize, durations).plot()
        logger.info { "Day $day part $part average of $average (Discord command: `/aoc_benchmark day:$day part:$part time_milliseconds:${average.inWholeMicroseconds / 1000.0}`)" }
    } catch (e: NotImplementedError) {
        logger.debug { "Day $day part $part not implemented" }
    } catch (e: AocDay.DayNotReleasedException) {
        logger.warn { e.message }
    }
}