package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import kotlin.math.pow
import kotlin.math.sqrt

object Day6 : AbstractDay(6) {
    private data class Race(val duration: Long, val record: Long) {
        enum class Side {
            LEFT, RIGHT
        }

        fun waysToBeat(): Long {
            // it * (duration - it) > record
            // -1 it**2 + duration * it - record = 0
            val a = -1.0
            val b = duration.toDouble()
            val c = -record.toDouble()
            fun zero(a: Double, b: Double, c: Double, side: Side): Double {
                val delta = b.pow(2) - 4 * a * c
                when (side) {
                    Side.RIGHT -> return (-b - sqrt(delta)) / (2 * a)
                    Side.LEFT -> return (-b + sqrt(delta)) / (2 * a)
                }
            }

            fun coercedZero(a: Double, b: Double, c: Double, side: Side): Long {
                val zero = zero(a, b, c, side)
                return when (side) {
                    Side.LEFT -> (zero + 1).toLong().coerceAtLeast(0)
                    Side.RIGHT -> (zero).toLong().coerceAtMost(duration)
                }
            }

            val left = coercedZero(a, b, c, Side.LEFT)
            val right = coercedZero(a, b, c, Side.RIGHT)
            return right - left + 1
        }
    }

    override fun solvePart1(input: Array<String>): String? {
        val nums = input.map { it.split(Regex("""\s+""")).drop(1).map { it.toLong() } }
        val races = nums[0].zip(nums[1]).map { (time, record) -> Race(time, record) }
        return races.fold(1L) { acc, race -> acc * race.waysToBeat() }.toString()
    }

    override fun solvePart2(input: Array<String>): String? {
        val ints = input.map { it.split(Regex("""\s+""")).drop(1).joinToString("").toLong() }
        val race = Race(ints[0], ints[1])
        return race.waysToBeat().toString()
    }
}
