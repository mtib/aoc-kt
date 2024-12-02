package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay

object Day8 : AbstractDay(8) {
    override fun solvePart1(input: Array<String>): String {
        data class Directions(val left: String, val right: String)

        val steps = input[0].toCharArray()
        val nodes = buildMap {
            val nodeSpecs = input.slice(2..<input.size)
            for (nodeSpec in nodeSpecs) {
                val nodeName = nodeSpec.slice(0..2)
                val nodeLeft = nodeSpec.slice(7..9)
                val nodeRight = nodeSpec.slice(12..14)
                put(nodeName, Directions(nodeLeft, nodeRight))
            }
        }

        var currentNode = "AAA"
        var stepsTaken = 0
        while (currentNode != "ZZZ") {
            val directions = nodes[currentNode]!!
            currentNode = when (steps[stepsTaken % steps.size]) {
                'L' -> directions.left
                'R' -> directions.right
                else -> throw IllegalArgumentException("Invalid direction ${steps[stepsTaken % steps.size]}")
            }
            stepsTaken += 1
        }
        return stepsTaken.toString()
    }

    fun lcm(a: Long, b: Long): Long {
        var s = 0L
        var r = b
        var old_s = 1L
        var old_r = a
        var temp: Long
        var bezout_t = 0L

        while (r != 0L) {
            val quotient = old_r / r;
            temp = r;
            r = old_r - quotient * r;
            old_r = temp;
            temp = s;
            s = old_s - quotient * s;
            old_s = temp;
        }

        return (a * b) / old_r
    }

    override fun solvePart2(input: Array<String>): String {
        data class Directions(val left: String, val right: String)

        val steps = input[0].toCharArray()
        val nodes = buildMap {
            val nodeSpecs = input.slice(2..<input.size)
            for (nodeSpec in nodeSpecs) {
                val nodeName = nodeSpec.slice(0..2)
                val nodeLeft = nodeSpec.slice(7..9)
                val nodeRight = nodeSpec.slice(12..14)
                put(nodeName, Directions(nodeLeft, nodeRight))
            }
        }

        var currentNodes = nodes.keys.filter { it[2] == 'A' }
        val cycleLength = currentNodes.map {
            var currentNode = it
            var stepsTaken = 0
            while (currentNode[2] != 'Z') {
                val directions = nodes[currentNode]!!
                currentNode = when (steps[stepsTaken % steps.size]) {
                    'L' -> directions.left
                    'R' -> directions.right
                    else -> throw IllegalArgumentException("Invalid direction ${steps[stepsTaken % steps.size]}")
                }
                stepsTaken += 1
            }
            stepsTaken
        }.fold(1L) { acc, i -> lcm(acc.toLong(), i.toLong()) }

        return cycleLength.toString()
    }
}
