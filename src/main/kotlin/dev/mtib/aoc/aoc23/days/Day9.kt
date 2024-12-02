package dev.mtib.aoc.aoc23.days

import dev.mtib.aoc.aoc23.util.AbstractDay

object Day9 : AbstractDay(9) {

    private fun extrapolateForwards(report: Array<Long>): Long {
        val sequences = buildList<Array<Long>> {
            add(report)

            while (last().any { it != 0L }) {
                val lastSequence = last()
                add(buildList<Long> {
                    for (i in 0 until lastSequence.size - 1) {
                        add(lastSequence[i + 1] - lastSequence[i])
                    }
                }.toTypedArray())
            }
        }
        val lastDiagonal = buildList<Long> {
            add(0L)
            for (i in 1 until sequences.size) {
                add(sequences[sequences.size - i - 1].last() + last())
            }
        }
        return lastDiagonal.last()
    }

    private fun extrapolateBackwards(report: Array<Long>): Long {
        val sequences = buildList<Array<Long>> {
            add(report)

            while (last().any { it != 0L }) {
                val lastSequence = last()
                add(buildList<Long> {
                    for (i in 0 until lastSequence.size - 1) {
                        add(lastSequence[i + 1] - lastSequence[i])
                    }
                }.toTypedArray())
            }
        }
        val firstDiagonal = buildList<Long> {
            add(0L)
            for (i in 1 until sequences.size) {
                add(sequences[sequences.size - i - 1].first() - last())
            }
        }
        return firstDiagonal.last()
    }

    override fun solvePart1(input: Array<String>): String {
        val oasisReports = input.filter { it.isNotBlank() }.map { it.split(" ").map { it.toLong() }.toTypedArray() }
        return oasisReports.sumOf { extrapolateForwards(it) }.toString()
    }

    override fun solvePart2(input: Array<String>): String {
        val oasisReports = input.filter { it.isNotBlank() }.map { it.split(" ").map { it.toLong() }.toTypedArray() }
        return oasisReports.sumOf { extrapolateBackwards(it) }.toString()
    }
}
