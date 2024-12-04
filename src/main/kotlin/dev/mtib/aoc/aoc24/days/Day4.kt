package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.chunkedParMap

object Day4 : AocDay(2024, 4) {
    private val xmas = arrayOf('X', 'M', 'A', 'S')
    private fun checkXMAS(point: Point, direction: Int = 0, step: Int = 0): Int {
        val actual = point.walk(direction, step)
        if (actual.outside()) {
            return 0
        }
        if (actual.get() != xmas[step]) {
            return 0
        }
        when (step) {
            0 -> {
                return (1..8).sumOf { checkXMAS(point, it, 1) }
            }
            1,2-> {
                return checkXMAS(point, direction, step + 1)
            }
        }
        return 1
    }

    private fun checkMAS(point: Point, direction: Int = 0, step: Int = 0): Boolean {
        val actual = point.walk(direction, step)
        if (actual.outside()) {
            return false
        }
        when (step) {
            0 -> {
                if (actual.get() != 'A') {
                    return false
                }
                return listOf(2,4,6,8).any {
                    val left = directionMod(it + 2)
                    checkMAS(point, it, 1) && checkMAS(point, left, 1)
                }
            }
            1 -> {
                val opposite = point.opposite(direction, 1)
                if (opposite.outside()) {
                    return false
                }
                if (actual.get() == 'M' && opposite.get() == 'S') {
                    return true
                }
            }
        }
        return false
    }

    private fun directionMod(direction: Int): Int = ((direction - 1) % 8) + 1

    private class Point(
        val x: Int,
        val y: Int,
    ) {
        fun get(): Char {
            return inputLinesList[y][x]
        }

        fun outside(): Boolean {
            return x < 0 || x >= inputLinesList[0].length || y < 0 || y >= inputLinesList.size
        }

        fun opposite(direction: Int, step: Int): Point {
            return this.walk(directionMod(direction + 4), step)
        }

        fun walk(direction: Int, step: Int) : Point {
            return when (direction) {
                0 -> if (step == 0) this else throw IllegalArgumentException("Invalid direction: $direction")
                1 -> Point(x, y - step)
                2 -> Point(x + step, y - step)
                3 -> Point(x + step, y)
                4 -> Point(x + step, y + step)
                5 -> Point(x, y + step)
                6 -> Point(x - step, y + step)
                7 -> Point(x - step, y)
                8 -> Point(x - step, y - step)
                else -> throw IllegalArgumentException("Invalid direction: $direction")
            }
        }
    }

    override suspend fun part1(): Any {
        return buildList {
            for (y in inputLinesList.indices) {
                for (x in inputLinesList[y].indices) {
                    add(Point(x,y))
                }
            }
        }.chunkedParMap(100) { chunk -> chunk.sumOf { checkXMAS(it) } }.sum()
    }

    override suspend fun part2(): Any {
        return buildList {
            val xlen = inputLinesList[0].length
            val ylen = inputLinesList.size
            for (y in 0 until ylen) {
                for (x in 0 until xlen) {
                    add(Point(x,y))
                }
            }
        }.chunkedParMap(100) { chunk -> chunk.count { point -> checkMAS(point) } }.sum()
    }
}
