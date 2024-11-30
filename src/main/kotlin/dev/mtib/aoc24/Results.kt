package dev.mtib.aoc24

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object Results {
    private val logger = KotlinLogging.logger {}
    private val path = Path("src/main/resources/results.json")

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
}