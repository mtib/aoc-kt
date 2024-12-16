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
        private fun toModel(): ExpressionsBasedModel {
            val options = Options()
            options.integer(IntegerStrategy.newConfigurable().withParallelism(Parallelism.CORES))
            options.solution = NumberContext.of(200, 200)
            options.feasibility = NumberContext.of(100, 100)
            val model = ExpressionsBasedModel(options)

            val aPress = model.addVariable("aPress").integer().lower(0)
            val bPress = model.addVariable("bPress").integer().lower(0)

            val cost = model.addExpression("cost")
            cost.set(aPress, BigInteger("3"))
            cost.set(bPress, BigInteger("1"))
            cost.weight(1)

            val xMatch = model.addExpression("xMatch").lower(target.first).upper(target.first)
            xMatch.set(aPress, aMovement.first)
            xMatch.set(bPress, bMovement.first)

            val yMatch = model.addExpression("yMatch").lower(target.second).upper(target.second)
            yMatch.set(aPress, aMovement.second)
            yMatch.set(bPress, bMovement.second)

            return model
        }

        fun solve(): BigInteger? {
            val model = toModel()
            val result: Optimisation.Result? = run {
                for (i in 1..(if (partMode == 2) 10 else 1)) {
                    val result = model.minimise()
                    if (result.state.isFeasible) {
                        if (i > 1) {
                            logger.log { "found solution after ${i} iterations" }
                        }
                        if (model.validate(result)) {
                            return@run result
                        }
                    }
                }
                null
            }

            if (result == null) {
                return null
            }

            val aPress = model.variables.find { it.name == "aPress" }!!
            val bPress = model.variables.find { it.name == "bPress" }!!

            return aPress.value.toBigIntegerExact().multiply(BigInteger("3")) + bPress.value.toBigIntegerExact()
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
        not(465116279064, Hint.Direction.TooLow)
        not(57928637781847, Hint.Direction.TooLow)
        not(81122608248687, Hint.Direction.TooHigh)
        not(80657109890803)
        return getClawMachines().mapNotNull { it.adjusted().solve() }.fold(BigInteger.ZERO, BigInteger::add)
    }
}