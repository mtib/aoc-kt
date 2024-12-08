package dev.mtib.aoc.util

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import dev.mtib.aoc.day.AocDay

class AocLogger private constructor(
    private val aocDay: AocDay? = null
) {
    private constructor(className: String) : this(
        aocDay = try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            null
        }?.let { clazz ->
            AocDay.getByClass(clazz).getOrNull()
        }
    )

    object Main

    fun log(main: Main, message: () -> String) {
        log(year = null, day = null, part = null, message = message)
    }

    fun log(year: Year, message: () -> String) {
        log(year = year.toInt(), message = message)
    }

    fun log(day: Day, message: () -> String) {
        log(year = day.year, day = day.toInt(), message = message)
    }

    fun log(puzzle: PuzzleIdentity, message: () -> String) {
        log(year = puzzle.year, day = puzzle.day, part = puzzle.part, message = message)
    }

    fun log(aocDay: AocDay? = this.aocDay, message: () -> String) {
        log(year = aocDay?.year, day = aocDay?.day, part = aocDay?.partMode, message = message)
    }

    private fun log(
        year: Int? = aocDay?.year,
        day: Int? = aocDay?.day,
        part: Int? = aocDay?.partMode,
        message: () -> String
    ) {
        if (aocDay?.benchmarking == true) {
            return
        }
        terminal.println(formatLineAsLog(year = year, day = day, part = part, line = message()))
    }

    suspend fun logSuspend(year: Year, message: suspend () -> String) {
        logSuspend(year = year.toInt(), message = message)
    }

    suspend fun logSuspend(day: Day, message: suspend () -> String) {
        logSuspend(year = day.year, day = day.toInt(), message = message)
    }

    suspend fun logSuspend(puzzle: PuzzleIdentity, message: suspend () -> String) {
        logSuspend(year = puzzle.year, day = puzzle.day, part = puzzle.part, message = message)
    }

    suspend fun logSuspend(aocDay: AocDay? = this.aocDay, message: suspend () -> String) {
        logSuspend(year = aocDay?.year, day = aocDay?.day, part = aocDay?.partMode, message = message)
    }

    private suspend fun logSuspend(
        year: Int? = aocDay?.year,
        day: Int? = aocDay?.day,
        part: Int? = aocDay?.partMode,
        message: suspend () -> String
    ) {
        if (aocDay?.benchmarking == true) {
            return
        }
        message().let { log(year = year, day = day, part = part) { it } }
    }

    fun error(e: Throwable? = null, puzzle: PuzzleIdentity, message: () -> String) {
        error(e, year = puzzle.year, day = puzzle.day, part = puzzle.part, message = message)
    }

    fun error(
        e: Throwable? = null,
        year: Int? = null,
        day: Int? = aocDay?.day,
        part: Int? = aocDay?.partMode,
        message: () -> String
    ) {
        log(year = year, day = day, part = part) {
            "${(TextColors.red + TextStyles.bold)("ERROR")} ${
                TextColors.red(
                    message()
                )
            }"
        }
        e?.printStackTrace()
    }

    companion object {
        private val terminal = Terminal()
        fun new(block: AocLogger.() -> Unit): AocLogger {
            return AocLogger(
                className = block.javaClass.name.let { name ->
                    when {
                        name.contains("Kt$") -> name.substringBefore("Kt$")
                        name.contains("$") -> name.substringBefore("$")
                        else -> name
                    }
                }
            ).apply { block() }
        }

        val AocDay.logger
            get() = AocLogger(this)

        fun Main.error(e: Throwable? = null, message: () -> String) {
            AocLogger(null).error(e, year = null, day = null, part = null, message = message)
        }


        private fun getStyledPrefix(year: Int?, day: Int?, part: Int?): String {
            return when {
                year != null -> TextColors.brightWhite(
                    "[${
                        TextColors.brightYellow(year.toString())
                    }|${
                        when (day) {
                            null -> TextColors.gray("**")
                            else -> TextColors.brightYellow(day.toString().padStart(2, '0'))
                        }
                    }|${
                        (when (part) {
                            null -> TextColors.gray("*")
                            else -> TextColors.brightYellow(part.toString())
                        })
                    }]"
                )

                else -> TextColors.brightWhite("[" + TextColors.gray("         ") + "]")
            }
        }

        fun formatLineAsLog(identity: PuzzleIdentity, line: String): String {
            return formatLineAsLog(identity.year, identity.day, identity.part, line)
        }

        fun formatLineAsLog(year: Int? = null, day: Int? = null, part: Int? = null, line: String): String {
            if (part != null && part !in 1..2) {
                throw IllegalArgumentException("Part must be 1 or 2: $part")
            }
            if (day != null && day !in 1..25) {
                throw IllegalArgumentException("Day must be in 1..25: $day")
            }
            val lines = line.split("\n").dropLastWhile { it.isBlank() }
            return buildString {
                append("${getStyledPrefix(year, day, part)}${TextColors.white(" ${lines[0]}")}")
                if (lines.size > 1) {
                    append("\n")
                    for (i in 1 until lines.size) {
                        append("${TextColors.gray("    ...    ")}${TextColors.white(" ${lines[i]}")}")
                        if (i < lines.size - 1) {
                            append("\n")
                        }
                    }
                }
            }
        }

        val resultTheme = TextStyles.bold + TextColors.brightWhite
    }
}
