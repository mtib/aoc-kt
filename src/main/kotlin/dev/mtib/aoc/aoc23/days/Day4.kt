package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import kotlin.math.pow

object Day4 : AbstractDay(4) {

    private data class Card(val number: Int, val winning: Set<Int>, val have: Set<Int>) {
        fun matches(): Int {
            return winning.intersect(have).size
        }

        fun value(): Int {
            return 2.0.pow(matches() - 1.0).toInt()
        }

        companion object {
            private val cardRegex = Regex("""Card\s+(\d+):\s+([^|]*)\s+\|\s+(.*)""")
            fun fromLine(line: String): Card {
                val parts =
                    cardRegex.matchEntire(line)?.groupValues ?: throw IllegalArgumentException("Invalid line: $line")
                val num = parts[1].toInt()
                val winning = parts[2].split("  ", " ").map { it.toInt() }.toSet()
                val have = parts[3].split("  ", " ").map { it.toInt() }.toSet()
                return Card(num, winning, have)
            }
        }
    }

    override fun solvePart1(input: Array<String>): String? {
        return input.map { Card.fromLine(it) }.sumOf { it.value() }.toString()
    }

    override fun solvePart2(input: Array<String>): String? {
        data class CardWithCount(val card: Card, var count: Int) {
            fun value(): Int {
                return card.matches()
            }

            fun addCopies(num: Int) {
                count += num
            }
        }

        val cards = input.map { Card.fromLine(it) }.map {
            CardWithCount(it, 1)
        }
        for ((i, card) in cards.withIndex()) {
            val value = card.value()
            for (j in 1..value) {
                cards[i + j].addCopies(card.count)
            }
        }

        return cards.sumOf { it.count }.toString()
    }
}
