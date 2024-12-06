package dev.mtib.aoc.aoc23.util

import dev.mtib.aoc.day.AocDay

abstract class AbstractDay(day: Int) : AocDay(2023, day) {
    abstract fun solvePart1(input: Array<String>): Any?
    abstract fun solvePart2(input: Array<String>): Any?
    override suspend fun part1(): Any {
        return solvePart1(inputLinesList.toTypedArray()) ?: throw NotImplementedError()
    }

    override suspend fun part2(): Any {
        return solvePart2(inputLinesList.toTypedArray()) ?: throw NotImplementedError()
    }
}
