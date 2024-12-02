package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import dev.mtib.aoc.util.AocLogger
import kotlin.math.abs

object Day11 : AbstractDay(11) {
    private val logger = AocLogger.new { }
    private fun expand(input: Array<String>): List<String> {
        val horizontalExpansionColumns =
            (0..<input[0].length).associateWith { column -> input.all { it[column] != '#' } }
        return input.flatMapIndexed { rowNumber, line ->
            val horizontalExpansion = line.flatMapIndexed { index, c ->
                if (horizontalExpansionColumns[index]!!) {
                    listOf(c, c)
                } else {
                    listOf(c)
                }
            }.joinToString("")
            if (line.contains("#")) {
                listOf(horizontalExpansion)
            } else {
                listOf(horizontalExpansion, horizontalExpansion)
            }
        }
    }

    private fun expandRealGood(input: Array<String>): List<String> {
        val horizontalExpansionColumns =
            (0..<input[0].length).associateWith { column -> input.all { it[column] != '#' } }
        return input.mapIndexed { rowNumber, line ->
            val horizontalExpansion = line.mapIndexed { index, c ->
                if (horizontalExpansionColumns[index]!!) {
                    '|'
                } else {
                    c
                }
            }.joinToString("")
            if (line.contains("#")) {
                horizontalExpansion
            } else {
                horizontalExpansion.map { '-' }.joinToString("")
            }
        }
    }

    data class Position(val x: Int, val y: Int) {
        fun distanceTo(other: Position): Int {
            return abs(x - other.x) + abs(y - other.y)
        }

        fun walkDistanceTo(other: Position, map: List<String>): Long {
            var counter = 0L
            val xDiff = abs(x - other.x)
            val yDiff = abs(y - other.y)

            val minX = minOf(x, other.x)
            val minY = minOf(y, other.y)

            val maxX = maxOf(x, other.x)
            val maxY = maxOf(y, other.y)
            return xDiff + yDiff + (map.slice(minY..maxY)
                .count { it[minX] == '-' } + map[maxY].slice(minX..maxX).count { it == '|' }) * 999_999L
        }
    }

    private fun findPairs(input: List<String>): Collection<Pair<Position, Position>> {
        val galaxies = input.mapIndexed { y, line ->
            line.mapIndexedNotNull { x, c ->
                if (c == '#') {
                    Position(x, y)
                } else {
                    null
                }
            }
        }.flatten()
        return galaxies.flatMapIndexed { i, a ->
            galaxies.drop(i + 1).mapNotNull { b ->
                if (a != b) {
                    Pair(a, b)
                } else {
                    null
                }
            }
        }
    }

    override fun solvePart1(input: Array<String>): Any? {
        val expanded = expand(input)
        return findPairs(expanded).sumOf { it.first.distanceTo(it.second).toLong() }
    }

    override fun solvePart2(input: Array<String>): Any? {
        val expanded = expandRealGood(input)

        logger.log {
            val check = expand(input)
            findPairs(check).forEach { (first, second) ->
                val d1 = first.distanceTo(second).toLong()
                val d2 = first.walkDistanceTo(second, check)
                require(d1 == d2) { "Distance mismatch: $d1 != $d2" }
            }
            "checked distance match"
        }

        return findPairs(expanded).sumOf { it.first.walkDistanceTo(it.second, expanded) }
    }
}
