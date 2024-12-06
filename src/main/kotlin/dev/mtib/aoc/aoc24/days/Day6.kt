package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.aoc24.days.Day6.Direction.*
import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.AocLogger.Companion.logger
import dev.mtib.aoc.util.chunkedParMap

object Day6: AocDay(2024, 6) {
    private enum class Direction {
        N, E, S, W
    }
    private fun runPart1(lines: List<String>): Int {
        var posX: Int = 0
        var posY: Int = 0
        var dir: Direction = N

        val x = lines[0].length
        val y = lines.size

        outer@for ((ly, line) in lines.withIndex()) {
            for ((lx, char) in line.withIndex()) {
                when (char) {
                    '^','v','<','>' -> {
                        posX = lx
                        posY = ly
                        dir = when (char) {
                            '^' -> N
                            'v' -> S
                            '<' -> W
                            '>' -> E
                            else -> throw IllegalStateException()
                        }
                        break@outer
                    }
                }
            }
        }

        val visited = mutableSetOf<Triple<Int, Int, Direction>>()

        var nextX = 0
        var nextY = 0
        outer@while (true) {
            val current = Triple(posX, posY, dir)
            if (current in visited) {
                throw IllegalStateException("Loop detected")
            }
            visited.add(current)

            do {
                nextX = posX
                nextY = posY
                when (dir) {
                    N -> nextY--
                    E -> nextX++
                    S -> nextY++
                    W -> nextX--
                }
                if (lines.getOrNull(nextY)?.getOrNull(nextX) == '#') {
                    dir = when (dir) {
                        N -> E
                        E -> S
                        S -> W
                        W -> N
                    }
                } else if (nextX !in 0 until x || nextY !in 0 until y) {
                    break@outer
                }
            } while (lines[nextY][nextX] == '#')
            posX = nextX
            posY = nextY
        }
        return visited.map { it.first to it.second }.toSet().size
    }
    override suspend fun part1(): Any {
        return runPart1(inputLinesList)
    }

    override suspend fun part2(): Any {
        var posX: Int = 0
        var posY: Int = 0
        var dir: Direction = N

        val x = inputLinesList[0].length
        val y = inputLinesList.size
        outer@ for ((y, line) in inputLinesList.withIndex()) {
            for ((x, char) in line.withIndex()) {
                when (char) {
                    '^', 'v', '<', '>' -> {
                        posX = x
                        posY = y
                        dir = when (char) {
                            '^' -> N
                            'v' -> S
                            '<' -> W
                            '>' -> E
                            else -> throw IllegalStateException()
                        }
                        break@outer
                    }
                }
            }
        }

        val walkedInto = mutableSetOf<Pair<Int, Int>>()

        var nextX = 0
        var nextY = 0
        while (true) {

            nextX = posX
            nextY = posY
            when (dir) {
                N -> nextY--
                E -> nextX++
                S -> nextY++
                W -> nextX--
            }
            if (nextX !in 0 until x || nextY !in 0 until y) {
                break
            }
            if (inputLinesList[nextY][nextX] == '#') {
                when (dir) {
                    N -> dir = E
                    E -> dir = S
                    S -> dir = W
                    W -> dir = N
                }
            }
            when (dir) {
                N -> posY--
                E -> posX++
                S -> posY++
                W -> posX--
            }
            walkedInto.add(posX to posY)
        }

        val count = walkedInto.chunkedParMap(10) { walkedIntoChunk ->
            walkedIntoChunk.count { stepped ->
                val (steppedX, steppedY) = stepped
                val fakeList = List(inputLinesList.size) { iterY ->
                    if (iterY == steppedY) {
                        inputLinesList[iterY].toCharArray().let { it[steppedX] = '#'; it.concatToString() }
                    } else {
                        inputLinesList[iterY]
                    }
                }

                try {
                    runPart1(fakeList)
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

        not(1812)
        not(2312)
        not(2109)
        not(3221)
        return count
    }
}