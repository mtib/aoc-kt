package dev.mtib.aoc.benchmark

import dev.mtib.aoc.util.AocLogger
import dev.mtib.aoc.util.Results
import dev.mtib.aoc.util.Results.toInstant
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.geom.geomPoint
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
        private val logger = AocLogger.new { }
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

        val failedData = results.mapValues { partResults ->
            partResults.value.filter { it.benchmarkMicros != null || (validated[partResults.key] != null && validated[partResults.key] != it.result) }
        }.mapValues {
            buildMap<String, MutableList<Any>> {
                it.value.forEach {
                    getOrPut("x") { mutableListOf() }.add(
                        it.toInstant()
                            .plusMillis(TimeZone.getTimeZone("Europe/Copenhagen").getOffset(it.timestamp).toLong())
                    )
                    getOrPut("y${it.part}") { mutableListOf() }.add(it.benchmarkMicros!!.div(1000.0))
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
        } + geomPoint(
            data = data[1]!!,
            color = "#44a",
            alpha = 0.9,
            manualKey = "part1",
        ) {
            x = "x"
            y = "y1"
        } + geomPoint(
            data = failedData[1]!!,
            color = "#44a",
            alpha = 0.5,
            manualKey = "part1",
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
        } + geomPoint(
            data = data[2]!!,
            color = "#a44",
            alpha = 0.9,
            manualKey = "part2",
        ) {
            x = "x"
            y = "y2"
        } + geomPoint(
            data = failedData[2]!!,
            color = "#a44",
            alpha = 0.5,
            manualKey = "part2",
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

        val filename = "benchmark_${day.toString().padStart(2, '0')}_progress.png"
        logger.log(day) { "saving benchmark progress plot to $filename" }

        ggsave(
            plot = plot,
            filename = filename,
            path = Path(System.getenv("PWD")!!).resolve(Path("./src/main/resources")).toString()
        )
    }
}
