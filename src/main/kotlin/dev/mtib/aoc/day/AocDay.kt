package dev.mtib.aoc.day

import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import dev.mtib.aoc.regenerateAocDayLoader
import dev.mtib.aoc.util.*
import dev.mtib.aoc.util.AocLogger.Companion.error
import dev.mtib.aoc.util.AocLogger.Companion.logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.reflections.Reflections
import java.math.BigInteger
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.milliseconds

open class AocDay(
    val year: Int,
    val day: Int,
) : PuzzleExecutor {
    val identity = Day(year, day)

    init {
        solutions.add(this)
    }

    class CiSkipException : Exception("This test is not supported in CI")
    val cpu = Runtime.getRuntime().availableProcessors()

    companion object {
        private val solutions = mutableListOf<AocDay>()
        private val client by lazy { OkHttpClient() }
        fun years(): Set<Year> = solutions.map { it.identity.toYear() }.toSet()
        fun get(day: Day): Option<AocDay> = solutions.find { it.identity == day }.toOption()
        fun getAll(): List<AocDay> = solutions.toList()
        fun getAll(year: Year): List<AocDay> = solutions.filter { it.identity.toYear() == year }
        fun getByClass(clazz: Class<*>): Option<AocDay> =
            solutions.find { it::class.java == clazz }.toOption()
        fun skipCi() {
            if (System.getenv("CI") != null) {
                throw CiSkipException()
            }
        }

        fun getReleaseTime(day: Day): ZonedDateTime {
            return ZonedDateTime.of(day.year, 12, day.day, 0, 0, 0, 0, ZoneId.of("UTC-5"))
        }

        fun load() {
            AocDayLoader.allDays.forEach {
                if (it !in solutions) {
                    solutions.add(it)
                }
            }
            try {
                val packageName = AocDay::class.java.packageName.split(".").dropLast(1).joinToString(".")

                Reflections(packageName).getSubTypesOf(AocDay::class.java).forEach {
                    Class.forName(it.name) // This loads the "class" of the object, which registers itself in `solutions` during init.
                }
                regenerateAocDayLoader()
            } catch (e: Exception) {
                AocLogger.Main.error(e) { "failed to load AocDay classes" }
            }
        }
    }

    open val pool: CoroutineDispatcher = Dispatchers.Default
    var benchmarking: Boolean = false
    var partMode: Int? = null

    data class Hint(val result: BigInteger, val direction: Direction) {
        enum class Direction {
            TooHigh, TooLow, Unknown
        }
    }

    private val storedRejections = mutableMapOf<Int, MutableSet<Hint>>()
    internal fun AocDay.not(result: Any, direction: Hint.Direction = Hint.Direction.Unknown) {
        val partMode = partMode
        if (benchmarking || partMode == null) {
            return
        }

        val value = result.tryToBigInteger()

        storedRejections.getOrPut(partMode, ::mutableSetOf).add(Hint(value, direction))
        checkResultsStillPossible(partMode)
    }

    private fun Any.tryToBigInteger() = when (this) {
        is BigInteger -> this
        is Int -> this.toBigInteger()
        is Long -> this.toBigInteger()
        is String -> this.toBigInteger()
        else -> throw IllegalArgumentException("Unsupported type: ${this::class.simpleName}")
    }

    private fun checkResultsStillPossible(part: Int) {
        var min = BigInteger.TEN.pow(100).negate()
        var max = BigInteger.TEN.pow(100)

        storedRejections[part]?.forEach {
            when (it.direction) {
                Hint.Direction.TooLow -> {
                    if (it.result > min) {
                        min = it.result.inc()
                    }
                }

                Hint.Direction.TooHigh -> {
                    if (it.result < max) {
                        max = it.result.dec()
                    }
                }

                Hint.Direction.Unknown -> {}
            }
        }

        if (min > max) {
            throw IllegalStateException("No possible results left for part $part")
        }

        if (min == max) {
            logger.log(identity) { "Only one possible result left: ${styleResult(min.toString())}" }
        }
    }

    fun compareHints(part: Int, result: Any): Either<Hint.Direction, Unit> {
        val value = result.tryToBigInteger()
        storedRejections[part]?.forEach {
            val compare = value.compareTo(it.result)
            if (compare == 0) {
                return it.direction.left()
            }
            if (compare < 0 && it.direction == Hint.Direction.TooLow) {
                return it.direction.left()
            }
            if (compare > 0 && it.direction == Hint.Direction.TooHigh) {
                return it.direction.left()
            }
        }
        return Unit.right()
    }

    val releaseTime = getReleaseTime(identity)
    val inputLocation = "src/main/resources/day_${year}_${day.toString().padStart(2, '0')}.txt"
    fun fetchInput() {
        val token = System.getenv("SESSION")
        val ketchupToken = System.getenv("KETCHUP_TOKEN")
        val snowflake = System.getenv("USER_SNOWFLAKE")
        if (token.isNullOrBlank() && (ketchupToken.isNullOrBlank() || snowflake.isNullOrBlank())) {
            logger.log(day = identity) {"No SESSION (AoC cookie) or KETCHUP_TOKEN environment variable found, please provide at least one."}
        }
        val path = Path(inputLocation)

        val body: String? = run download@{
            runCatching ketchup@{
                if (!ketchupToken.isNullOrBlank()) {
                    if (snowflake.isNullOrBlank()) {
                        logger.log(day=identity) {"No USER_SNOWFLAKE environment variable found, please provide one."}
                        return@ketchup
                    }
                    val ketchupRequest = Request.Builder()
                        .url("https://api.ketchup.mtib.dev/aoc/input/$snowflake/$year/$day")
                        .get()
                        .addHeader("Authorization", "Bearer $ketchupToken")
                        .build()
                    val response = client.newCall(ketchupRequest).execute()
                    if (response.code != 200) {
                        logger.log(day=identity) {"failed to fetch input from Ketchup: ${response.code}"}
                        return@ketchup
                    }
                    logger.log(day=identity) {"fetched input from ketchup"}
                    return@download response.body!!.string().also {
                        response.close()
                    }
                }
            }.onFailure { logger.error(e = it, year=identity.year, day=identity.day, part=null) { "error getting input from ketchup"} }

            runCatching aoc@{
                if (!token.isNullOrBlank()) {
                    val aocRequest = Request.Builder()
                        .url("https://adventofcode.com/$year/day/$day/input")
                        .get()
                        .addHeader("Cookie", "session=$token")
                        .build()
                    val response = client.newCall(aocRequest).execute()
                    if (response.code != 200) {
                        logger.log(day=identity) {"failed to fetch input from AoC: ${response.code}"}
                        return@aoc
                    }
                    val body = response.body!!.string()
                    response.close()
                    logger.log(day=identity) {"fetched input from aoc"}
                    if (!ketchupToken.isNullOrBlank() && !snowflake.isNullOrBlank()) {
                        val ketchupRequest = Request.Builder()
                            .url("https://api.ketchup.mtib.dev/aoc/input/$snowflake/$year/$day")
                            .put(body.toRequestBody())
                            .addHeader("Authorization", "Bearer $ketchupToken")
                            .build()
                        val ketchupResponse = client.newCall(ketchupRequest).execute()
                        if (ketchupResponse.code != 200) {
                            logger.log(day=identity) {"failed to store input in Ketchup: ${ketchupResponse.code}"}
                        }
                        ketchupResponse.close()
                        logger.log(day=identity) {"stored aoc input in ketchup"}
                    }
                    return@download body
                }
            }.onFailure { logger.error(e = it, year=identity.year, day=identity.day, part=null) { "error getting input from AoC"} }

            null
        }
        if (body == null) {
            return
        }
        path.writeText(body)

        // Some time for FS to sync
        Thread.sleep(10)
    }

    class DayNotReleasedException(day: Day) : Exception(
        "Day $day of year ${day.year} is not released yet, it will be released at ${
            ZonedDateTime.ofInstant(getReleaseTime(day).toInstant(), ZoneId.of("CET"))
                .format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm z"))
        } (in ${
            (getReleaseTime(day).toInstant().toEpochMilli() - System.currentTimeMillis()).milliseconds
        })"
    ) {
        val releaseTime = getReleaseTime(day)
        val waitDuration = (releaseTime.toInstant().toEpochMilli() - System.currentTimeMillis()).milliseconds
    }

    fun createTestFile() {
        if (year < 2024) {
            return
        }
        if (releaseTime.isAfter(ZonedDateTime.now())) {
            return
        }
        if (!System.getenv("CI").isNullOrBlank()) {
            return
        }
        val token = System.getenv("SESSION")
        if (token.isNullOrBlank()) {
            return
        }
        val outPath = Path("src/test/kotlin/dev/mtib/aoc/aoc${year%100}/days/Day${day}Test.kt")
        if (outPath.exists()) {
            return
        }
        val request = Request.Builder()
            .url("https://adventofcode.com/$year/day/$day")
            .get()
            .addHeader("Cookie", "session=$token")
            .build()
        val client = OkHttpClient()
        val response = client.newCall(request).execute()
        val body = response.body!!.string()
        val codeRegex = Regex("""<pre><code>(.*?)</code></pre>""", RegexOption.DOT_MATCHES_ALL)
        val codeSnippets = codeRegex.findAll(body)
            .map { it.groupValues[1].replace("&lt;", "<").replace("&gt;", ">") }
            .filterNot { it.contains("<em>") }
            .distinct()

        logger.log(identity) { "creating ${outPath.fileName} with code snippets" }

        outPath.writeText(buildString {
            appendLine("""
                package dev.mtib.aoc.aoc24.days

                import io.kotest.core.spec.style.FunSpec
                
                class Day${day}Test : FunSpec({
            """.trimIndent())
            codeSnippets.forEachIndexed { index, code ->
                appendLine(
                    "    val snippet${index} = \"\"\"\n${
                        code.trimEnd().lines().joinToString("\n") { " ".repeat(8) + it }
                    }\n    \"\"\".trimIndent()"
                )
            }
            appendLine("""
                    context("part1") {
                        test("doesn't throw") {
                            try {
                                Day${day}.part1()
                            } catch (e: NotImplementedError) {
                                // Ignore allowed exception
                            }
                        }
                    }
                    context("part2") {
                        test("doesn't throw") {
                            try {
                                Day${day}.part2()
                            } catch (e: NotImplementedError) {
                                // Ignore allowed exception
                            }
                        }
                    }
                })
            """.trimIndent())
        })
    }

    suspend fun <T> withInput(input: String, block: suspend AocDay.() -> T): T {
        fakedInput = input
        _inputArray = null
        _inputLinesList = null
        _inputLinesArray = null
        return this.block().also {
            fakedInput = null
            _inputArray = null
            _inputLinesList = null
            _inputLinesArray = null
        }
    }

    private var fakedInput: String? = null
    private val realInput: String by lazy {
        if (!Path(inputLocation).exists()) {
            if (releaseTime.isAfter(ZonedDateTime.now())) {
                throw DayNotReleasedException(identity)
            }
            fetchInput()
        }
        Path(inputLocation).readText().trimEnd('\n')
    }

    val input: String
        get() = fakedInput ?: realInput

    private var _lineLength: Int? = null
    val lineLength: Int
        get() = _lineLength ?: input.indexOf('\n').also {
        _lineLength = it
    }
    private var _inputArray: CharArray? = null

    val inputArray: CharArray
        get() = _inputArray ?: input.toCharArray().also {
            _inputArray = it
        }

    fun getChar(x: Int, y: Int): Char {
        return inputArray[y * lineLength + x + y]
    }

    fun getChar(pair: Pair<Int, Int>): Char {
        return inputArray[pair.second * lineLength + pair.first + pair.second]
    }

    fun getCharOrNull(pair: Pair<Int, Int>): Char? {
        val index = pair.second * lineLength + pair.first + pair.second
        if (index < 0 || index >= inputArray.size) {
            return null
        }
        return inputArray[index]
    }


    private var _inputLinesList: List<String>? = null
    val inputLinesList: List<String>
        get() = _inputLinesList ?: input.lines().also {
            _inputLinesList = it
        }

    private var _inputLinesArray: Array<CharArray>? = null
    val inputLinesArray: Array<CharArray>
        get() = _inputLinesArray ?: inputLinesList.map{it.toCharArray()}.toTypedArray().also {
            _inputLinesArray = it
        }

    suspend fun part1(fakeInput: String): Any {
        return withInput(fakeInput) {
            part1()
        }
    }

    override suspend fun part1(): Any {
        throw NotImplementedError()
    }

    suspend fun part2(fakeInput: String): Any {
        return withInput(fakeInput) {
            part2()
        }
    }

    override suspend fun part2(): Any {
        throw NotImplementedError()
    }

    open suspend fun teardown() {
        // No cheating!
        _inputLinesArray = null
        _inputLinesList = null
        _inputArray = null
    }
}
