package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.days.Day10.Position.Companion.DOWN
import dev.mtib.aoc.aoc23.days.Day10.Position.Companion.LEFT
import dev.mtib.aoc.aoc23.days.Day10.Position.Companion.RIGHT
import dev.mtib.aoc.aoc23.days.Day10.Position.Companion.UP
import dev.mtib.aoc.aoc23.util.AbstractDay
import kotlin.math.absoluteValue

object Day10 : AbstractDay(10) {
    private abstract class Position(val x: Int, val y: Int) {
        companion object {
            val UP = DifferentialPosition(0, -1)
            val RIGHT = DifferentialPosition(1, 0)
            val DOWN = DifferentialPosition(0, 1)
            val LEFT = DifferentialPosition(-1, 0)
            fun cardinalDirections(): List<DifferentialPosition> {
                return listOf(UP, RIGHT, DOWN, LEFT)
            }

            val KEY_DIRECTIONS = mapOf(
                'S' to cardinalDirections(),
                '-' to listOf(LEFT, RIGHT),
                '|' to listOf(UP, DOWN),
                'F' to listOf(DOWN, RIGHT),
                '7' to listOf(LEFT, DOWN),
                'J' to listOf(LEFT, UP),
                'L' to listOf(UP, RIGHT),
                '.' to emptyList(),
            )
        }

        override fun toString(): String {
            return "($x,$y)"
        }

        override fun equals(other: Any?): Boolean {
            if (other is Position) {
                return other.x == x && other.y == y
            }
            return super.equals(other)
        }

        override fun hashCode(): Int {
            return x + 10000 * y
        }
    }

    private class DifferentialPosition(x: Int, y: Int) : Position(x, y) {
        operator fun unaryMinus(): DifferentialPosition {
            return DifferentialPosition(-x, -y)
        }
    }

    private class AbsolutePosition(x: Int, y: Int, val input: Array<String>) : Position(x, y) {
        val symbol by lazy { input[y][x] }
        val potentialNeighborDirections by lazy {
            KEY_DIRECTIONS[symbol]!!
        }
        val connectedNeighbors by lazy {
            val potentialDirections = potentialNeighborDirections
            potentialDirections.filter {
                try {
                    (this + it).potentialNeighborDirections().contains(-it)
                } catch (e: StringIndexOutOfBoundsException) {
                    false
                } catch (e: ArrayIndexOutOfBoundsException) {
                    false
                }
            }
                .map { this + it }
        }

        override fun toString(): String {
            return "'${symbol}'@($x,$y)"
        }

        operator fun plus(other: DifferentialPosition): AbsolutePosition {
            return AbsolutePosition(x + other.x, y + other.y, input)
        }

        operator fun minus(other: DifferentialPosition): AbsolutePosition {
            return AbsolutePosition(x - other.x, y - other.y, input)
        }

        fun potentialNeighborDirections(): List<DifferentialPosition> {
            return potentialNeighborDirections
        }

        fun connectedNeighbors(): List<AbsolutePosition> {
            return connectedNeighbors
        }

        fun toSubGridPositions(): Collection<SubGridPosition> {
            return listOf(
                SubGridPosition(x, y, input),
                SubGridPosition(x + 1, y, input),
                SubGridPosition(x + 1, y + 1, input),
                SubGridPosition(x, y + 1, input),
            )
        }

        fun isEdgeNode(): Boolean {
            return x <= 0 || y <= 0 || x >= input[0].length - 1 || y >= input.size - 1
        }
    }

    private class SubGridPosition(x: Int, y: Int, val input: Array<String>) : Position(x, y) {
        /**
         * i.e. bottom right square
         */
        fun toAbsolutePosition(): AbsolutePosition {
            if (x < 0 || y < 0 || x >= input[0].length || y >= input.size) {
                throw IllegalArgumentException("Invalid subgrid position ($x,$y)")
            }
            return AbsolutePosition(x, y, input)
        }

