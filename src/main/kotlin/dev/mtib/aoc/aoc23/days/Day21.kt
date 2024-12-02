package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay

class Day21 : AbstractDay(21) {

    override fun solvePart1(input: Array<String>): Any? {
        val start = input.withIndex().find { 'S' in it.value }!!.let { Pair(it.value.indexOf('S'), it.index) }
        var afterNSteps = mutableSetOf(start)
        for (i in 1..64) {
            val beforeNSteps = afterNSteps.toMutableSet()
            afterNSteps.clear()
            for (pos in beforeNSteps) {
                val (x, y) = pos

                listOf(
                    Pair(x + 1, y),
                    Pair(x - 1, y),
                    Pair(x, y + 1),
                    Pair(x, y - 1),
                ).filter { (x, y) ->
                    !(x < 0 || y < 0 || x >= input[0].length || y >= input.size || input[y][x] == '#')
                }.forEach {
                    afterNSteps.add(it)
                }
            }
        }
        return afterNSteps.size
    }

    override fun solvePart2(input: Array<String>): Any? {

        val start = input.withIndex().find { 'S' in it.value }!!.let { Pair(it.value.indexOf('S'), it.index) }
        val afterNSteps = mutableSetOf(start)

        val rem = 26501365.mod(input.size)
        val crossesTile = input.size

        val measure0 = rem
        val measure1 = rem + crossesTile
        val measure2 = rem + 2 * crossesTile

        val map = mutableMapOf<Int, Int>()

        for (i in 1..measure2) {
            val beforeNSteps = afterNSteps.toMutableSet()
            afterNSteps.clear()
            for (pos in beforeNSteps) {
                val (x, y) = pos

                listOf(
                    Pair(x + 1, y),
                    Pair(x - 1, y),
                    Pair(x, y + 1),
                    Pair(x, y - 1),
                ).filter { (x, y) ->
                    input[y.mod(input.size)][x.mod(input[0].length)] != '#'
                }.forEach {
                    afterNSteps.add(it)
                }
            }
            map[i] = afterNSteps.size
        }

        // Make quadratic from points
        val c0 = map[measure0]!!.toLong()
        val c1 = map[measure1]!!.toLong()
        val c2 = map[measure2]!!.toLong()

        val a = (c0 - 2 * c1 + c2) / 2
        val b = (-3 * c0 + 4 * c1 - c2) / 2
        val c = c0
        val n = 26501365L / input.size

        return n * n * a + n * b + c
    }
}
