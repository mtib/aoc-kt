package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.chunkedParMap
import java.util.concurrent.ConcurrentHashMap

object Day11: AocDay(2024, 11) {
    class Bag private constructor(private val bag: MutableMap<Long, Long>) {
        constructor(numbers: Iterable<Long>) : this(Unit.run {
            val bag = mutableMapOf<Long, Long>()
            numbers.forEach {
                bag[it] = bag.getOrDefault(it, 0) + 1
            }
            bag
        })

        private val splitMap = ConcurrentHashMap<Long, List<Long>>().also {
            it[0] = listOf(1L)
        }

        suspend fun step(): Bag {
            val updates = bag.entries.chunkedParMap(100) {
                it.flatMap {current ->
                    if (splitMap[current.key] != null) {
                        return@flatMap splitMap[current.key]!!.map { it to current.value }
                    }

                    // case of 1 handled by initialising map

                    val string = current.key.toString()
                    if (string.length % 2 == 0) {
                        val first = string.substring(0, string.length / 2).toLong()
                        val second = string.substring(string.length / 2).toLong()
                        val retval = listOf(first, second)
                        splitMap[current.key] = retval
                        return@flatMap retval.map { it to current.value }
                    }

                    val retval = listOf(current.key * 2024)
                    splitMap[current.key] = retval
                    return@flatMap retval.map { it to current.value }
                }
            }.flatten()

            bag.clear()

            updates.forEach { (key, value) ->
                bag[key] = bag.getOrDefault(key, 0) + value
            }

            return this
        }

        fun count(): Long = bag.values.sum()
    }

    override suspend fun part1(): Any {
        val state = Bag(input.split(" ").map{ it.toLong() })

        repeat(25) {
            state.step()
        }

        return state.count()
    }

    override suspend fun part2(): Any {
        val state = Bag(input.split(" ").map{ it.toLong() })

        repeat(75) {
            state.step()
        }

        return state.count()
    }
}