package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs

object Day2 : AocDay(2024, 2) {
    private fun List<Int>.isTotallySafe(): Boolean {
        for (v in indices) {
            val last2 = getOrNull(v - 2)
            val last1 = getOrNull(v - 1)
            val current = this[v]

            if (last2 != null && last1 != null) {
                if (last2 < last1 && last1 > current) {
                    return false
                }
                if (last2 > last1 && last1 < current) {
                    return false
                }
            }

            if (last1 != null) {
                if (abs(last1 - current).let {
                        it == 0 || it > 3
                    }) {
                    return false
                }
            }
        }
        return true
    }

    override suspend fun part1(): String = coroutineScope {
        inputLinesList.filter {
            it.split(" ").map { it.toInt() }.isTotallySafe()
        }.size.toString()
    }

    override suspend fun part2(): String = coroutineScope {
        inputLinesList.filter {
            val report = it.split(" ").map { it.toInt() }

            if (report.isTotallySafe()) {
                return@filter true
            }

            for (removed in report.indices) {
                val removedReport = report.toMutableList().also { it.removeAt(removed) }
                if (removedReport.isTotallySafe()) {
                    return@filter true
                }
            }

            false
        }.size.toString()
    }
}
