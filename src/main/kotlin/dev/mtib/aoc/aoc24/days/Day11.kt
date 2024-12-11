package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.chunkedParLaunchZ
import dev.mtib.aoc.util.chunkedParMap
import dev.mtib.aoc.util.chunkedParMapZ
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.log10

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

        private val powOfTens = ConcurrentHashMap<Int, Long>()

        suspend fun stepList(): Bag = coroutineScope {
                val updates = bag.entries.toList().chunkedParMapZ(bag.size / cpu - 2) {
                    it.map {current ->
                        if (splitMap[current.key] != null) {
                            return@map splitMap[current.key]!! to current.value
                        }

                        // case of 1 handled by initialising map

                        val string = current.key.toString()
                        val digits = log10(current.key.toDouble()).toInt() + 1
                        if (digits % 2 == 0) {
                            val powOfTen = powOfTens.getOrPut(digits / 2) { List(digits / 2) { 10L }.reduce { acc, i -> acc * i } }
                            val first = current.key / powOfTen
                            val second = current.key % powOfTen
                            val retval = listOf(first, second)
                            splitMap[current.key] = retval
                            return@map retval to current.value
                        }

                        val retval = listOf(current.key * 2024)
                        splitMap[current.key] = retval
                        return@map retval to current.value
                    }
                }

            bag.clear()

            for (chunk in updates) {
                for ((keys, value) in chunk) {
                    for (key in keys) {
                        bag[key] = bag.getOrDefault(key, 0) + value
                    }
                }
            }

            return@coroutineScope this@Bag
        }

        suspend fun stepChannel(): Bag = coroutineScope {
            val entries = bag.entries.toList()
            val updateChannel = Channel<Pair<List<Long>, Long>>(Channel.UNLIMITED)
            launch {
                entries.chunkedParLaunchZ(entries.size / cpu - 2) {
                    it.forEach {current ->
                        if (splitMap[current.key] != null) {
                            updateChannel.send(splitMap[current.key]!! to current.value)
                            return@forEach
                        }

                        // case of 1 handled by initialising map

                        val string = current.key.toString()
                        val digits = log10(current.key.toDouble()).toInt() + 1
                        if (digits % 2 == 0) {
                            val powOfTen = powOfTens.getOrPut(digits / 2) { List(digits / 2) { 10L }.reduce { acc, i -> acc * i } }
                            val first = current.key / powOfTen
                            val second = current.key % powOfTen
                            val retval = listOf(first, second)
                            splitMap[current.key] = retval
                            updateChannel.send(retval to current.value)
                            return@forEach
                        }

                        val retval = listOf(current.key * 2024)
                        splitMap[current.key] = retval
                        updateChannel.send(retval to current.value)
                        return@forEach
                    }
                }.joinAll()
                updateChannel.close()
            }

            bag.clear()

            for ((keys, value) in updateChannel) {
                for (key in keys) {
                    bag[key] = bag.getOrDefault(key, 0) + value
                }
            }

            return@coroutineScope this@Bag
        }

        fun count(): Long = bag.values.sum()
    }

    override suspend fun part1(): Any {
        val state = Bag(input.split(" ").map{ it.toLong() })

        repeat(25) {
            state.stepList()
        }

        return state.count()
    }

    override suspend fun part2(): Any {
        val state = Bag(input.split(" ").map{ it.toLong() })

        repeat(75) {
            state.stepList()
        }

        return state.count()
    }
}