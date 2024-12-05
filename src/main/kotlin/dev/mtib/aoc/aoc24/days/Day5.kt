package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.chunkedParMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.launch

object Day5: AocDay(2024, 5) {
    private data class Parsed(
        val manuals: ReceiveChannel<List<Short>>,
        val rules: Map<Short, Set<Short>>
    ) {
        enum class Validity {
            VALID, INVALID
        }
        companion object {
            suspend fun get(only: Validity = Validity.VALID, parentScope: CoroutineScope) : Parsed {
                val emptyLine = inputLinesList.indexOfFirst { it == "" }

                val rules: MutableMap<Short, MutableSet<Short>> = mutableMapOf()
                coroutineScope {
                    val ruleLines = inputLinesList.subList(0, emptyLine)
                    val ruleChannel = Channel<Pair<Short, Short>>(ruleLines.size)
                    launch {
                        ruleLines.chunkedParMap(ruleLines.size / cpu) { lines ->
                            for (line in lines) {
                                val (pred, succ) = line.split("|", limit=2)
                                ruleChannel.send(pred.toShort() to succ.toShort())
                            }
                        }
                        ruleChannel.close()
                    }
                    launch {
                        for ((pred, succ) in ruleChannel) {
                            rules.getOrPut(pred) { mutableSetOf() }.add(succ)
                        }
                    }
                }

                val manualChannel = Channel<List<Short>>(100)
                parentScope.launch {
                    val manualLines = inputLinesList.subList(emptyLine + 1, inputLinesList.size)
                    manualLines.chunkedParMap(manualLines.size / cpu) { lines ->
                        lines@for (line in lines) {
                            val pages = line.split(",").map { it.toShort() }
                            for (i in pages.indices) {
                                val notAllowed = rules[pages[i]] ?: emptySet()
                                if (pages.subList(0, i).any { it in notAllowed }) {
                                    if (only == Validity.VALID) continue@lines
                                    manualChannel.send(pages)
                                    continue@lines
                                }
                            }
                            if (only == Validity.INVALID) continue@lines
                            manualChannel.send(pages)
                        }
                    }
                    manualChannel.close()
                }

                return Parsed(manualChannel, rules)
            }
        }
        data class Manuals(
            val valid: Set<List<Short>>,
            val invalid: Set<List<Short>>
        )
    }

    override suspend fun part1(): Any = coroutineScope {
        val parsed = Parsed.get(only = Parsed.Validity.VALID, parentScope = this)

        parsed.manuals.consumeAsFlow().fold(0) { acc, it -> acc + it[it.size / 2].toInt() }
    }

    override suspend fun part2(): Any = coroutineScope {
        val parsed = Parsed.get(only = Parsed.Validity.INVALID, parentScope = this)

        val comparator = java.util.Comparator<Short> { o1, o2 ->
            when {
                parsed.rules[o1]?.contains(o2) == true -> -1
                parsed.rules[o2]?.contains(o1) == true -> 1
                else -> 0
            }
        }

        parsed.manuals.consumeAsFlow().fold(0) { acc, it ->
            acc + it.sortedWith(comparator)[it.size / 2].toInt()
        }
    }
}