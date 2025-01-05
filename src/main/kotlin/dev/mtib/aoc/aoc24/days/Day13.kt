package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.AocLogger.Companion.logger
import org.ojalgo.concurrent.Parallelism
import org.ojalgo.optimisation.ExpressionsBasedModel
import org.ojalgo.optimisation.Optimisation
import org.ojalgo.optimisation.Optimisation.Options
import org.ojalgo.optimisation.integer.IntegerStrategy
import org.ojalgo.type.context.NumberContext
import java.math.BigInteger
import java.math.RoundingMode


object Day13: AocDay(2024, 13) {
    private class ClawMachine(
        private val target: Pair<BigInteger, BigInteger>,
        private val aMovement: Pair<BigInteger, BigInteger>,
        private val bMovement: Pair<BigInteger, BigInteger>

    ) {
        companion object {
            private val part2Offset = BigInteger("10000000000000")
        }
        fun adjusted(): ClawMachine = ClawMachine(
            target = Pair(target.first + part2Offset, target.second + part2Offset),
            aMovement = aMovement,
            bMovement = bMovement
        )
        private fun findIntersection(
            aX: BigInteger, aY: BigInteger, bX: BigInteger, bY: BigInteger, xPrize: BigInteger, yPrize: BigInteger
        ): Pair<BigInteger, BigInteger>? {
            val aXWithBY = aX * bY
            val xPrizeWithBY = xPrize * bY

            val aYWithBX = aY * bX
            val yPrizeWithBX = yPrize * bX

            val (a, aRem) = (xPrizeWithBY - yPrizeWithBX).divideAndRemainder(aXWithBY - aYWithBX)
            val (b, bRem) = (yPrize - aY * a).divideAndRemainder(bY)

            if (aRem.compareTo(BigInteger.ZERO) != 0 || bRem.compareTo(BigInteger.ZERO) != 0) {
                return null
            }

            return Pair(a, b)
        }


        fun solve(): BigInteger? {
            val (aPress, bPress) = findIntersection(
                aMovement.first, aMovement.second, bMovement.first, bMovement.second, target.first, target.second
            ) ?: return null
            return aPress.multiply(BigInteger("3")) + bPress
        }
    }

    private fun getClawMachines(): Sequence<ClawMachine> = sequence {
        var aBtn: Pair<BigInteger, BigInteger>? = null
        var bBtn: Pair<BigInteger, BigInteger>? = null
        var prize: Pair<BigInteger, BigInteger>? = null
        inputLinesList.forEach {
            when {
                aBtn == null -> {
                    val match = Regex("Button A: X((?:\\+|-)\\d+), Y(?:\\+|-)(\\d+)").find(it)!!
                    val (x, y) = match.destructured
                    aBtn = Pair(x.toBigInteger(), y.toBigInteger())
                }
                bBtn == null -> {
                    val match = Regex("Button B: X((?:\\+|-)\\d+), Y(?:\\+|-)(\\d+)").find(it)!!
                    val (x, y) = match.destructured
                    bBtn = Pair(x.toBigInteger(), y.toBigInteger())
                }
                prize == null -> {
                    val match = Regex("Prize: X=((?:\\+|-|)\\d+), Y=((?:\\+|-|)\\d+)").find(it)!!
                    val (x, y) = match.destructured
                    prize = Pair(x.toBigInteger(), y.toBigInteger())
                }
                it == "" -> {
                    yield(ClawMachine(
                        target = prize!!,
                        aMovement = aBtn!!,
                        bMovement = bBtn!!
                    ))
                    aBtn = null
                    bBtn = null
                    prize = null
                }
                else -> {
                    throw IllegalStateException("Invalid input: $it")
                }
            }
        }
        if (aBtn != null && bBtn != null && prize != null) {
            yield(ClawMachine(
                target = prize!!,
                aMovement = aBtn!!,
                bMovement = bBtn!!
            ))
        } else {
            throw IllegalStateException("Invalid input")
        }
    }

    override suspend fun part1(): BigInteger {
        return getClawMachines().mapNotNull { it.solve() }.fold(BigInteger.ZERO, BigInteger::add)
    }

    override suspend fun part2(): BigInteger {
        return getClawMachines().mapNotNull { it.adjusted().solve() }.fold(BigInteger.ZERO, BigInteger::add)
    }
}