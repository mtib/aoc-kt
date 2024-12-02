package dev.mtib.aoc.day

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.some
import arrow.core.toOption
import dev.mtib.aoc.util.Day
import dev.mtib.aoc.util.Year
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
    val year: Int,
    val day: Int,
) : PuzzleExecutor {
    val identity = Day(year, day)

    init {
        solutions.add(this)
    }

    companion object {
        private val solutions = mutableListOf<AocDay>()
        fun years(): Set<Year> = solutions.map { it.identity.toYear() }.toSet()
        fun get(day: Day): Option<AocDay> = solutions.find { it.identity == day }.toOption()
        fun getAll(): List<AocDay> = solutions.toList()
        fun getAll(year: Year): List<AocDay> = solutions.filter { it.identity.toYear() == year }
        fun getByClass(clazz: Class<*>): Option<AocDay> =
            solutions.find { it::class.java == clazz }.toOption()

        fun getReleaseTime(day: Day): ZonedDateTime {
            return ZonedDateTime.of(day.year, 12, day.day, 0, 0, 0, 0, ZoneId.of("UTC-5"))
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
    val releaseTime = getReleaseTime(identity)
    val inputLocation = "src/main/resources/day_${year}_${day.toString().padStart(2, '0')}.txt"
    fun fetchInput() {
        val token = System.getenv("SESSION")
        if (token.isNullOrBlank()) {
            println("No AOC_SESSION environment variable found, please set it to your session token")
            return
        }
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://adventofcode.com/$year/day/$day/input")
            .get()
            .addHeader("Cookie", "session=$token")
            .build()
        val response = client.newCall(request).execute()
        val body = response.body!!.string()
        Path(inputLocation).writeText(body)
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

    suspend fun <T> withInput(input: String, block: suspend AocDay.() -> T): T {
        fakedInput = input.some()
        return this.block().also { fakedInput = None }
    }

    private var fakedInput: Option<String> = None
    val realInput: String by lazy {
        if (!Path(inputLocation).exists()) {
            if (releaseTime.isAfter(ZonedDateTime.now())) {
                throw DayNotReleasedException(identity)
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

    override suspend fun part1(): Any {
        throw NotImplementedError()
    }

    override suspend fun part2(): Any {
        throw NotImplementedError()
    }

    open suspend fun teardown() {}
}
