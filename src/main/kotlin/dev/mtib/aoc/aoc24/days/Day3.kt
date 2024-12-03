package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.chunkedParMap

object Day3 : AocDay(2024, 3) {
    private val re = Regex(""".*?mul\((\d+),(\d+)\)""", setOf(RegexOption.DOT_MATCHES_ALL))
    override suspend fun part1(): Long {
        return inputLinesList.chunkedParMap(1) {
            it.sumOf {
                var i = 0
                var total: Long = 0
                while (true) {
                    val match = re.matchAt(it, i) ?: break

                    match.groupValues.let { (full, a, b) ->
                        total += a.toInt() * b.toInt()
                        i += full.length
                    }
                }
                total
            }
        }.sum()
    }

    private val re2 = Regex("""don't\(\).*?(do\(\)|$)""", setOf(RegexOption.DOT_MATCHES_ALL))
    override suspend fun part2(): Long {
        return input.replace(re2, "/").lines().chunkedParMap(1) {
            var i = 0
            var total: Long = 0
            while (true) {
                val match = re.matchAt(it[0], i) ?: break

                match.groupValues.let { (full, a, b) ->
                    total += a.toInt() * b.toInt()
                    i += full.length
                }
            }
            total
        }.sum()
    }
}
