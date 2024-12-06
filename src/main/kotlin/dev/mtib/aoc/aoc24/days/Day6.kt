package dev.mtib.aoc.aoc24.days

import arrow.atomic.AtomicInt
import arrow.core.Tuple4
import dev.mtib.aoc.aoc24.days.Day6.Direction.*
import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.AocLogger.Companion.logger
import dev.mtib.aoc.util.chunkedParMap
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object Day6: AocDay(2024, 6) {
    private enum class Direction {
        N, E, S, W
    }
    override suspend fun part1(): Any {
        var posX: Int = 0
        var posY: Int = 0
        var dir: Direction = N

        val x = inputLinesList[0].length
        val y = inputLinesList.size

        outer@for ((y, line) in inputLinesList.withIndex()) {
            for ((x, char) in line.withIndex()) {
                when (char) {
                    '^','v','<','>' -> {
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

        val visited = mutableSetOf<Pair<Int, Int>>()

        var nextX = 0
        var nextY = 0
        while (true) {
            visited.add(posX to posY)

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
        }
        return visited.size
    }

    override suspend fun part2(): Any {
        var posX: Int = 0
        var posY: Int = 0
        var dir: Direction = N

        val x = inputLinesList[0].length
        val y = inputLinesList.size

        outer@for ((y, line) in inputLinesList.withIndex()) {
            for ((x, char) in line.withIndex()) {
                when (char) {
                    '^','v','<','>' -> {
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

        val visited = mutableSetOf<Pair<Int, Int>>()

        val bumpedInto = mutableSetOf<Triple<Int, Int, Boolean>>()

        var nextX = 0
        var nextY = 0
        while (true) {
            visited.add(posX to posY)

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
                    N -> {
                        dir = E
                        bumpedInto.add(Triple(nextX, nextY, true))
                    }
                    E -> dir = S
                    S -> {
                        dir = W
                        bumpedInto.add(Triple(nextX, nextY, false))
                    }
                    W -> dir = N
                }
            }
            when (dir) {
                N -> posY--
                E -> posX++
                S -> posY++
                W -> posX--
            }
        }

        // now try for all bumped into to find that rect.

        return coroutineScope {
            val channel = Channel<Tuple4<Int, Int, Int, Int>>(Channel.UNLIMITED)
            val found = AtomicInt(0)
            val tested = AtomicInt(0)
            launch {
                bumpedInto.chunkedParMap(1) {
                    it.forEach {
                        val (initialX, initialY, isUp) = it

                        // probe rect (kinky)
                        for (width in 1 until x) {
                            val firstCornerX = if (isUp) initialX + width else initialX - width
                            val firstCornerY = if (isUp) initialY + 1 else initialY - 1

                            val corner1 = (inputLinesList.getOrNull(firstCornerY)?.getOrNull(firstCornerX) ?: break) == '#'

                            for (height in 1 until y) {
                                val tested = tested.incrementAndGet()
                                val secondCornerY = if (isUp) firstCornerY + height else firstCornerY - height
                                val secondCornerX = if (isUp) firstCornerX - 1 else firstCornerX + 1
                                val corner2 =
                                    (inputLinesList.getOrNull(secondCornerY)?.getOrNull(secondCornerX) ?: break) == '#'
                                val corner3 = (inputLinesList.getOrNull(if (isUp) secondCornerY - 1 else secondCornerY + 1)
                                    ?.getOrNull(if (isUp) initialX - 1 else initialX + 1) ?: break) == '#'

                                val count = 1 + (if (corner1) 1 else 0) + (if (corner2) 1 else 0) + (if (corner3) 1 else 0)

                                if (tested % 1000 == 0) {
                                    logger.log { "Tested $tested, found $found" }
                                }

                                if (count == 3) {
                                    found.incrementAndGet()
                                    channel.send(
                                        if (isUp) Tuple4(
                                            initialX,
                                            initialY,
                                            width,
                                            height
                                        ) else Tuple4(
                                            initialX - width + 1,
                                            secondCornerY,
                                            width,
                                            height
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                channel.close()
            }

            not(2312)
            not(2109)
            not(3221)
            channel.toList().toSet().also {logger.log{ it.toString()}} .count().also {
                logger.log { "had ${found.get() - it} duplicates" }
            }
        }
    }
}