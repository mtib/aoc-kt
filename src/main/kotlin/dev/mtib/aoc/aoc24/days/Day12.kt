package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.milliseconds

object Day12: AocDay(2024, 12) {
    private enum class Side {
        TOP, RIGHT, BOTTOM, LEFT
    }
    class Region(
        private val representant: Char
    ) {
        private val _cells: MutableSet<Pair<Int, Int>> = mutableSetOf()

        fun add(i: Int, j: Int) {
            _cells.add(Pair(i, j))
        }

        operator fun contains(element: Pair<Int, Int>): Boolean {
            return _cells.contains(element)
        }

        val cells
            get() = _cells.toSet()

        val size
            get() = _cells.size

        private fun getPerimeter(): Set<Pair<Side, Pair<Int, Int>>> {
            return cells.mapNotNull { cell ->
                val i = cell.first
                val j = cell.second
                val neighbors = listOf(
                    Side.TOP to Pair(i - 1, j),
                    Side.RIGHT to Pair(i, j + 1),
                    Side.BOTTOM to Pair(i + 1, j),
                    Side.LEFT to Pair(i, j - 1)
                ).filter {
                    val (_, neighbor) = it
                    neighbor !in cells
                }.map {
                    val (side, _) = it
                    side to cell
                }
                neighbors.ifEmpty {
                    null
                }
            }.flatten().toSet()
        }

        fun getPerimiterSize(): Int {
            return getPerimeter().size
        }

        fun getSidesCount(): Int {
            val perimeter = getPerimeter()
            return perimeter.filter { (itSide, itPosition) ->
                val (itY, itX) = itPosition
                !perimeter.any { (otherSide, otherPosition) ->
                    val (otherY, otherX) = otherPosition
                    if (itSide != otherSide) {
                        return@any false
                    }
                    when (itSide) {
                        Side.TOP -> otherX == itX - 1 && otherY == itY
                        Side.RIGHT -> otherY == itY - 1 && otherX == itX
                        Side.BOTTOM -> otherX == itX + 1 && otherY == itY
                        Side.LEFT -> otherY == itY + 1 && otherX == itX
                    }
                }
            }.count()
        }
    }

    fun regions(map: Array<CharArray>): Set<Region> {
        val unknownField = map.indices.map { i -> map[i].indices.map { j -> Pair(i, j) } }.flatten().toMutableSet()
        val regions = mutableSetOf<Region>()

        while (unknownField.isNotEmpty()) {
            val pick = unknownField.first()
            val char = map[pick.first][pick.second]
            val region = Region(char)

            val toVisit = mutableSetOf(pick)
            while (toVisit.isNotEmpty()) {
                val current = toVisit.first()
                val currentChar = map[current.first][current.second]
                toVisit.remove(current)

                if (currentChar == char) {
                    region.add(current.first, current.second)
                    val neighbors = setOf(
                        Pair(current.first - 1, current.second),
                        Pair(current.first + 1, current.second),
                        Pair(current.first, current.second - 1),
                        Pair(current.first, current.second + 1)
                    ).filter { it in unknownField && it !in region }

                    neighbors.forEach {
                        toVisit.add(it)
                    }
                }
            }

            regions.add(region)

            unknownField.removeAll(region.cells)
        }

        return regions
    }

    override suspend fun part1(): Any {
        return regions(inputLinesArray).sumOf { (it.size * it.getPerimiterSize()).toLong() }
    }

    override suspend fun part2(): Any {
        return regions(inputLinesArray).sumOf { (it.size * it.getSidesCount()).toLong() }
    }
}