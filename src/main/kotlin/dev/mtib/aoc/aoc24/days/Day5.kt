package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.AocLogger.Companion.logger
import dev.mtib.aoc.util.chunkedParMap
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object Day5: AocDay(2024, 5) {
    private data class Parsed(
        val manuals: Manuals,
        val rules: Map<Int, Set<Int>>
    ) {
        companion object {
            suspend fun get() : Parsed = coroutineScope {
                val emptyLine = inputLinesList.indexOfFirst { it == "" }

                val rules: MutableMap<Int, MutableSet<Int>> = mutableMapOf()
                coroutineScope {
                    val ruleLines = inputLinesList.slice(0 until emptyLine)
                    val ruleChannel = Channel<Pair<Int, Int>>(ruleLines.size)
                    launch {
                        ruleLines.chunkedParMap(ruleLines.size / cpu) { lines ->
                            for (line in lines) {
                                val rule = line.split("|")
                                val pred = rule[0].trim().toInt()
                                val succ = rule[1].trim().toInt()

                                ruleChannel.send(pred to succ)
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

                val manuals = object {
                    val valid = mutableSetOf<List<Int>>()
                    val invalid = mutableSetOf<List<Int>>()
                }
                coroutineScope {
                    val manualLines = inputLinesList.slice(emptyLine + 1 until inputLinesList.size)
                    val manualChannel = Channel<Pair<Boolean, List<Int>>>(manualLines.size)
                    launch {
                        manualLines.chunkedParMap(manualLines.size / cpu) { lines ->
                            lines@for (line in lines) {
                                val pages = line.split(",").map { it.toInt() }
                                for (i in pages.indices) {
                                    val notAllowed = rules[pages[i]] ?: emptySet()
                                    if (notAllowed.intersect(pages.slice(0 until i).toSet()).isNotEmpty() ) {
                                        manualChannel.send(false to pages)
                                        continue@lines
                                    }
                                }
                                manualChannel.send(true to pages)
                            }
                        }
                        manualChannel.close()
                    }

                    launch {
                        for ((isValid, manual) in manualChannel) {
                            when(isValid) {
                                true -> manuals.valid
                                false -> manuals.invalid
                            }.add(manual)
                        }
                    }
                }

                Parsed(Manuals(manuals.valid, manuals.invalid), rules)
            }
        }
        data class Manuals(
            val valid: Set<List<Int>>,
            val invalid: Set<List<Int>>
        )
    }

    override suspend fun part1(): Any {
        val parsed = Parsed.get()

        return parsed.manuals.valid.sumOf { it[it.size / 2] }
    }

    override suspend fun part2(): Any {
        val parsed = Parsed.get()

        val comparator = java.util.Comparator<Int> { o1, o2 ->
            when {
                parsed.rules[o1]?.contains(o2) == true -> -1
                parsed.rules[o2]?.contains(o1) == true -> 1
                else -> 0
            }
        }
        return parsed.manuals.invalid.sumOf {
            it.sortedWith(comparator)[it.size / 2]
        }
    }
}