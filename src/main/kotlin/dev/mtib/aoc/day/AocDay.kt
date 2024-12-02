package dev.mtib.aoc.day

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.some
import arrow.core.toOption
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import org.reflections.Reflections
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.milliseconds

open class AocDay(
    val day: Int,
) : PuzzleExecutor {
    init {
        this.also { solutions[day] = it }
    }

    companion object {
        private val solutions = mutableMapOf<Int, AocDay>()
        fun get(day: Int): Option<AocDay> = solutions[day].toOption()
        fun getAll(): Map<Int, AocDay> = solutions.toList().associateBy({ it.first }, { it.second })
        fun getByClass(clazz: Class<*>): Option<AocDay> =
            solutions.values.find { it::class.java == clazz }.toOption()

        fun getReleaseTime(day: Int): ZonedDateTime {
            return ZonedDateTime.of(2024, 12, day, 0, 0, 0, 0, ZoneId.of("UTC-5"))
        }

        fun load() {
            val packageName = AocDay::class.java.packageName.split(".").dropLast(1).joinToString(".")

            Reflections(packageName).getSubTypesOf(AocDay::class.java).forEach {
                Class.forName(it.name) // This loads the "class" of the object, which registers itself in `solutions` during init.
            }
        }
    }

    open val pool: CoroutineDispatcher = Dispatchers.Default
    var benchmarking: Boolean = false
    var partMode: Int? = null
    val releaseTime = getReleaseTime(day)
    val inputLocation = "src/main/resources/day${day.toString().padStart(2, '0')}.txt"
    fun fetchInput() {
        val token = System.getenv("SESSION")
        if (token.isNullOrBlank()) {
            println("No AOC_SESSION environment variable found, please set it to your session token")
            return
        }
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://adventofcode.com/2024/day/$day/input")
            .get()
            .addHeader("Cookie", "session=$token")
            .build()
        val response = client.newCall(request).execute()
        val body = response.body!!.string()
        Path(inputLocation).writeText(body)
    }

    class DayNotReleasedException(day: Int) : Exception(
        "Day $day is not released yet, it will be released at ${
            ZonedDateTime.ofInstant(getReleaseTime(day).toInstant(), ZoneId.of("CET"))
                .format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm z"))
        } (in ${
            (getReleaseTime(day).toInstant().toEpochMilli() - System.currentTimeMillis()).milliseconds
        })"
    ) {
        val releaseTime = getReleaseTime(day)
        val waitDuration = (releaseTime.toInstant().toEpochMilli() - System.currentTimeMillis()).milliseconds
    }

    suspend fun <T> withInput(input: String, block: suspend AocDay.() -> T): T {
        fakedInput = input.some()
        return this.block().also { fakedInput = None }
    }

    private var fakedInput: Option<String> = None
    val realInput: String by lazy {
        if (!Path(inputLocation).exists()) {
            if (releaseTime.isAfter(ZonedDateTime.now())) {
                throw DayNotReleasedException(day)
            }
            fetchInput()
        }
        Path(inputLocation).readText().trimEnd('\n')
    }

    val input: String
        get() = fakedInput.getOrElse { realInput }

    val inputLinesList: List<String>
        get() = input.lines()

    val inputLinesArray: Array<String>
        get() = inputLinesList.toTypedArray()

    override suspend fun part1(): String {
        throw NotImplementedError()
    }

    override suspend fun part2(): String {
        throw NotImplementedError()
    }
}
