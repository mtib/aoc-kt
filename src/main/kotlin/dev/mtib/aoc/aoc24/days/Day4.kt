package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.AocLogger
import dev.mtib.aoc.util.AocLogger.Companion.logger
import dev.mtib.aoc.util.chunkedParMap
import kotlinx.coroutines.coroutineScope

object Day4 : AocDay(2024, 4) {
    private val xmas = arrayOf('X', 'M', 'A', 'S')
    private fun checkXMAS(x: Int, y: Int, direction: Int = 0, step: Int = 0): Int {
        val (ax, ay) = Point.walk(x, y, direction, step)
        if (Point.outside(ax, ay)) {
            return 0
        }
        if (Point.get(ax, ay) != xmas[step]) {
            return 0
        }
        when (step) {
            0 -> {
                return (1..8).sumOf { checkXMAS(x, y, it, 1) }
            }
            1,2-> {
                return checkXMAS(x, y, direction, step + 1)
            }
        }
        return 1
    }

    private fun checkMAS(x: Int, y: Int, direction: Int = 0, step: Int = 0): Boolean {
        val (ax, ay) = Point.walk(x, y, direction, step)
        if (Point.outside(ax, ay)) {
            return false
        }
        when (step) {
            0 -> {
                if (Point.get(ax, ay) != 'A') {
                    return false
                }
                return listOf(2,4,6,8).any {
                    val left = directionMod(it + 2)
                    checkMAS(x, y, it, 1) && checkMAS(x, y, left, 1)
                }
            }
            1 -> {
                val (ox, oy) = Point.opposite(x, y, direction, 1)
                if (Point.outside(ox, oy)) {
                    return false
                }
                if (Point.get(ax, ay) == 'M' && Point.get(ox, oy) == 'S') {
                    return true
                }
            }
        }
        return false
    }

    private fun directionMod(direction: Int): Int = ((direction - 1) % 8) + 1

    private object Point {
            fun get(x: Int, y: Int): Char {
                return inputLinesList[y][x]
            }

            fun outside(x: Int, y: Int): Boolean {
                return x < 0 || x >= inputLinesList[0].length || y < 0 || y >= inputLinesList.size
            }

            fun opposite(x: Int, y: Int, direction: Int, step: Int): Pair<Int, Int> {
                return walk(x, y, directionMod(direction + 4), step)
            }

            fun walk(x: Int, y: Int, direction: Int, step: Int) : Pair<Int, Int> {
                return when (direction) {
                    0 -> if (step == 0) Pair(x, y) else throw IllegalArgumentException("Invalid direction: $direction")
                    1 -> Pair(x, y - step)
                    2 -> Pair(x + step, y - step)
                    3 -> Pair(x + step, y)
                    4 -> Pair(x + step, y + step)
                    5 -> Pair(x, y + step)
                    6 -> Pair(x - step, y + step)
                    7 -> Pair(x - step, y)
                    8 -> Pair(x - step, y - step)
                    else -> throw IllegalArgumentException("Invalid direction: $direction")
                }
            }
    }

    override suspend fun part1(): Any {
        val xRange = 0 until inputLinesList[0].length
        return inputLinesList.indices.chunkedParMap(inputLinesList.size / Runtime.getRuntime().availableProcessors()) { chunk -> chunk.sumOf { y -> xRange.sumOf { x -> checkXMAS(x, y)} } }.sum()
    }

    override suspend fun part2(): Any = coroutineScope {
        logger.log { "Solving part 2" }
        val xRange = (1 until inputLinesList[0].length -1)
        (1 until inputLinesList.size - 1)
            .chunkedParMap(inputLinesList.size / Runtime.getRuntime().availableProcessors()) { yRange -> yRange.sumOf { y -> xRange.count { x-> checkMAS(x, y) }  } }
            .sum()
    }
}
