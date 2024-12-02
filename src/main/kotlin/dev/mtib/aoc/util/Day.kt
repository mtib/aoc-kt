package dev.mtib.aoc.util

data class Day(
    val year: Int,
    val day: Int,
) : Comparable<Day> by object : Comparable<Day> {
    override fun compareTo(other: Day): Int {
        val yearComparison = year.compareTo(other.year)
        return if (yearComparison != 0) {
            yearComparison
        } else {
            day.compareTo(other.day)
        }
    }
} {
    fun PuzzleIdentity(part: Int) = PuzzleIdentity(this, part)
    fun toYear() = Year(year)
    fun toInt() = day
}
