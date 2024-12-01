package dev.mtib.aoc24.days

import arrow.fx.coroutines.parMapUnordered
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

object AocDay01 : AocDay(1) {
    private val logger = KotlinLogging.logger {}

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.readChannels(): Pair<Channel<Int>, Channel<Int>> {
        val left = Channel<Int>(Channel.UNLIMITED)
        val right = Channel<Int>(Channel.UNLIMITED)

        launch {
            inputLines.asFlow().parMapUnordered(8) {
                if (it.isBlank()) {
                    return@parMapUnordered
                }
                it.split("   ").map { it.toInt() }.also {
                    left.send(it[0])
                    right.send(it[1])
                }
            }.collect { }
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

        if (!benchmarking) {
            leftList.await().let { logger.log { "Left: ${it.size}" } }
            rightList.await().let { logger.log { "Right: ${it.size}" } }
        }

        val result = leftList.await().zip(rightList.await()) { l, r -> abs(l - r) }.sum()

        result.toString()
    }

    class SetFlowCollector : FlowCollector<Int> {
        val state = mutableSetOf<Int>()
        override suspend fun emit(value: Int) {
            state.add(value)
        }
    }

    class GroupingCountByCollector : FlowCollector<Int> {
        val state = mutableMapOf<Int, Int>()
        override suspend fun emit(value: Int) {
            state[value] = state.getOrPut(value) { 0 } + 1
        }
    }

    override suspend fun part2(): String = coroutineScope {
        val (left, right) = readChannels()

        val leftList = async {
            val collector = SetFlowCollector()
            left.consumeAsFlow().collect(collector)
            collector.state
        }
        val rightList = async {
            val collector = GroupingCountByCollector()
            right.consumeAsFlow().collect(collector)
            collector.state
        }

        val r = rightList.await()
        val result = leftList.await().sumOf {
            it * (r[it] ?: 0)
        }

        result.toString()
    }
}
