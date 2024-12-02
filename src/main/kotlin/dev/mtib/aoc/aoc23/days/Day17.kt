package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import dev.mtib.aoc.util.AocLogger
import java.util.PriorityQueue

object Day17 : AbstractDay(17) {
    private val logger = AocLogger.new {}

    private data class Position(val x: Int, val y: Int) {
        operator fun plus(other: Position): Position {
            return Position(x + other.x, y + other.y)
        }

        operator fun minus(other: Position): Position {
            return Position(x - other.x, y - other.y)
        }

        override fun toString(): String {
            return when {
                x == 0 && y == 0 -> "(0)"
                x == 0 && y == 1 -> "Down"
                x == 0 && y == -1 -> "Up"
                x == 1 && y == 0 -> "Right"
                x == -1 && y == 0 -> "Left"
                else -> "($x,$y)"
            }
        }

        operator fun times(i: Int): Position {
            return Position(x * i, y * i)
        }
    }

    private fun bfs(
        input: Array<IntArray>,
        start: Position = Position(0, 0),
        minStraight: Int = 1,
        maxStraight: Int = 3
    ): List<Position> {
        data class MoveHistory(val positions: List<Position>, val cost: Int) {
            fun lastStraight(): Int {
                val secondToLastPosition = positions.getOrNull(positions.size - 2) ?: return 0
                val lastMove = positions.last() - secondToLastPosition
                var straight = 0
                while (positions.getOrNull(positions.size - straight - 2) == positions.last() - lastMove * (straight + 1)) {
                    straight++
                }
                return straight
            }
        }

        val queue = PriorityQueue<MoveHistory>(2048, compareBy { it.cost })

        queue.add(MoveHistory(listOf(start), 0))

        val possibleDirections = listOf(
            Position(0, -1),
            Position(1, 0),
            Position(0, 1),
            Position(-1, 0),
        )

        data class CacheKey(val position: Position, val lastMove: Position?, val lastStraight: Int)

        val minSeen = mutableMapOf<CacheKey, Int>()
        while (queue.isNotEmpty()) {
            val current = queue.poll()!!
            val currentPosition = current.positions.last()

            val straight = current.lastStraight()
            val lastDirection = run {
                val beforeLast = current.positions.getOrNull(current.positions.size - 2) ?: return@run null
                currentPosition - beforeLast
            }
            val cacheKey = CacheKey(currentPosition, lastDirection, straight)

            if (cacheKey in minSeen) {
                if (current.cost >= minSeen[cacheKey]!!) {
                    continue
                }
            }
            minSeen[cacheKey] = current.cost

            val backDirection = lastDirection?.let { Position(-it.x, -it.y) }

            possibleDirections.filter {
                (it != backDirection && (straight < maxStraight || it != lastDirection) && (straight >= minStraight || it == lastDirection)) || lastDirection == null
            }.forEach { move ->
                val nextPosition = currentPosition + move
                if (nextPosition.x !in input[0].indices || nextPosition.y !in input.indices) {
                    return@forEach
                }
                val nextMoveHistory = MoveHistory(
                    current.positions + nextPosition,
                    current.cost + input[nextPosition.y][nextPosition.x]
                )
                if (nextPosition == Position(
                        input[0].size - 1,
                        input.size - 1
                    ) && nextMoveHistory.lastStraight() >= minStraight
                ) {
                    return nextMoveHistory.positions
                }
                queue.add(
                    nextMoveHistory
                )
            }
        }
        throw IllegalStateException("No path found")
    }

    private fun parseMap(input: Array<String>): Array<IntArray> {
        return input.map { it.map { it.digitToInt() }.toIntArray() }.toTypedArray()
    }

    private fun printMap(map: Array<IntArray>, path: List<Position>) {
        logger.log {
            buildString {
                map.forEachIndexed { y, row ->
                    row.forEachIndexed { x, it ->
                        path.indexOf(Position(x, y)).takeIf { it >= 0 }?.let {
                            val before = path.getOrNull(it - 1) ?: return@let null
                            append(
                                when (path[it] - before) {
                                    Position(0, -1) -> "↑"
                                    Position(1, 0) -> "→"
                                    Position(0, 1) -> "↓"
                                    Position(-1, 0) -> "←"
                                    else -> throw IllegalStateException("Invalid path")
                                }
                            )
                        } ?: append(it)
                    }
                    appendLine()
                }
            }
        }
    }

    override fun solvePart1(input: Array<String>): Any? {
        val parsedMap = parseMap(input)
        val path = bfs(parsedMap)
        printMap(parsedMap, path)
        return path.drop(1).sumOf { input[it.y][it.x].digitToInt() }
    }

    override fun solvePart2(input: Array<String>): Any? {
        logger.log {
            val exampleMap = parseMap(
                """111111111111
999999999991
999999999991
999999999991
999999999991
""".lines().filter { it.isNotBlank() }.toTypedArray()
            )
            val examplePath = bfs(exampleMap, minStraight = 4, maxStraight = 10)
            printMap(exampleMap, examplePath)
            examplePath.drop(1).sumOf { exampleMap[it.y][it.x] }.also {
                require(it == 71) { "Example path is wrong, got $it != 71" }
            }
            "verified path"
        }
        val parsedMap = parseMap(input)
        val path = bfs(parsedMap, minStraight = 4, maxStraight = 10)
        printMap(parsedMap, path)
        return path.drop(1).sumOf { input[it.y][it.x].digitToInt() }
    }
}