        fun toAbsolutePositionOrNull(): AbsolutePosition? {
            return try {
                toAbsolutePosition()
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        fun getAdjacentAbsolutePositions(): List<AbsolutePosition> {
            return listOf(
                AbsolutePosition(x, y, input),
                AbsolutePosition(x - 1, y, input),
                AbsolutePosition(x - 1, y - 1, input),
                AbsolutePosition(x, y - 1, input),
            ).filter { it.y >= 0 && it.x >= 0 && it.y < input.size && it.x < input[0].length }
        }

        fun isEdgeNode(): Boolean {
            return x <= 0 || y <= 0 || x >= input[0].length || y >= input.size
        }

        operator fun plus(other: DifferentialPosition): SubGridPosition {
            return SubGridPosition(x + other.x, y + other.y, input)
        }

        companion object {
            /** top right corner */
            fun fromAbsolutePosition(position: AbsolutePosition): SubGridPosition {
                return SubGridPosition(position.x, position.y, position.input)
            }
        }
    }

    private data class FloodFill(
        val distances: Map<AbsolutePosition, Int>,
        val entrances: Map<AbsolutePosition, Set<AbsolutePosition>>,
        val visited: Int
    )

    private fun floodFill(input: Array<String>, startPosition: AbsolutePosition): FloodFill {
        val visited = mutableSetOf<AbsolutePosition>()
        val toVisit = mutableListOf(startPosition)
        val distanceMap = mutableMapOf<AbsolutePosition, Int>()
        val enteredFrom = mutableMapOf<AbsolutePosition, MutableSet<AbsolutePosition>>()
        distanceMap[startPosition] = 0
        while (toVisit.isNotEmpty()) {
            val current = toVisit.removeFirst()
            val currentDistance = distanceMap[current]!!

            for (neighbor in current.connectedNeighbors()) {
                distanceMap.compute(neighbor) { _, distance ->
                    if (distance == null || distance > currentDistance + 1) {
                        currentDistance + 1
                    } else {
                        distance
                    }
                }
                enteredFrom.compute(neighbor) { _, list ->
                    if (list == null) {
                        mutableSetOf(current)
                    } else {
                        list.add(current)
                        list
                    }
                }

                if (neighbor !in visited) {
                    toVisit.add(neighbor)
                }
            }
            visited.add(current)
        }

        return FloodFill(
            distanceMap,
            enteredFrom,
            visited.size
        )
    }

    private data class Enclosed(
        val enclosed: Set<AbsolutePosition>,
        val unenclosed: Set<AbsolutePosition>,
    )

    private data class BreakCheck(
        val c1: AbsolutePosition?,
        val d1: DifferentialPosition,
        val c2: AbsolutePosition?,
        val d2: DifferentialPosition,
        val d: DifferentialPosition,
    )

    private fun enclosedNodes(input: Array<String>, pathNodes: List<AbsolutePosition>): Enclosed {
        val startEnclosedNodes = System.currentTimeMillis()
        val allPositions = input.withIndex().flatMap { (y, line) ->
            line.withIndex().map { (x, _) ->
                AbsolutePosition(x, y, input)
            }
        }.toSet()
        val pathNodesSet = pathNodes.toSet()
        val subgridPositions = buildList<SubGridPosition> {
            for (x in 0..input[0].length) {
                add(SubGridPosition(x, 0, input))
                add(SubGridPosition(x, input.size, input))
            }
            for (y in 0..input.size) {
                add(SubGridPosition(0, y, input))
                add(SubGridPosition(input[0].length, y, input))
            }
        }
        val knownUnenclosed = mutableSetOf<SubGridPosition>()
        val startSubgridIteration = System.currentTimeMillis()
        var countInterestingIterations = 0
        var numberOfInterestingVisits = 0
        var numberOfBreakChecks = 0
        subgridPositions.forEach { subgridPosition ->
            if (subgridPosition in knownUnenclosed) {
                return@forEach
            }
            countInterestingIterations += 1
            val visited = mutableSetOf<SubGridPosition>()
            val toVisit = mutableSetOf(subgridPosition)
            while (toVisit.isNotEmpty()) {
                val current = toVisit.random()
                toVisit.remove(current)

                if (current in knownUnenclosed) {
                    visited.add(current)
                    break
                }

                numberOfInterestingVisits += 1
                val bottomRight = current.toAbsolutePositionOrNull()
                val topRight = (current + UP).toAbsolutePositionOrNull()
                val bottomLeft = (current + LEFT).toAbsolutePositionOrNull()
                val topLeft = (current + UP + LEFT).toAbsolutePositionOrNull()
                listOf(
                    BreakCheck(bottomLeft, RIGHT, bottomRight, LEFT, DOWN),
                    BreakCheck(topLeft, RIGHT, topRight, LEFT, UP),
                    BreakCheck(topRight, DOWN, bottomRight, UP, RIGHT),
                    BreakCheck(topLeft, DOWN, bottomLeft, UP, LEFT),
                ).forEach { breakCheck ->
                    val squeeze = current + breakCheck.d
                    if (squeeze in visited || squeeze.isEdgeNode()) {
                        return@forEach
                    }
                    val c1InPathNodes = breakCheck.c1 in pathNodesSet
                    val c2InPathNodes = breakCheck.c2 in pathNodesSet
                    if (breakCheck.c1 != null && breakCheck.c2 != null && c1InPathNodes && c2InPathNodes) {
                        numberOfBreakChecks += 1
                        if (!breakCheck.c1.potentialNeighborDirections()
                                .contains(breakCheck.d1) && !breakCheck.c2.potentialNeighborDirections()
                                .contains(breakCheck.d2)
                        ) {
                            toVisit.add(squeeze)
                        }
                    }
                    if (!(c1InPathNodes && c2InPathNodes)) {
                        toVisit.add(squeeze)
                    }
                }
                visited.add(current)
            }
            knownUnenclosed.addAll(visited)
        }

        val (enclosed, unenclosed) = allPositions.partition { SubGridPosition.fromAbsolutePosition(it) !in knownUnenclosed }
        return Enclosed(
            enclosed = enclosed.filter { it !in pathNodesSet }.toSet(),
            unenclosed = unenclosed.toSet()
        )
    }

    override fun solvePart1(input: Array<String>): Any? {
        val startPos = input.withIndex().map { (sy, it) ->
            val sx = it.indexOf('S')
            if (sx != -1) {
                AbsolutePosition(sx, sy, input)
            } else {
                null
            }
        }.firstNotNullOf { it }
        val fill = floodFill(input, startPos)
        val pathNodes = fill.distances.filterKeys { fill.entrances[it]!!.size == 2 }
        return pathNodes.values.maxOrNull()
    }

    override fun solvePart2(input: Array<String>): Any? {
        /*
        val startPos = input.withIndex().map { (sy, it) ->
            val sx = it.indexOf('S')
            if (sx != -1) {
                AbsolutePosition(sx, sy, input)
            } else {
                null
            }
        }.firstNotNullOf { it }
        val fill = floodFill(input, startPos)
        val pathNodes = fill.distances.filterKeys { fill.entrances[it]!!.size == 2 }
        return enclosedNodes(input, pathNodes.keys.toList()).enclosed.size
         */
        return shoelace(input)
    }

    private fun shoelace(input: Array<String>): Int {
        val startPos = input.asSequence().withIndex().map { (sy, it) ->
            val sx = it.indexOf('S')
            if (sx != -1) {
                AbsolutePosition(sx, sy, input)
            } else {
                null
            }
        }.firstNotNullOf { it }
        val path = buildList<AbsolutePosition> {
            add(startPos)
            var next = startPos.connectedNeighbors[0]
            while (next != startPos) {
                add(next)
                next = next.connectedNeighbors.filter { asReversed()[1] != it }[0]
            }
        }
        // Shoelace Area (Triangle)
        val area = path.zipWithNext().sumOf { (a, b) ->
            (b.x - a.x) * (b.y + a.y)
        }
        return (area.absoluteValue - path.size) / 2 + 1 // Solve Pick's Theorem for /i/
    }

    private fun prettyPrint(
        input: Array<String>,
        path: Collection<AbsolutePosition> = emptyList(),
        part1Solution: AbsolutePosition? = null,
        part2Solution: Set<AbsolutePosition> = emptySet(),
    ) {
        // box drawing characters
        val replacements = mapOf(
            'S' to "S",
            '7' to "┐",
            'F' to "┌",
            'J' to "┘",
            'L' to "└",
            '|' to "│",
            '-' to "─",
            '.' to " ",
        )
        input.forEachIndexed { lineNum, line ->
            println(
                buildString {
                    append(
                        replacements.entries.fold(line) { acc, entry -> acc.replace(entry.key.toString(), entry.value) }
                            .mapIndexed { charNum, char ->
                                val position = AbsolutePosition(charNum, lineNum, input)
                                when {
                                    char == 'S' -> "\u001b[1;31mS\u001b[0m"
                                    position == part1Solution -> "\u001b[1;31m$char\u001b[0m"
                                    position in part2Solution -> "\u001b[41m$char\u001b[0m"
                                    position in path -> "\u001b[1;33m$char\u001b[0m"
                                    else -> char.toString()
                                }
                            }.joinToString("")
                    )
                    val poi = mutableListOf<String>()
                    if (line.contains('S')) {
                        poi.add("has start")
                    }
                    if (part1Solution?.y == lineNum) {
                        poi.add("part 1 solution")
                    }
                    if (part2Solution.any { it.y == lineNum }) {
                        poi.add("part 2 (${part2Solution.count { it.y == lineNum }} enclosed)")
                    }
                    if (poi.isNotEmpty()) {
                        append("  ")
                        append(poi.joinToString(", "))
                    }
                }
            )
        }
    }
}

