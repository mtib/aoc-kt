package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import kotlin.math.abs

object Day21 : AocDay(2024, 21) {
    private val numeric = "789\n456\n123\n 0A".split("\n")
    private val directional = " ^A\n<v>".split("\n")

    private fun walk(keypad: List<String>, x: Int, y: Int, path: String): Sequence<Char> = sequence {
        var x = x
        var y = y
        for (direction in path) {
            val neighbors = listOf(
                x - 1 to y, x + 1 to y, x to y - 1, x to y + 1
            )
            val (newX, newY) = neighbors["<>^v".indexOf(direction)]
            x = newX
            y = newY
            yield(keypad[y][x])
        }
    }

    private fun pathsBetween(keypad: List<String>, start: Char, end: Char): Set<String> {
        val (x1, y1) = keypad.flatMapIndexed { y, row ->
            row.mapIndexed { x, key -> x to y to key }
        }.first { (_, key) -> key == start }.let { (pos, _) -> pos }
        val (x2, y2) = keypad.flatMapIndexed { y, row ->
            row.mapIndexed { x, key -> x to y to key }
        }.first { (_, key) -> key == end }.let { (pos, _) -> pos }
        val hor = "<>"[if (x2 > x1) 1 else 0].toString().repeat(abs(x2 - x1))
        val ver = "^v"[if (y2 > y1) 1 else 0].toString().repeat(abs(y2 - y1))
        return setOf(hor + ver, ver + hor).filter { ' ' !in walk(keypad, x1, y1, it).toList() }.map { it + "A" }.toSet()
    }

    private val cache = mutableMapOf<String, Long>()

    private fun costBetween(keypad: List<String>, start: Char, end: Char, links: Int): Long {
        val key = "costBetween_$keypad$start$end$links"
        if (key in cache) {
            return cache[key]!!
        }
        return run {
            if (links > 0) {
                pathsBetween(keypad, start, end).map { path ->
                    cost(directional, path, links - 1)
                }.minOrNull()!!
            } else {
                1
            }
        }.also {
            cache[key] = it
        }
    }

    private fun cost(keypad: List<String>, keys: String, links: Int): Long {
        val key = "cost_$keypad$keys$links"
        if (key in cache) {
            return cache[key]!!
        }
        return "A$keys".windowed(2).sumOf { string ->
            val (a, b) = string.toCharArray()
            costBetween(keypad, a, b, links)
        }.also {
            cache[key] = it
        }
    }

    private fun complexity(code: String, robots: Int): Long {
        return cost(numeric, code, robots + 1) * code.dropLast(1).toLong()
    }

    override suspend fun part1(): Any {
        return inputLinesList.sumOf { complexity(it, 2) }
    }

    override suspend fun part2(): Any {
        return inputLinesList.sumOf { complexity(it, 25) }
    }

    override suspend fun teardown() {
        super.teardown()
        cache.clear()
    }
}