package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

object Day2 : AocDay(2024, 2) {
    private fun List<Int>.isTotallySafe(): Boolean {
        val up = (this[1] - this[0]) > 0
        var last1: Int = this[0]
        var current: Int
        for (v in 1 until this.size) {
            current = this[v]
            val diff = current - last1
            if ((diff !in 1..3 && diff !in -3..-1) || (up != (diff > 0))) {
                return false
            }
            last1 = current
        }
        return true
    }

    override suspend fun part1(): Int = coroutineScope {
        inputLinesList.chunked(100).map { lines ->
            async {
                lines.count { line -> line.split(" ").map { it.toInt() }.isTotallySafe() }
            }
        }.awaitAll().sum()
    }

    override suspend fun part2(): Int = coroutineScope {
        inputLinesList.chunked(100).map { lines ->
            async {
                lines.count {
                    val report = it.split(" ").map { it.toInt() }

                    if (report.isTotallySafe()) {
                        return@count true
                    }

                    for (removed in report.indices) {
                        val removedReport = report.toMutableList().also { it.removeAt(removed) }
                        if (removedReport.isTotallySafe()) {
                            return@count true
                        }
                    }

                    false
                }
            }
        }.awaitAll().sum()
    }
}
