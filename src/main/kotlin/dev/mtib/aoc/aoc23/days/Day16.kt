package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.days.Day16.Cell.TileType
import dev.mtib.aoc.aoc23.days.Day16.Cell.TileType.Companion.toTileType
import dev.mtib.aoc.aoc23.util.AbstractDay

object Day16 : AbstractDay(16) {

    private data class Cell(val tile: TileType, var energized: Boolean = false) {
        enum class TileType(val char: Char) {
            Empty('.'),
            HorizontalSplitter('-'),
            VerticalSplitter('|'),
            ForwardSlashMirror('/'),
            BackwardSlashMirror('\\');

            companion object {
                fun Char.toTileType(): TileType {
                    return entries.find { it.char == this } ?: throw IllegalArgumentException("Unknown tile type $this")
                }
            }

            fun refect(inDirection: Direction): Direction {
                return when (this) {
                    Empty -> throw IllegalArgumentException("Empty cells cannot reflect")
                    HorizontalSplitter -> throw IllegalArgumentException("Horizontal splitters cannot reflect")
                    VerticalSplitter -> throw IllegalArgumentException("Vertical splitters cannot reflect")
                    ForwardSlashMirror -> when (inDirection) {
                        Direction.Up -> Direction.Left
                        Direction.Right -> Direction.Down
                        Direction.Down -> Direction.Right
                        Direction.Left -> Direction.Up
                    }

                    BackwardSlashMirror -> when (inDirection) {
                        Direction.Up -> Direction.Right
                        Direction.Right -> Direction.Up
                        Direction.Down -> Direction.Left
                        Direction.Left -> Direction.Down
                    }
                }
            }
        }
    }

    private enum class Direction(val dx: Int, val dy: Int) {
        Up(0, -1),
        Right(1, 0),
        Down(0, 1),
        Left(-1, 0);

        fun invert(): Direction {
            return when (this) {
                Up -> Down
                Right -> Left
                Down -> Up
                Left -> Right
            }
        }

        val horizontal: Boolean
            get() = this == Left || this == Right

        val vertical: Boolean
            get() = this == Up || this == Down
    }

    private data class Position(val x: Int, val y: Int) {
        fun move(direction: Direction): Position {
            return Position(x + direction.dx, y + direction.dy)
        }
    }

    private data class Beam(var position: Position, var direction: Direction)

    private fun simulate(
        input: Array<Array<Cell>>,
        beam: Beam = Beam(Position(0, 0), Direction.Right)
    ): Array<Array<Cell>> {
        val beams = mutableListOf(beam)

        var lastEnergized = 0

        val seenBeams = mutableSetOf<Beam>()
        for (iteration in 1..(input.size * input[0].size * 2)) {
            if (lastEnergized < iteration - 10 || beams.size == 0) {
                break
            }
            beams.removeIf { beam ->
                beam.position.x < 0 || beam.position.x >= input[0].size || beam.position.y < 0 || beam.position.y >= input.size || beam in seenBeams
            }
            seenBeams.addAll(beams.map { it.copy() })
            val newBeams = mutableListOf<Beam>()
            for (beam in beams) {
                val cell = input[beam.position.y][beam.position.x]
                if (!cell.energized) {
                    lastEnergized = iteration
                    cell.energized = true
                }
                when (cell.tile) {
                    TileType.Empty -> {
                        beam.position = beam.position.move(beam.direction)
                    }

                    TileType.HorizontalSplitter -> {
                        if (beam.direction.horizontal) {
                            beam.position = beam.position.move(beam.direction)
                            continue
                        }
                        beam.direction = Direction.Left
                        beam.position = beam.position.move(beam.direction)
                        newBeams.add(Beam(beam.position.move(Direction.Right), Direction.Right))
                    }

                    TileType.VerticalSplitter -> {
                        if (beam.direction.vertical) {
                            beam.position = beam.position.move(beam.direction)
                            continue
                        }
                        beam.direction = Direction.Down
                        beam.position = beam.position.move(beam.direction)
                        newBeams.add(Beam(beam.position.move(Direction.Up), Direction.Up))
                    }

                    TileType.ForwardSlashMirror -> {
                        beam.direction = cell.tile.refect(beam.direction.invert())
                        beam.position = beam.position.move(beam.direction)
                    }

                    TileType.BackwardSlashMirror -> {
                        beam.direction = cell.tile.refect(beam.direction.invert())
                        beam.position = beam.position.move(beam.direction)
                    }
                }
            }
            beams.addAll(newBeams)
        }

        return input
    }

    private fun parse(input: Array<String>): Array<Array<Cell>> {
        return input.map { it.toCharArray().map { Cell(it.toTileType()) }.toTypedArray() }.toTypedArray()
    }

    override fun solvePart1(input: Array<String>): Any? {
        return simulate(parse(input)).sumOf { row -> row.count { it.energized } }
    }

    private fun bruteForcePart2(input: Array<String>): Int {
        class Guess(val position: Position, val direction: Direction)

        val map = parse(input)
        return buildList {
            for (y in input.indices) {
                add(Guess(Position(0, y), Direction.Right))
                add(Guess(Position(input[y].length - 1, y), Direction.Left))
            }
            for (x in input[0].indices) {
                add(Guess(Position(x, 0), Direction.Down))
                add(Guess(Position(x, input.size - 1), Direction.Up))
            }
        }.maxOf { guess ->
            simulate(map, Beam(guess.position, guess.direction)).sumOf { row -> row.count { it.energized } }
                .also {
                    map.forEach { row -> row.forEach { it.energized = false } }
                }
        }
    }

    override fun solvePart2(input: Array<String>): Any? {
        return bruteForcePart2(input)
    }
}
