package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay
import java.math.BigInteger

class Day24 : AbstractDay(24) {
    data class Vec3(val x: BigInteger, val y: BigInteger, val z: BigInteger)
    data class Hailstone(val pos: Vec3, val vel: Vec3) {
        companion object {
            fun fromLine(s: String): Hailstone {
                val (pos, vel) = s.split("@")
                return Hailstone(
                    run {
                        val (x, y, z) = pos.split(",").map { it.trim().toBigInteger() }
                        Vec3(x, y, z)
                    },
                    run {
                        val (x, y, z) = vel.split(",").map { it.trim().toBigInteger() }
                        Vec3(x, y, z)
                    }
                )
            }
        }

        fun plusTime(ns: BigInteger): Hailstone {
            return Hailstone(
                atTime(ns),
                vel
            )
        }

        fun atTime(ns: BigInteger): Vec3 {
            return Vec3(pos.x + vel.x * ns, pos.y + vel.y * ns, pos.z + vel.z * ns)
        }

        fun intersection(other: Hailstone): Vec3? {
            runCatching {
                val x1 = pos.x
                val x2 = pos.x + vel.x
                val x3 = other.pos.x
                val x4 = other.pos.x + other.vel.x
                val y1 = pos.y
                val y2 = pos.y + vel.y
                val y3 = other.pos.y
                val y4 = other.pos.y + other.vel.y

                val t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) /
                    ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4))
                val u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) /
                    ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4))

                val x = pos.x + t * vel.x
                val y = pos.y + t * vel.y

                if (t >= 0.toBigInteger() && u >= 0.toBigInteger()) {
                    return Vec3(x, y, 0.toBigInteger())
                }
            }
            return null
        }
    }

    val testAreaStart = "200000000000000".toBigInteger()
    val testAreaEnd = "400000000000000".toBigInteger()
    override fun solvePart1(input: Array<String>): Any? {
        val stones = input.map { Hailstone.fromLine(it) }
        val intersections = sequence {
            stones.withIndex().forEach { (i, stone) ->
                stones.subList(i + 1, stones.size).forEach { other ->
                    yield(stone.intersection(other))
                }
            }
        }
        return intersections.filterNotNull()
            .filter { it.x in testAreaStart..testAreaEnd && it.y in testAreaStart..testAreaEnd }.count()
    }

    override fun solvePart2(input: Array<String>): Any? {
        return null // Sympy: just throwing the equations at it.
    }
}
