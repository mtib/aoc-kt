package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import dev.mtib.aoc.util.AocLogger
import kotlin.math.absoluteValue

class Day18 : AbstractDay(18) {

    enum class Direction(val char: Char) {
        Up('U'),
        Right('R'),
        Down('D'),
        Left('L');

        companion object {
            fun fromChar(char: Char): Direction {
                return values().first { it.char == char }
            }
        }
    }

    companion object {
        private val logger = AocLogger.new {}
    }

    data class DigInstruction(val direction: Direction, val steps: Int, val color: String) {
        companion object {
            fun fromLine(string: String): DigInstruction {
                val parts = string.split(" ")
                val direction = Direction.fromChar(parts[0][0])
                val steps = parts[1].toInt()
                val color = parts[2].substring(1, parts[2].length - 1)
                return DigInstruction(direction, steps, color)
            }
        }

        fun toPart2(): DigInstruction {
            val newDirection = when (color[6]) {
                '0' -> Direction.Right
                '1' -> Direction.Down
                '2' -> Direction.Left
                '3' -> Direction.Up
                else -> throw IllegalArgumentException()
            }
            val newSteps = color.slice(1..5).toInt(16)
            return DigInstruction(newDirection, newSteps, "part2")
        }
    }

    fun shoelace(points: List<Pair<Number, Number>>): Long {
        val points = points.map { (x, y) -> Pair(x.toLong(), y.toLong()) }
        val area = points.mapIndexed { index, (x, y) ->
            val (nextX, nextY) = points[(index + 1) % points.size]
            (nextX - x) * (nextY + y)
        }.sum().absoluteValue / 2

        fun Pair<Long, Long>.dist(other: Pair<Long, Long>) =
            (first - other.first).absoluteValue + (second - other.second).absoluteValue
        return (points.zipWithNext()
            .sumOf { (a, b) -> a.dist(b) } + points.first().dist(points.last())) / 2 + area + 1
    }

    /** only returns inner points */
    fun outerFloodFill(dug: Collection<Pair<Int, Int>>): Set<Pair<Int, Int>> {
        val minX = dug.minOf { it.first }
        val maxX = dug.maxOf { it.first }
        val minY = dug.minOf { it.second }
        val maxY = dug.maxOf { it.second }

        val toVisit = mutableSetOf<Pair<Int, Int>>(Pair(minX - 1, minY - 1))
        val visited = mutableSetOf<Pair<Int, Int>>()
        while (toVisit.isNotEmpty()) {
            val (x, y) = toVisit.first()
            toVisit.remove(Pair(x, y))
            visited.add(Pair(x, y))
            for (i in -1..1) {
                for (j in -1..1) {
                    val (newX, newY) = Pair(x + i, y + j)
                    if (newX in minX - 1..maxX + 1 && newY in minY - 1..maxY + 1 && Pair(newX, newY) !in dug && Pair(
                            newX,
                            newY
                        ) !in visited
                    ) {
                        toVisit.add(Pair(newX, newY))
                    }
                }
            }
        }
        val innerPoints = mutableSetOf<Pair<Int, Int>>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                if (Pair(x, y) !in dug && Pair(x, y) !in visited) {
                    innerPoints.add(Pair(x, y))
                }
            }
        }
        return innerPoints
    }

    private fun simulateDig(digInstructions: List<DigInstruction>): List<Pair<Int, Int>> {
        val dug = mutableListOf(Pair(0, 0))
        for (digInstruction in digInstructions) {
            val (direction, steps) = digInstruction
            val (x, y) = dug.last()
            for (i in 1..steps) {
                val (newX, newY) = when (direction) {
                    Direction.Up -> Pair(x, y + i)
                    Direction.Right -> Pair(x + i, y)
                    Direction.Down -> Pair(x, y - i)
                    Direction.Left -> Pair(x - i, y)
                }
                dug.add(Pair(newX, newY))
            }
        }
        return dug
    }

    private fun simulateFastDig(digInstructions: List<DigInstruction>): List<Pair<Int, Int>> {
        val dug = mutableListOf(Pair(0, 0))
        for (digInstruction in digInstructions) {
            val (direction, steps) = digInstruction
            val (x, y) = dug.last()
            val (newX, newY) = when (direction) {
                Direction.Up -> Pair(x, y + steps)
                Direction.Right -> Pair(x + steps, y)
                Direction.Down -> Pair(x, y - steps)
                Direction.Left -> Pair(x - steps, y)
            }
            dug.add(Pair(newX, newY))
        }
        return dug
    }

    override fun solvePart1(input: Array<String>): Any? {
        val digInstructions = input.map { DigInstruction.fromLine(it) }

        /*
        val dug = benchmark("subList or slice", {
            simulateDig(digInstructions)
        }, {
            simulateFastDig(digInstructions)
        })
        //subList or slice a: 200ms, sublist or slice b: 2µs
         */
        logger.log {
            val dug = simulateDig(digInstructions)
            val dugSet = dug.toSet()
            require(dugSet.size == dug.size - 1) { "dugSet.size: ${dugSet.size}, dug.size: ${dug.size}" }
            val a = dugSet.size + outerFloodFill(dugSet).size
            val b = shoelace(dug.slice(0..<dug.size - 1))
            require(a.toLong() == b) { "a: $a, b: $b" }

            val c = shoelace(simulateFastDig(digInstructions))
            require(c == b) { "c: $c, b: $b" }
            "verified shoelace"
        }
        val dug = simulateFastDig(digInstructions)
        return shoelace(dug.subList(0, dug.size - 1))
    }

    override fun solvePart2(input: Array<String>): Any? {
        val digInstructions = input.map { DigInstruction.fromLine(it).toPart2() }
        val dug = simulateFastDig(digInstructions)
        logger.log {
            require(dug[0] == dug.last()) { "dug[0]: ${dug[0]}, dug.last(): ${dug.last()}" }
            "verified dug"
        }
        return shoelace(dug.subList(0, dug.size - 1))
    }
}
