package dev.mtib.aoc24

import io.github.oshai.kotlinlogging.KotlinLogging
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
import java.time.ZoneId
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds

object Results {
    private val logger = KotlinLogging.logger {}
    private val path = Path("src/main/resources/results.json")
    private val startTime = System.currentTimeMillis()
    private val runDataChannel = Channel<RunData>(Channel.UNLIMITED)

    @Serializable
    class Result(
        val day: Int,
        val part: Int,
        val benchmarkMicros: Long? = null,
        val benchmarkIterations: Long? = null,
        val result: String? = null,
        val cookie: String? = null,
        val timestamp: Long,
        val verified: Boolean = false,
    )

    fun Result.toInstant() = java.time.Instant.ofEpochMilli(timestamp)!!
    private fun get(): List<Result> {
        return try {
            path.readText().let { json ->
                Json.decodeFromString<List<Result>>(json)
            }
        } catch (e: NoSuchFileException) {
            emptyList()
        } catch (e: Exception) {
            logger.error(e) { "Failed to read results" }
            emptyList()
        }
    }

    fun getProgress(day: Int, part: Int): Iterable<Result> {
        return get().filter { it.day == day && it.part == part }
    }

    private val json = Json { prettyPrint = true }
    fun save(result: Result) {
        val current = get()

        path.writeText(json.encodeToString(current + result))
    }

    /**
     * Saves version of the results that doesn't contain any sensitive information.
     */
    fun saveCleaned() {
        buildJsonObject {
            put("timestamp_epoch_millis", startTime)
            put(
                "timestamp_cet",
                java.time.Instant.ofEpochMilli(startTime).atZone(ZoneId.of("Europe/Copenhagen")).toString()
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
            put("days", buildJsonObject {
                get().groupBy { it.day }.mapValues { (_, parts) ->
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
                                                    put("text", result.benchmarkMicros?.microseconds?.toString())
                                                    put("micros", result.benchmarkMicros)
                                                    put(
                                                        "millis",
                                                        result.benchmarkMicros?.toBigDecimal()?.movePointLeft(3)
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
        }.let { data ->
            path.resolveSibling("results_cleaned.json").writeText(json.encodeToString(data))
        }
    }

    fun findVerifiedOrNull(day: Int, part: Int): Result? {
        return get().find { it.day == day && it.part == part && it.verified }
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
    }

    sealed class RunData(
        private val day: Int,
        private val part: Int,
    ) {
        fun toIdentifiedPart() = IdentifiedPart(day, part)
    }

    class RunResult(val result: String, day: Int, part: Int) : RunData(day, part)
    class BenchmarkResult(val duration: Duration, val iterations: Long, day: Int, part: Int) : RunData(day, part)
    data class IdentifiedPart(val day: Int, val part: Int)
}
