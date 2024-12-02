package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import dev.mtib.aoc.util.AocLogger

object Day23 : AbstractDay(23) {
    private val slopeChars = setOf('^', '>', 'v', '<')
    private val spaceChar = '.'
    private val wallChar = '#'
    private val logger = AocLogger.new {}

    private data class Point(val x: Int, val y: Int) {
        companion object {
            val cardinalDirections = listOf(
                Point(0, -1),
                Point(1, 0),
                Point(0, 1),
                Point(-1, 0)
            )
        }

        fun forward(direction: Char): Point {
            return when (direction) {
                '^' -> Point(x, y - 1)
                '>' -> Point(x + 1, y)
                'v' -> Point(x, y + 1)
                '<' -> Point(x - 1, y)
                '.' -> this
                else -> throw IllegalArgumentException("Unknown direction $direction")
            }
        }

        fun forward(input: Array<String>): Point {
            return forward(charFrom(input))
        }

        fun backwards(direction: Char): Point {
            return when (direction) {
                '^' -> Point(x, y + 1)
                '>' -> Point(x - 1, y)
                'v' -> Point(x, y - 1)
                '<' -> Point(x + 1, y)
                '.' -> this
                else -> throw IllegalArgumentException("Unknown direction $direction")
            }
        }

        fun backwards(input: Array<String>): Point {
            return backwards(charFrom(input))
        }

        fun charFrom(input: Array<String>): Char {
            return input[y][x]
        }

        operator fun plus(other: Point): Point {
            return Point(x + other.x, y + other.y)
        }
    }

    private data class Area(val points: Set<Point>) {
        val entrySlopes = mutableMapOf<Point, Area>()
        val exitSlopes = mutableMapOf<Point, Area>()

        companion object {
            val stepsBetweenCache = mutableMapOf<Pair<Point, Point>, Int>()
            val pathLengthCache = mutableMapOf<Pair<Point, Point>, List<Point>>()
        }

        fun getInnerPath(start: Point, end: Point): List<Point> {
            if ((start to end) in stepsBetweenCache) {
                return pathLengthCache[start to end]!!
            }
            if ((end to start) in stepsBetweenCache) {
                return pathLengthCache[end to start]!!.reversed()
            }
            data class State(val path: List<Point>)

            val toVisit = mutableListOf<State>(State(listOf(start)))
            while (toVisit.isNotEmpty()) {
                val current = toVisit.removeFirst()!!
                if (current.path.last() == end) {
                    pathLengthCache[start to end] = current.path
                    return current.path
                }
                for (direction in Point.cardinalDirections) {
                    val next = current.path.last() + direction
                    if (next in points && next !in current.path) {
                        toVisit.add(State(current.path + next))
                    }
                }
            }
            throw IllegalStateException("No path found")
        }
    }

    private fun floodFill(input: Array<String>, start: Point): Area {
        val area = mutableSetOf<Point>()
        val toVisit = mutableListOf(start)
        while (toVisit.isNotEmpty()) {
            val current = toVisit.removeFirst()
            if (current in area) continue
            if (current.charFrom(input) != spaceChar) continue
            area.add(current)
            for (direction in Point.cardinalDirections) {
                val next = current + direction
                if (next.x < 0 || next.x >= input[0].length) continue
                if (next.y < 0 || next.y >= input.size) continue
                toVisit.add(next)
            }
        }
        return Area(area)
    }

    private data class AreaGraph(val areas: Set<Area>, val entry: Area, val exit: Area)

    private fun printInputWithPoints(input: Array<String>, points: Collection<Point>) {
        logger.log {
            buildString {
                input.withIndex().forEach { (y, line) ->
                    line.withIndex().forEach { (x, char) ->
                        val point = Point(x, y)
                        if (point in points) {
                            append("\u001b[31mO\u001b[0m")
                        } else {
                            append(char)
                        }
                    }
                    appendLine()
                }
            }
        }
    }

    private fun buildAreaGraph(input: Array<String>, bidirectional: Boolean = false): AreaGraph {
        val slopePositions = input.withIndex().flatMap { (y, line) ->
            line.withIndex().filter { (_, char) -> char in slopeChars }.map { (x, _) -> Point(x, y) }
        }
        val areas = mutableSetOf<Area>()
        for (slopePosition in slopePositions) {
            val fromPoint = slopePosition.backwards(slopePosition.charFrom(input))
            val toPoint = slopePosition.forward(slopePosition.charFrom(input))
            var fromArea = areas.find { fromPoint in it.points }
            var toArea = areas.find { toPoint in it.points }

            if (fromArea == null) {
                fromArea = floodFill(input, fromPoint)
            }
            if (toArea == null) {
                toArea = floodFill(input, toPoint)
            }

            fromArea.exitSlopes[slopePosition] = toArea
            toArea.entrySlopes[slopePosition] = fromArea

            if (bidirectional) {
                fromArea.entrySlopes[slopePosition] = toArea
                toArea.exitSlopes[slopePosition] = fromArea
            }

            areas.add(fromArea)
            areas.add(toArea)
        }
        return AreaGraph(
            areas,
            areas.first { it.points.any { point -> point.y == 0 } },
            areas.last { it.points.any { point -> point.y == input.size - 1 } })
    }

