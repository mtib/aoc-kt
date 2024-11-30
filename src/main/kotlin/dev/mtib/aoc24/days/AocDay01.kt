package dev.mtib.aoc24.days

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

object AocDay01 : AocDay(1) {
    override suspend fun part1(): String {
        return input.length.toString()
    }

    override suspend fun part2(): String {
        delay(10.milliseconds)
        return "DEADBEEF"
    }
}
