package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.aoc24.days.Day6.Direction.*
import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.chunkedParMap

object Day6: AocDay(2024, 6) {
    private enum class Direction(private val char: Char) {
        N('^'), E('>'), S('v'), W('<');
        companion object {
            fun fromChar(char: Char): Direction {
                return entries.find { it.char == char } ?: throw IllegalArgumentException("Unknown direction: $char")
            }
            val all = entries.toSet()
            val allChars = all.map { it.char }.toSet()
        }
    }

    private data class Solution(
        val visited: Set<Triple<Int, Int, Direction>>,
        val visitedInOrder: List<Triple<Int, Int, Direction>>,
        val startPosition: Triple<Int, Int, Direction>,
    )

    private fun Triple<Int, Int, Direction>.backward(): Triple<Int, Int, Direction> {
        return when (third) {
            N -> Triple(first, second + 1, third)
            E -> Triple(first - 1, second, third)
            S -> Triple(first, second - 1, third)
            W -> Triple(first + 1, second, third)
        }
    }

    private fun runPart1(startPosition: Triple<Int, Int, Direction>? = null, shadow: Pair<Int, Int>? = null, skipPart2Work: Boolean = false): Solution {
        val x = lineLength
        val y = inputArray.size / x

        val startPosition = startPosition ?: run {
            for (ly in 0..y) {
                for (lx in 0..x) {
                    val char = getChar(lx, ly)
                    if (char in Direction.allChars) {
                        return@run Triple(lx, ly, Direction.fromChar(char))
                    }
                }
            }
            throw IllegalStateException("No starting position found")
        }
        var (posX, posY, dir) = startPosition

        val visited = mutableSetOf<Triple<Int, Int, Direction>>()
        val visitedInOrder = mutableListOf<Triple<Int, Int, Direction>>()

        var nextX: Int
        var nextY: Int
        outer@while (true) {

            when (skipPart2Work) {
                true -> {
                    visited.add(Triple(posX, posY, startPosition.third))
                }
                false -> {
                    val current = Triple(posX, posY, dir)
                    if (current in visited) {
                        throw IllegalStateException("Loop detected")
                    }
                    visited.add(current)
                    visitedInOrder.add(current)
                }
            }

            var bad = false
            do {
                nextX = posX
                nextY = posY
                when (dir) {
                    N -> nextY--
                    E -> nextX++
                    S -> nextY++
                    W -> nextX--
                }
                if (nextX !in 0 until x || nextY !in 0 until y) {
                    break@outer
                }
                bad = (shadow?.first == nextX && shadow.second == nextY) || getChar(nextX, nextY) == '#'
                if (bad) {
                    dir = when (dir) {
                        N -> E
                        E -> S
                        S -> W
                        W -> N
                    }
                }
            } while (bad)
            posX = nextX
            posY = nextY
        }
        return Solution(visited, visitedInOrder, startPosition)
    }
    override suspend fun part1(): Any {
        return runPart1(skipPart2Work = true).visited.size
    }

    override suspend fun part2(): Any {
        val result = runPart1()
        val walkedInto = result.visitedInOrder.filterNot { it.first == result.startPosition.first && it.second == result.startPosition.second }
            .fold(mutableListOf<Triple<Int, Int, Direction>>()) { acc, (x, y, d) ->
                if (acc.any { it.first == x && it.second == y }) acc else acc.apply {
                    add(
                        Triple(x, y, d)
                    )
                }
            }

        val count = walkedInto.chunkedParMap(walkedInto.size / cpu) { walkedIntoChunk ->
            walkedIntoChunk.count { stepped ->
                val (steppedX, steppedY) = stepped
                if (getChar(steppedX, steppedY) != '.') {
                    return@count false
                }

                try {
                    runPart1(startPosition = stepped.backward(), shadow = stepped.first to stepped.second)
                    false
                } catch (e: IllegalStateException) {
                    if (e.message == "Loop detected") {
                        true
                    } else {
                        throw e
                    }
                }
            }
        }.sum()

        return count
    }
}