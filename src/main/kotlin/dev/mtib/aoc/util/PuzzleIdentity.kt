package dev.mtib.aoc.util

data class PuzzleIdentity(
    val year: Int,
    val day: Int,
    val part: Int,
) {
    fun toDay() = Day(year, day)
    fun toYear() = Year(year)

    constructor(day: Day, part: Int) : this(day.year, day.day, part)
}
