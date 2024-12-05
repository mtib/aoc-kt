package dev.mtib.aoc.util

import com.github.ajalt.mordant.terminal.ConversionResult
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.prompt
import dev.mtib.aoc.util.AocLogger.Companion.resultTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.UnifiedJedis
import redis.clients.jedis.exceptions.JedisConnectionException
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
import redis.clients.jedis.json.Path2 as JsonPath

object Results {
    private val logger = AocLogger.new { }
    private val path = Path("src/main/resources/results.json")
    private val startTime = System.currentTimeMillis()
    private val runDataChannel = Channel<RunData>(Channel.UNLIMITED)

    private const val REDIS_KEY = "aoc-results"
    private enum class StorageLocation {
        FILE,
        REDIS
    }

    private val storageLocation by lazy {
        if (System.getenv("REDIS_URL") != null) {
            StorageLocation.REDIS
        } else {
            StorageLocation.FILE
        }
    }

    private val redisClient by lazy {
        try {
            val redisUrl = System.getenv("REDIS_URL")?: return@lazy null
            JedisPooled(redisUrl)
        } catch(e: Exception) {
            logger.error(e) { "Failed to connect to redis" }
            null
        }
    }

    @Serializable
    data class Result(
        val year: Int,
        val day: Int,
        val part: Int,
        val benchmarkMicros: Long? = null,
        val benchmarkIterations: Long? = null,
        val result: String? = null,
        val cookie: String? = null,
        val timestamp: Long,
        val verified: Boolean = false,
    ) {
        companion object {
            fun from(json: JSONObject): Result = Result(
                year = json.getInt("year"),
                day = json.getInt("day"),
                part = json.getInt("part"),
                benchmarkMicros = json.getOrNull("benchmarkMicros"),
                benchmarkIterations = json.getOrNull("benchmarkIterations"),
                result = json.getOrNull("result"),
                cookie = json.getOrNull("cookie"),
                timestamp = json.getLong("timestamp"),
                verified = json.getOrNull("verified") ?: false
            )

            fun from(map: Map<*, *>): Result = Result(
                year = map["year"] as Int,
                day = map["day"] as Int,
                part = map["part"] as Int,
                benchmarkMicros = (map["benchmarkMicros"] as Int?)?.toLong(),
                benchmarkIterations = (map["benchmarkIterations"] as Int?)?.toLong(),
                result = map["result"] as String?,
                cookie = map["cookie"] as String?,
                timestamp = map["timestamp"] as Long,
                verified = (map["verified"] ?: false) as Boolean
            )
        }

        fun toJsonObject(): JSONObject = JSONObject().apply {
            put("year", year)
            put("day", day)
            put("part", part)
            put("benchmarkMicros", benchmarkMicros)
            put("benchmarkIterations", benchmarkIterations)
            put("result", result)
            put("cookie", cookie)
            put("timestamp", timestamp)
            put("verified", verified)
        }
    }

    private inline fun <reified T> JSONObject.getOrNull(key: String): T? = try {
        when (T::class) {
            String::class -> getString(key) as T
            Int::class -> getInt(key) as T
            Long::class -> getLong(key) as T
            Boolean::class -> getBoolean(key) as T
            else -> throw Exception("unsupported type: ${T::class}")
        }
    } catch (e: JSONException) {
        null
    }

    private fun getWithFilter(filter: String): List<Result> {
        return when (storageLocation) {
            StorageLocation.FILE -> throw NotImplementedError("filtering results from file is not implemented")
            StorageLocation.REDIS -> {
                try {
                    redisClient!!.jsonGet(REDIS_KEY, JsonPath("$[?($filter)]")).let { outer ->
                        if (outer !is JSONArray) throw Exception("unexpected result: $outer")
                        outer.toList().filterIsInstance<Map<*,*>>().map { Result.from(it) }
                    }
                } catch (e: JedisConnectionException) {
                    logger.error() { "failed to read results: ${e.message} ($filter)" }
                    emptyList()
                }
            }
        }
    }

