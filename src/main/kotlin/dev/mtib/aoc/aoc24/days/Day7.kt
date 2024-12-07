package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.chunkedParMap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.log10

object Day7: AocDay(2024, 7) {
    private suspend fun bruteForce(goal: BigInteger, parts: List<BigInteger>, current: BigInteger? = null, withPart2: Boolean = false): Boolean {
        if (current == null) {
            return bruteForce(
                goal = goal,
                parts = parts.subList(1, parts.size),
                current = parts[0],
                withPart2 = withPart2,
            )
        }

        if (current > goal) {
            return false
        }

        if (parts.isEmpty()) {
            return current == goal
        }

        return bruteForce(goal, parts.subList(1, parts.size), current * parts[0], withPart2 = withPart2) ||
                (withPart2 && bruteForce(goal, parts.subList(1, parts.size), current * BigInteger.TEN.pow(log10(parts[0].toDouble()).toInt() + 1) + parts[0], withPart2 = true)) ||
                bruteForce(goal, parts.subList(1, parts.size), current + parts[0], withPart2 = withPart2)
    }

    override suspend fun part1(): Any {
        val matching = inputLinesList.chunkedParMap(inputLinesList.size / cpu) {lines ->
            lines.map {
                val colon = it.indexOf(':')
                val goal = it.substring(0, colon).toBigInteger()
                val parts = it.substring(colon + 2).split(" ").map { it.toBigInteger() }
                goal to parts
            }.filter { bruteForce(it.first, it.second) }
                .map { it.first }
                .fold (BigInteger.ZERO) { acc, i -> acc + i }
        }.fold(BigInteger.ZERO) { acc, i -> acc + i }
        return matching
    }

    override suspend fun part2(): Any {
        val matching = inputLinesList.chunkedParMap(inputLinesList.size / cpu) {lines ->
            lines.map {
                val colon = it.indexOf(':')
                val goal = it.substring(0, colon).toBigInteger()
                val parts = it.substring(colon + 2).split(" ").map { it.toBigInteger() }
                goal to parts
            }.filter { bruteForce(it.first, it.second, withPart2 = true) }
                .map { it.first }
                .fold (BigInteger.ZERO) { acc, i -> acc + i }
        }.fold(BigInteger.ZERO) { acc, i -> acc + i }
        return matching
    }
}