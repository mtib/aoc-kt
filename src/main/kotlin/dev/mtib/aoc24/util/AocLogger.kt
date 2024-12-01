package dev.mtib.aoc24.util

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import dev.mtib.aoc24.days.AocDay

class AocLogger private constructor(
    private val name: String,
) {
    private val aocDay: AocDay? = try {
        Class.forName(name)
    } catch (e: ClassNotFoundException) {
        null
    }?.let { clazz ->
        if (clazz.superclass == AocDay::class.java) {
            AocDay.getByClass(clazz).getOrNull()
        } else {
            null
        }
    }

    private fun getStyledPrefix(day: Int?, part: Int?): String {
        return when {
            day != null -> TextColors.brightWhite(
                "[${
                    TextColors.brightYellow(
                        day.toString()
                            .padStart(2, '0')
                    )
                }|${
                    (when (part) {
                        null -> TextColors.gray("*")
                        else -> TextColors.brightYellow(part.toString())
                    })
                }]"
            )

            else -> TextColors.brightWhite("[" + TextColors.gray("main") + "]")
        }
    }

    fun log(day: Int? = aocDay?.day, part: Int? = aocDay?.partMode, message: () -> String) {
        if (aocDay?.benchmarking == true) {
            return
        }
        terminal.println("${getStyledPrefix(day, part)}${TextColors.white(" ${message()}")}")
    }

    suspend fun logSuspend(day: Int? = aocDay?.day, part: Int? = aocDay?.partMode, message: suspend () -> String) {
        if (aocDay?.benchmarking == true) {
            return
        }
        message().let { log(day, part) { it } }
    }

    fun error(e: Throwable? = null, day: Int? = aocDay?.day, part: Int? = aocDay?.partMode, message: () -> String) {
        log(day, part) { "${(TextColors.red + TextStyles.bold)("ERROR")} ${TextColors.red(message())}" }
        e?.printStackTrace()
    }

    companion object {
        private val terminal = Terminal()
        fun new(block: AocLogger.() -> Unit): AocLogger {
            return AocLogger(
                name = block.javaClass.name.let { name ->
                    when {
                        name.contains("Kt$") -> name.substringBefore("Kt$")
                        name.contains("$") -> name.substringBefore("$")
                        else -> name
                    }
                }
            ).apply { block() }
        }
    }
}
