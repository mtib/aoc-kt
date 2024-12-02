package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.AocLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

object AocDay01 : AocDay(1) {
    private val logger = AocLogger.new { }
    private fun CoroutineScope.readChannels(): Pair<Channel<Int>, Channel<Int>> {
        val left = Channel<Int>(Channel.UNLIMITED)
        val right = Channel<Int>(Channel.UNLIMITED)

        launch {
            coroutineScope {
                inputLinesList.chunked(100).forEach {
                    launch {
                        it.forEach {
                            it.split("   ").map { it.toInt() }.also {
                                left.send(it[0])
                                right.send(it[1])
                            }
                        }
                    }
                }
            }
            left.close()
            right.close()
        }

        return left to right
    }

    class SortedFlowCollector : FlowCollector<Int> {
        val state = mutableListOf<Int>()
        override suspend fun emit(value: Int) {
            state.binarySearch(value).let { index ->
                state.add(if (index < 0) -index - 1 else index, value)
            }
        }
    }

    override suspend fun part1(): String = coroutineScope {
        val (left, right) = readChannels()

        val leftList = async {
            val collector = SortedFlowCollector()
            left.consumeAsFlow().collect(collector)
            collector.state
        }
        val rightList = async {
            val collector = SortedFlowCollector()
            right.consumeAsFlow().collect(collector)
            collector.state
        }

        logger.logSuspend { leftList.await().let { "left list size: ${it.size}" } }
        logger.logSuspend { rightList.await().let { "right list size: ${it.size}" } }

        val result = leftList.await().zip(rightList.await()) { l, r -> abs(l - r) }.sum()

        result.toString()
    }

    class GroupingCountByCollector : FlowCollector<Int> {
        val state = mutableMapOf<Int, Int>()
        override suspend fun emit(value: Int) {
            state[value] = state.getOrPut(value) { 0 } + 1
        }
    }

    override suspend fun part2(): String = coroutineScope {
        val (left, right) = readChannels()

        val rightList = async {
            val collector = GroupingCountByCollector()
            right.consumeAsFlow().collect(collector)
            collector.state
        }

        logger.logSuspend {
            rightList.await()
                .let { "right map size: ${it.size} (min=${it.values.min()}, max=${it.values.max()})" }
        }

        val r = rightList.await()
        val result = left.consume {
            var sum = 0
            left.consumeEach {
                sum += it * (r[it] ?: 0)
            }
            sum
        }

        result.toString()
    }
}