    private fun tryGraphCombinationsForLongestPathLength(input: Array<String>): Int {
        val graph = buildAreaGraph(input)

        logger.log {
            buildString {
                appendLine("graph TD")
                for (area in graph.areas) {
                    appendLine("  ${area.hashCode()}[Area ${area.hashCode()}]")
                    for ((_, target) in area.exitSlopes) {
                        appendLine("  ${area.hashCode()} --> ${target.hashCode()}")
                    }
                }
            }
        }

        data class State(val points: List<Point>, val currentArea: Area)

        val startPoint = graph.entry.points.first { it.y == 0 }
        val endPoint = graph.exit.points.first { it.y == input.size - 1 }

        val toVisit = mutableListOf(State(listOf(startPoint), graph.entry))
        return sequence<State> {
            while (toVisit.isNotEmpty()) {
                val current = toVisit.removeFirst()!!
                val choices = current.currentArea.exitSlopes.keys
                val lastPoint = current.points.last()
                if (current.currentArea == graph.exit) {
                    val pathToExit = current.currentArea.getInnerPath(lastPoint, endPoint)
                    yield(
                        State(
                            (current.points + pathToExit.subList(1, pathToExit.size)).distinct(),
                            graph.exit,
                        )
                    )
                }
                for (choice in choices) {
                    val nextArea = current.currentArea.exitSlopes[choice]!!
                    val pathToNextArea = current.currentArea.getInnerPath(lastPoint, choice.backwards(input))
                    logger.log {
                        if (pathToNextArea.filter { it in current.points }.size > 1) {
                            printInputWithPoints(input, current.points)
                        }
                        require(pathToNextArea.filter { it in current.points }.size <= 1) { "Loop detected, $pathToNextArea\n" }
                        "no loop detected"
                    }
                    toVisit.add(
                        State(
                            current.points + pathToNextArea.subList(
                                1,
                                pathToNextArea.size
                            ) + choice + choice.forward(input),
                            nextArea,
                        )
                    )
                }
            }
        }.maxBy { it.points.size }.also { maxState ->
            printInputWithPoints(input, maxState.points)
        }.let { it.points.size } - 1
    }

    private fun isExitStillReachable(visitedAreas: List<Area>, graph: AreaGraph): Boolean {
        val toVisit = mutableListOf(visitedAreas)
        while (toVisit.isNotEmpty()) {
            val extension = toVisit.removeFirst()!!
            val currentArea = extension.last()
            if (currentArea == graph.exit) {
                return true
            }
            for (choice in currentArea.exitSlopes.keys) {
                val nextArea = currentArea.exitSlopes[choice]!!
                if (nextArea !in extension) {
                    toVisit.add(extension + nextArea)
                }
            }
        }
        return false
    }

    private fun tryGraphCombinationsForLongestPathLengthPt2(input: Array<String>): Int {
        val graph = buildAreaGraph(input, bidirectional = true)

        data class State(val lastPoint: Point, val visitedAreas: List<Area>, val currentArea: Area)

        val startPoint = graph.entry.points.first { it.y == 0 }
        val endPoint = graph.exit.points.first { it.y == input.size - 1 }

        val dontHaveBoth = run {
            val beforeEnd = graph.exit.exitSlopes.values.first()
            graph.areas.filter { it != graph.exit && beforeEnd in it.exitSlopes.values }
        }

        var lastPrinted: Int? = null
        fun printCurrentBest(length: Int) {
            if (lastPrinted != length) {
                logger.log { "Current best: $length steps" }
            }
            lastPrinted = length
        }

        val toVisit = mutableListOf(State(startPoint, emptyList(), graph.entry))
        return sequence<State> {
            while (toVisit.isNotEmpty()) {
                val current = toVisit.removeFirst()!!
                if (dontHaveBoth.all { it in current.visitedAreas }) {
                    continue
                }
                val choices = current.currentArea.exitSlopes.filter { it.value !in current.visitedAreas }.keys
                if (current.currentArea == graph.exit) {
                    yield(
                        State(
                            endPoint,
                            current.visitedAreas + current.currentArea,
                            graph.exit,
                        )
                    )
                } else {
                    for (choice in choices) {
                        val nextArea = current.currentArea.exitSlopes[choice]!!
                        val downhill = choice.backwards(input) in current.currentArea.points
                        toVisit.add(
                            State(
                                if (downhill) choice.forward(input) else choice.backwards(input),
                                current.visitedAreas + current.currentArea,
                                nextArea,
                            )
                        )
                    }
                }
            }
        }.map {
            val path = mutableListOf(startPoint)
            var currentArea = it.visitedAreas[0]
            for (nextArea in it.visitedAreas.subList(1, it.visitedAreas.size)) {
                val exitSlope = currentArea.exitSlopes.entries.first { it.value == nextArea }.key
                val exitSlopeSides = listOf(exitSlope.backwards(input), exitSlope.forward(input))

                val currentAreaLast = exitSlopeSides.first { it in currentArea.points }
                val nextAreaFirst = exitSlopeSides.first { it in nextArea.points }

                val pathToNextAreaEntrance = currentArea.getInnerPath(path.last(), currentAreaLast)

                path.addAll(pathToNextAreaEntrance.subList(1, pathToNextAreaEntrance.size))
                path.add(exitSlope)
                path.add(nextAreaFirst)

                currentArea = nextArea
            }
            val pathToExit = currentArea.getInnerPath(path.last(), endPoint)
            path.addAll(pathToExit.subList(1, pathToExit.size))
            path
        }.reduce { acc, next ->
            (if (acc.size > next.size) acc else next).also {
                printCurrentBest(it.size - 1)
            }
        }.let { it.size - 1 }
    }

    override fun solvePart1(input: Array<String>): Any? {
        // return tryGraphCombinationsForLongestPathLength(input)
        return null
    }

    override fun solvePart2(input: Array<String>): Any? {
        // return tryGraphCombinationsForLongestPathLengthPt2(input)
        return null
    }
}
