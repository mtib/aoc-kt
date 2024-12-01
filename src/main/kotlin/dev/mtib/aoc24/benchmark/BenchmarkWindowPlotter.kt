package dev.mtib.aoc24.benchmark

import io.github.oshai.kotlinlogging.KotlinLogging
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
    private val day: Int,
    private val part: Int,
    private val windowSize: Int,
    private val durations: List<Duration>
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun plot(skip: Int = 5) {
        if (durations.isEmpty()) {
            logger.warn { "Not enough data points to plot" }
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
            title = "Day $day part $part benchmark",
            subtitle = "Window size: $windowSize, min: ${durations.minOrNull()}, max: ${durations.maxOrNull()}",
            x = "Window end index",
            y = "Average time [ms]"
        )

        ggsave(
            plot = plot,
            filename = "benchmark_${day.toString().padStart(2, '0')}_$part.png",
            path = Path(System.getenv("PWD")!!).resolve(Path("./src/main/resources")).toString()
        )
    }
}
