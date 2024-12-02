package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.chunkedParMap
import kotlinx.coroutines.coroutineScope

object Day2 : AocDay(2024, 2) {
    private fun List<Int>.isTotallySafe(skip: Int? = null): Boolean {
        val up = if (skip !in 0..1) (this[1] - this[0]) > 0 else (this[3] - this[2]) > 0
        var last1: Int = this[if (skip != 0) 0 else 1]
        var current: Int
        for (v in (if (skip != 0) 1 else 2) until this.size) {
            if (v == skip) {
                continue
            }
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
        inputLinesList.chunkedParMap(100) { lines ->
            lines.count { line -> line.split(" ").map { it.toInt() }.isTotallySafe() }
        }.sum()
    }

    override suspend fun part2(): Int = coroutineScope {
        inputLinesList.chunkedParMap(100) { lines ->
            lines.count {
                val report = it.split(" ").map { it.toInt() }

                if (report.isTotallySafe()) {
                    return@count true
                }

                for (removed in report.indices) {
                    if (report.isTotallySafe(removed)) {
                        return@count true
                    }
                }

                false
            }
        }.sum()
    }
}