    private fun getAll(): List<Result> {
        return when (storageLocation) {
            StorageLocation.FILE -> path.readText().let { json ->
                Json.decodeFromString<List<Result>>(json)
            }
            StorageLocation.REDIS -> {
                try {
                    redisClient!!.jsonGet(REDIS_KEY, JsonPath("$.*")).let { outer ->
                        if (outer !is JSONArray) throw Exception("unexpected result: $outer")
                        outer.toList().filterIsInstance<Map<*, *>>().map { Result.from(it) }
                    }
                } catch (e: JedisConnectionException) {
                    logger.error() { "failed to read results: ${e.message}" }
                    emptyList()
                }
            }
        }
    }

    fun Result.identity() = PuzzleIdentity(year, day, part)
    fun Result.toInstant() = Instant.ofEpochMilli(timestamp)!!
    private fun get(year: Int? = null): List<Result> {
        return try {
            when (storageLocation) {
                StorageLocation.FILE -> path.readText().let { json ->
                    Json.decodeFromString<List<Result>>(json)
                }
                StorageLocation.REDIS -> when(year) {
                    null -> getAll()
                    else -> getWithFilter("@.year == $year")
                }
            }
        } catch (e: NoSuchFileException) {
            emptyList()
        } catch (e: Exception) {
            logger.error(e, year) { "failed to read results" }
            emptyList()
        }
    }

    fun getProgress(puzzle: PuzzleIdentity): Iterable<Result> {
        return getProgress(puzzle.year, puzzle.day, puzzle.part)
    }

    private fun getProgress(year: Int, day: Int, part: Int): Iterable<Result> {
        return when (storageLocation) {
            StorageLocation.FILE -> {
                get(year).filter { it.day == day && it.part == part }
            }

            StorageLocation.REDIS -> {
                getWithFilter("@.year == $year && @.day == $day && @.part == $part")
            }
        }
    }

    private val json = Json { prettyPrint = true }
    fun save(result: Result) {
        when (storageLocation) {
            StorageLocation.FILE -> {
                val current = get()
                path.writeText(json.encodeToString((current.filter {
                    it.year != result.year || it.day != result.day || it.part != result.part || it.timestamp != result.timestamp || it.cookie != result.cookie
                } + result).sortedWith(compareBy({ it.timestamp }, { it.day }, { it.part }))))
            }
            StorageLocation.REDIS -> {
                try {
                    redisClient!!.jsonArrAppend(REDIS_KEY, JsonPath.ROOT_PATH, result.toJsonObject())
                } catch (e: JedisConnectionException) {
                    logger.error() { "failed to save result: ${e.message}" }
                }
            }
        }
    }

    /**
     * Saves version of the results that doesn't contain any sensitive information.
     */
    fun saveCleaned() {
        val data = get()
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

            put("results", data.size)
            put("years", buildJsonObject {
                data.groupBy { it.year }.mapValues { (year, days) ->
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
        return when (storageLocation) {
            StorageLocation.FILE -> {
                return get(year).find { it.day == day && it.part == part && it.verified }
            }

            StorageLocation.REDIS -> {
                return getWithFilter("@.year == $year && @.day == $day && @.part == $part && @.verified == true").firstOrNull()
            }
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
        data class ResultIdentity(val puzzle: PuzzleIdentity, val cookie: String)
        val verifiedResults = results.filter { it.verified && it.cookie != null }.associateBy { ResultIdentity(it.identity(), it.cookie!!) }
        val unverifiedResults = results.filter { !it.verified && it.cookie != null && !verifiedResults.containsKey(ResultIdentity(it.identity(), it.cookie!!)) }.groupBy { ResultIdentity(it.identity(), it.cookie!!) }

        val bestUnverifiedResults = unverifiedResults.mapValues { (_, v) -> v.maxByOrNull { it.timestamp }!! }

        if (bestUnverifiedResults.isNotEmpty()) {
            logger.log(AocLogger.Main) { "attempting to verify ${bestUnverifiedResults.size} unverified results" }
        }

        bestUnverifiedResults.entries.forEach { (identity, last) ->
            logger.log(
                identity.puzzle
            ) { "verifying last result ${resultTheme(last.result!!)} from ${(System.currentTimeMillis() - last.timestamp).milliseconds} ago" }
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
                logger.log(identity.puzzle) { "verified result" }
            } else {
                logger.log(identity.puzzle) { "not verified" }
            }
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
