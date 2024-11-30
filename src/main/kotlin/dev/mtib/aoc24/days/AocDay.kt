package dev.mtib.aoc24.days

import arrow.core.Option
import arrow.core.toOption
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.milliseconds

open class AocDay(
    private val day: Int,
) : IAocDay {
    init {
        this.also { solutions[day] = it }
    }

    companion object {
        private val solutions = mutableMapOf<Int, IAocDay>()
        private val logger = KotlinLogging.logger {}

        fun get(day: Int): Option<IAocDay> = solutions[day].toOption()

        fun getAll(): Map<Int, IAocDay> = solutions.toList().associateBy({ it.first }, { it.second })

        fun getReleaseTime(day: Int): ZonedDateTime {
            return ZonedDateTime.of(2024, 12, day, 0, 0, 0, 0, ZoneId.of("UTC-5"))
        }

        fun load() {
            val packageName = AocDay::class.java.packageName
            (1..25).forEach {
                try {
                    Class.forName("$packageName.AocDay${it.toString().padStart(2, '0')}")
                    logger.trace { "Day $it loaded" }
                } catch (e: ClassNotFoundException) {
                    logger.trace { "Day $it not found" }
                }
            }
        }
    }

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
    )

    val input: String by lazy {
        if (!Path(inputLocation).exists()) {
            if (releaseTime.isAfter(ZonedDateTime.now())) {
                throw DayNotReleasedException(day)
            }
            fetchInput()
        }
        Path(inputLocation).readText()
    }

    val inputLines by lazy { input.lines().toTypedArray() }

    override suspend fun part1(): String {
        throw NotImplementedError()
    }

    override suspend fun part2(): String {
        throw NotImplementedError()
    }
}