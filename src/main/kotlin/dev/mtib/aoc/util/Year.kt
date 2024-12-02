package dev.mtib.aoc.util

data class Year(
    val year: Int
) {
    fun Day(day: Int) = Day(year, day)
    fun PuzzleIdentity(day: Int, part: Int) = PuzzleIdentity(year, day, part)
    fun toInt() = year

    constructor(year: String) : this(year.toInt())
}
