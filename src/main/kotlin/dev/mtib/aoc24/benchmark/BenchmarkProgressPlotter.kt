package dev.mtib.aoc24.benchmark

import dev.mtib.aoc24.Results
import dev.mtib.aoc24.Results.toInstant
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleXDateTime
import org.jetbrains.letsPlot.themes.elementBlank
import org.jetbrains.letsPlot.themes.elementRect
import org.jetbrains.letsPlot.themes.elementText
import org.jetbrains.letsPlot.themes.theme
import java.util.TimeZone
import kotlin.io.path.Path

class BenchmarkProgressPlotter(
    private val day: Int,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun plot() {
        val results = mapOf(
            1 to Results.getProgress(day, 1),
            2 to Results.getProgress(day, 2)
        )

        val validated = results.mapValues { partResults ->
            partResults.value.find { it.verified && it.result != null }?.result
        }

        val data = results.mapValues { partResults ->
            partResults.value.sortedBy { it.timestamp }
        }.mapValues { partResults ->
            val validatedResult = validated[partResults.key]
            buildMap<String, MutableList<Any>> {
                partResults.value.forEach {
                    if (it.benchmarkMicros == null || (validatedResult != null && validatedResult != it.result)) {
                        return@forEach
                    }
                    getOrPut("x") { mutableListOf() }.add(
                        it.toInstant()
                            .plusMillis(TimeZone.getTimeZone("Europe/Copenhagen").getOffset(it.timestamp).toLong())
                    )
                    getOrPut("y${partResults.key}") { mutableListOf() }.add(it.benchmarkMicros / 1000.0)
                }
            }
        }

        val plot = letsPlot() + geomLine(
            data = data[1]!!,
            color = "#44a",
            alpha = 0.9,
            manualKey = "part1",
            linetype = validated[1]?.let { "solid" } ?: "dashed"
        ) {
            x = "x"
            y = "y1"
        } + geomLine(
            data = data[2]!!,
            color = "#a44",
            alpha = 0.9,
            manualKey = "part2",
            linetype = validated[2]?.let { "solid" } ?: "dashed"
        ) {
            x = "x"
            y = "y2"
        } + scaleXDateTime(
            format = "%d. %H:%M"
        ) + theme(
            text = elementText(color = "#fff"),
            axis = elementText(color = "#fff"),
            rect = elementBlank(),
            panelGridMajorX = elementText(color = "#333"),
            panelGridMajorY = elementText(color = "#333"),
            plotBackground = elementRect(fill = "#150808"),
        ) + labs(
            title = "Day $day benchmark progress",
            x = "Time",
            y = "Runtime [ms]"
        )

        ggsave(
            plot = plot,
            filename = "benchmark_${day.toString().padStart(2, '0')}_progress.png",
            path = Path(System.getenv("PWD")!!).resolve(Path("./src/main/resources")).toString()
        )
    }
}
