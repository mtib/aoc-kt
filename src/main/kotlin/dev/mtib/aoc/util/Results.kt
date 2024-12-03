package dev.mtib.aoc.util

import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.prompt
import dev.mtib.aoc.util.AocLogger.Companion.resultTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.math.BigDecimal
import java.nio.file.NoSuchFileException
import java.time.Instant
import java.time.ZoneId
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds

object Results {
    private val logger = AocLogger.new { }
    private val path = Path("src/main/resources/results.json")
    private val startTime = System.currentTimeMillis()
    private val runDataChannel = Channel<RunData>(Channel.UNLIMITED)

    @Serializable
    data class Result(
        val year: Int = 2024,
        val day: Int,
        val part: Int,
        val benchmarkMicros: Long? = null,
        val benchmarkIterations: Long? = null,
        val result: String? = null,
        val cookie: String? = null,
        val timestamp: Long,
        val verified: Boolean = false,
    )

    fun Result.identity() = PuzzleIdentity(year, day, part)
    fun Result.toInstant() = Instant.ofEpochMilli(timestamp)!!
    private fun get(year: Int? = null): List<Result> {
        return try {
            path.readText().let { json ->
                Json.decodeFromString<List<Result>>(json)
            }
        } catch (e: NoSuchFileException) {
            emptyList()
        } catch (e: Exception) {
            logger.error(e, year) { "Failed to read results" }
            emptyList()
        }.let {
            if (year != null) {
                it.filter { result -> result.year == year }
            } else {
                it
            }
        }
    }

    fun getProgress(puzzle: PuzzleIdentity): Iterable<Result> {
        return getProgress(puzzle.year, puzzle.day, puzzle.part)
    }

    private fun getProgress(year: Int, day: Int, part: Int): Iterable<Result> {
        return get(year).filter { it.day == day && it.part == part }
    }

    private val json = Json { prettyPrint = true }
    fun save(result: Result) {
        val current = get()

        path.writeText(json.encodeToString((current.filter {
            it.year != result.year || it.day != result.day || it.part != result.part || it.timestamp != result.timestamp || it.cookie != result.cookie
        } + result).sortedWith(compareBy({ it.timestamp }, { it.day }, { it.part }))))
    }

    /**
     * Saves version of the results that doesn't contain any sensitive information.
     */
    fun saveCleaned() {
        buildJsonObject {
            put("timestamp_epoch_millis", startTime)
            put(
                "timestamp_cet",
                Instant.ofEpochMilli(startTime).atZone(ZoneId.of("Europe/Copenhagen")).toString()
            )
            put("memory_mb", Runtime.getRuntime().totalMemory().toBigDecimal().divide(BigDecimal("1024").pow(2)))
            put("cpu_cores", Runtime.getRuntime().availableProcessors())
            put("ref", System.getenv("GITHUB_REF"))
            put("ref_short", System.getenv("GITHUB_REF_NAME"))
            put("run_id", System.getenv("GITHUB_RUN_ID"))
            put("sha", System.getenv("GITHUB_SHA"))
            put("arch", System.getProperty("RUNNER_ARCH"))
            put("os", System.getProperty("RUNNER_OS"))

            put("results", get().size)
            put("years", buildJsonObject {
                get().groupBy { it.year }.mapValues { (year, days) ->
                    put(year.toString(), buildJsonObject {
                        put("days", buildJsonObject {
                            days.groupBy { it.day }.mapValues { (_, parts) ->
                                parts.groupBy { it.part }
                            }.entries.forEach { (day, parts) ->
                                put(day.toString(), buildJsonObject {
                                    put("parts", buildJsonObject {
                                        parts.entries.forEach { (part, results) ->
                                            put(part.toString(), buildJsonArray {
                                                results.forEach { result ->
                                                    add(
                                                        buildJsonObject {
                                                            put("benchmark", buildJsonObject {
                                                                put(
                                                                    "text",
                                                                    result.benchmarkMicros?.microseconds?.toString()
                                                                )
                                                                put("micros", result.benchmarkMicros)
                                                                put(
                                                                    "millis",
                                                                    result.benchmarkMicros?.toBigDecimal()
                                                                        ?.movePointLeft(3)
                                                                        ?.toDouble()
                                                                )
                                                                put("iterations", result.benchmarkIterations)
                                                            })
                                                            put("timestamp", result.timestamp)
                                                        }
                                                    )
                                                }
                                            })
                                        }
                                    })
                                })
                            }
                        })
                    })
                }
            })
        }.let { data ->
            path.resolveSibling("results_cleaned.json").writeText(json.encodeToString(data))
        }
    }

