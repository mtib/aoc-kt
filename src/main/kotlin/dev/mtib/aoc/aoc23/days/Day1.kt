package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.day.AocDay

object Day1 : AocDay(2023, 1) {

    override suspend fun part1(): String {
        return inputLinesArray.filter { it.concatToString().isNotBlank() }.sumOf {
            it.find { it.isDigit() }!!.digitToInt() * 10 + it.findLast { it.isDigit() }!!.digitToInt()
        }.toString()
    }

    override suspend fun part2(): String {
        val spelledDigits = listOf(
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine"
        )

        val regexp = Regex("(${spelledDigits.joinToString("|")}|[0-9])")
        val reversedRegexp = Regex("(${spelledDigits.joinToString("|") { it.reversed() }}|[0-9])")
        fun digitToValue(digit: String): Int {
            return when {
                digit in spelledDigits -> spelledDigits.indexOf(digit) + 1
                digit.length == 1 -> digit[0].digitToInt()
                else -> throw IllegalArgumentException("Invalid digit $digit")
            }
        }

        return inputLinesArray.sumOf {
            digitToValue(regexp.find(it.concatToString())!!.groupValues[1]) * 10 + digitToValue(
                reversedRegexp.find(
                    it.concatToString().reversed()
                )!!.groupValues[1].reversed()
            )
        }
            .toString()
    }
}
