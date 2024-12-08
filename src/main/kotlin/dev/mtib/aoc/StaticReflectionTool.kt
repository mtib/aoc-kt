package dev.mtib.aoc

import dev.mtib.aoc.day.AocDay
import kotlin.io.path.Path
import kotlin.io.path.writeText

fun regenerateAocDayLoader() {
    val loaderClass = buildString {
        val classes = AocDay.getAll().map { day ->
            day.javaClass
        }.sortedBy { it.name }

        appendLine("package dev.mtib.aoc.util")
        appendLine()
        appendLine("object AocDayLoader {")
        appendLine("    init {")
        classes.forEach { clazz ->
            appendLine("        Class.forName(\"${clazz.name}\")")
        }
        appendLine("    }")
        appendLine("    ")
        appendLine("    val allDays: List<${AocDay::class.java.name}> = listOf(")
        classes.forEach { clazz ->
            appendLine("        ${clazz.name},")
        }
        appendLine("    )")
        appendLine("}")
    }
    Path("src/main/kotlin/dev/mtib/aoc/util/AocDayLoader.kt").writeText(loaderClass)
}

fun main() {
    regenerateAocDayLoader()
}