    fun findVerifiedOrNull(puzzle: PuzzleIdentity): Result? {
        return findVerifiedOrNull(puzzle.year, puzzle.day, puzzle.part)
    }

    private fun findVerifiedOrNull(year: Int, day: Int, part: Int): Result? {
        return get(year).find { it.day == day && it.part == part && it.verified }
    }

    suspend fun send(result: RunData) {
        runDataChannel.send(result)
    }

    suspend fun collect() {
        runDataChannel.close()
        runDataChannel.toList().groupBy { it.toIdentifiedPart() }.entries.forEach { (id, data) ->
            val runResult = data.filterIsInstance<RunResult>().firstOrNull()
            val benchmarkResult = data.filterIsInstance<BenchmarkResult>().firstOrNull()

            save(
                Result(
                    year = id.year,
                    day = id.day,
                    part = id.part,
                    benchmarkMicros = benchmarkResult?.duration?.inWholeMicroseconds,
                    benchmarkIterations = benchmarkResult?.iterations,
                    result = runResult?.result,
                    cookie = System.getenv("SESSION"),
                    timestamp = startTime,
                    verified = false,
                )
            )
        }
        saveCleaned()
        logger.log { "saved results" }
    }

    fun verifyLast() {
        val results = get()
        val last =
            results.filter { it.result != null && !results.any { otherResult -> otherResult.verified && otherResult.cookie == it.cookie && otherResult.day == it.day && otherResult.part == it.part && otherResult.year == it.year } }
                .maxByOrNull { it.timestamp }
                ?: return logger.log(AocLogger.Main) { "no results to verify" }
        val timestamp = Instant.ofEpochMilli(last.timestamp)
        val identity = last.identity()
        logger.log(
            identity
        ) { "verifying last result ${resultTheme(last.result!!)} from ${(System.currentTimeMillis() - timestamp.toEpochMilli()).milliseconds} ago" }
        val good = Terminal().prompt(
            AocLogger.formatLineAsLog(last.year, last.day, last.part, "is this correct?"),
            choices = listOf("yes", "no", "y", "n"),
            invalidChoiceMessage = AocLogger.formatLineAsLog(last.year, last.day, last.part, "invalid choice")
        ) {
            when (it) {
                "yes", "y" -> {
                    ConversionResult.Valid("yes")
                }

                "no", "n" -> {
                    ConversionResult.Valid("no")
                }

                else -> ConversionResult.Invalid("invalid choice")
            }
        }.let {
            when (it) {
                "yes" -> true
                "no" -> false
                else -> false
            }
        }
        if (good) {
            save(
                last.copy(
                    verified = true
                )
            )
            logger.log(identity) { "verified last result ${resultTheme(last.result!!)}" }
        } else {
            logger.log(identity) { "not verified" }
        }
    }

    sealed class RunData(
        private val year: Int,
        private val day: Int,
        private val part: Int,
    ) {
        fun toIdentifiedPart() = PuzzleIdentity(year, day, part)
    }

    class RunResult(
        val result: String,
        year: Int,
        day: Int,
        part: Int
    ) : RunData(
        year = year,
        day = day,
        part = part
    ) {
        constructor(result: String, puzzle: PuzzleIdentity) : this(result, puzzle.year, puzzle.day, puzzle.part)
    }

    class BenchmarkResult(
        val duration: Duration,
        val iterations: Long,
        year: Int,
        day: Int,
        part: Int
    ) : RunData(
        year = year,
        day = day,
        part = part
    ) {
        constructor(duration: Duration, iterations: Long, puzzle: PuzzleIdentity) : this(
            duration,
            iterations,
            puzzle.year,
            puzzle.day,
            puzzle.part
        )
    }
}
