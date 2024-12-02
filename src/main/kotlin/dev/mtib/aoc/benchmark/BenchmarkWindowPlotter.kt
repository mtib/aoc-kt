package dev.mtib.aoc.benchmark

import dev.mtib.aoc.util.AocLogger
import dev.mtib.aoc.util.PuzzleIdentity
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.themes.elementBlank
import org.jetbrains.letsPlot.themes.elementRect
import org.jetbrains.letsPlot.themes.elementText
import org.jetbrains.letsPlot.themes.theme
import kotlin.io.path.Path
import kotlin.time.Duration

class BenchmarkWindowPlotter(
    private val puzzle: PuzzleIdentity,
    private val windowSize: Int,
    private val durations: List<Duration>
) {
    companion object {
        private val logger = AocLogger.new { }
    }

    fun plot(skip: Int = 5) {
        if (durations.isEmpty()) {
            logger.error(puzzle = puzzle) { "not enough data points to plot" }
            return
        }

        val data = mutableMapOf(
            "x" to durations.indices.toList(),
            "window" to mutableListOf<Double?>(),
            "result" to mutableListOf<Double>(),
        )

        for (i in durations.indices) {
            val window = durations.subList((i - windowSize).coerceAtLeast(0), i + 1)
            val windowMs = window
                .let { tail ->
                    tail.sumOf { sample -> sample.inWholeMicroseconds.toBigDecimal() } / tail.size.toBigDecimal()
                }
                .movePointLeft(3)
                .toDouble()
            data["window"]?.let {
                if (it is MutableList) {
                    if (i < skip) {
                        it.add(null)
                    } else {
                        it.add(windowMs)
                    }
                }
            }
            data["result"]?.let {
                if (it is MutableList) {
                    it.add(durations[i].inWholeMicroseconds.toDouble() / 1000.0)
                }
            }
        }

        val year = puzzle.year
        val day = puzzle.day
        val part = puzzle.part

        val plot = letsPlot(data) + geomLine(
            color = "#22a",
            alpha = 0.8,
            manualKey = "runtime"
        ) {
            x = "x"
            y = "result"
        } + geomLine(
            color = "#f33",
            manualKey = "window"
        ) {
            x = "x"
            y = "window"
        } + theme(
            text = elementText(color = "#fff"),
            axis = elementText(color = "#fff"),
            rect = elementBlank(),
            panelGridMajorX = elementText(color = "#333"),
            panelGridMajorY = elementText(color = "#333"),
            plotBackground = elementRect(fill = "#150808"),
        ) + labs(
            title = "AoC $year day $day part $part benchmark",
            subtitle = "Window size: $windowSize, min: ${durations.minOrNull()}, max: ${durations.maxOrNull()}",
            x = "Window end index",
            y = "Average time [ms]"
        )

        val filename = "benchmark_${year}_${day.toString().padStart(2, '0')}_$part.png"
        logger.log(puzzle) { "saving benchmark window plot to $filename" }

        ggsave(
            plot = plot,
            filename = filename,
            path = Path(System.getenv("PWD")!!).resolve(Path("./src/main/resources")).toString()
        )
    }
}
