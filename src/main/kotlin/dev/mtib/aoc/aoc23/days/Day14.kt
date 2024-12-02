package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import dev.mtib.aoc.util.AocLogger

class Day14 : AbstractDay(14) {

    companion object {
        private val logger = AocLogger.new {}
    }

    private fun computeLoad(input: Array<Array<Char>>): Int = input.withIndex().sumOf { (line, row) ->
        row.count { it == 'O' } * (input.size - line)
    }

    private fun Array<String>.toBoard(): Array<Array<Char>> =
        this.map { it.toCharArray().toTypedArray() }.toTypedArray()

    override fun solvePart1(input: Array<String>): Int {
        return computeLoad(tilt(input.toBoard(), Direction.UP))
    }

    private val cycles = 1000000000L

    enum class Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private fun tilt(input: Array<Array<Char>>, direction: Direction): Array<Array<Char>> {
        input.withIndex().forEach { (row, line) ->
            line.withIndex().forEach charHandler@{ (column, c) ->
                when (c) {
                    '.' -> {
                        return@charHandler
                    }

                    'O' -> {
                        class Position(val row: Int, val column: Int, val char: Char)

                        val next = sequence<Position> {
                            var dist = 1
                            while (true) {
                                val nextRow = when (direction) {
                                    Direction.UP -> row - dist
                                    Direction.DOWN -> row + dist
                                    else -> row
                                }
                                val nextColumn = when (direction) {
                                    Direction.LEFT -> column - dist
                                    Direction.RIGHT -> column + dist
                                    else -> column
                                }
                                if (nextRow < 0 || nextRow >= input.size || nextColumn < 0 || nextColumn >= input[0].size) {
                                    break
                                }
                                val nextChar = input[nextRow][nextColumn]
                                if (nextChar == '#') {
                                    break
                                }
                                yield(Position(nextRow, nextColumn, nextChar))
                                dist++
                            }
                        }
                        val moveTo = next.findLast { it.char == '.' }
                        if (moveTo != null) {
                            input[moveTo.row][moveTo.column] = 'O'
                            input[row][column] = '.'
                        } else {
                            input[row][column] = 'O'
                        }
                    }

                    '#' -> {
                        return@charHandler
                    }
                }
            }
        }
        return input
    }

    fun Array<Array<Char>>.hash(): Int {
        return this.sumOf { it.contentHashCode() }
    }

    override fun solvePart2(input: Array<String>): Any? {
        logger.log {
            val example = listOf(
                "#.O",
                ".#.",
                "...",
                "OOO"
            ).map { it.toCharArray().toTypedArray() }.toTypedArray()

            Direction.entries.forEach {
                require(
                    tilt(example, it).contentDeepEquals(
                        tilt(
                            tilt(example, it),
                            it
                        )
                    )
                ) { "Tilting twice in the same direction should be the same as tilting once. (broke for $it)" }
                require(
                    tilt(example, it).sumOf { it.count { it == 'O' } } == example.sumOf { it.count { it == 'O' } }
                ) { "Tilting should not change the number of rolling stones. (broke for $it)" }
            }

            "example verified"
        }

        val map = input.toBoard()
        val seenMaps = mutableListOf(map.hash())
        for (it in 1..cycles) {
            tilt(map, Direction.UP)
            tilt(map, Direction.LEFT)
            tilt(map, Direction.DOWN)
            tilt(map, Direction.RIGHT)
            if (map.hash() in seenMaps) {
                val cycleLength = it - seenMaps.lastIndexOf(map.hash())
                logger.log {
                    "Found cycle at index $it (size ${cycleLength})"
                }
                val remainingCycles = (cycles - it) % cycleLength
                for (i in 1..remainingCycles) {
                    tilt(map, Direction.UP)
                    tilt(map, Direction.LEFT)
                    tilt(map, Direction.DOWN)
                    tilt(map, Direction.RIGHT)
                }
                return computeLoad(map)
            }
            seenMaps.add(map.hash())
        }
        return null
    }
}
