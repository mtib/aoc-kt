package dev.mtib.aoc24

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration

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
        val result: String? = null,
        val cookie: String? = null,
        val timestamp: Long,
        val verified: Boolean = false,
    )

    fun save(result: Result) {
        val current = run {
            if (!path.exists()) {
                emptyList()
            } else {
                try {
                    path.readText().let { json ->
                        Json.decodeFromString<List<Result>>(json)
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to read results" }
                    emptyList()
                }
            }
        }

        path.writeText(Json { prettyPrint = true }.encodeToString(current + result))
    }

    fun findVerifiedOrNull(day: Int, part: Int): Result? {
        return try {
            path.readText().let { json ->
                Json.decodeFromString<List<Result>>(json)
            }.find { it.day == day && it.part == part && it.verified }
        } catch (e: Exception) {
            logger.error(e) { "Failed to read results" }
            null
        }
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
                    result = runResult?.result,
                    cookie = System.getenv("SESSION"),
                    timestamp = startTime,
                    verified = false,
                )
            )
        }
    }

    sealed class RunData(
        val day: Int,
        val part: Int,
    ) {
        fun toIdentifiedPart() = IdentifiedPart(day, part)
    }

    class RunResult(val result: String, day: Int, part: Int) : RunData(day, part)
    class BenchmarkResult(val duration: Duration, day: Int, part: Int) : RunData(day, day)

    data class IdentifiedPart(val day: Int, val part: Int)
}