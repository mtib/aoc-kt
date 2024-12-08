package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.AocLogger.Companion.logger

object Day8 : AocDay(2024, 8) {
    @JvmInline
    private value class Frequency(val letter: Char)
    private data class Vector(
        val x: Int,
        val y: Int
    ) {
        operator fun plus(other: Vector) = Vector(x + other.x, y + other.y)
        operator fun minus(other: Vector) = Vector(x - other.x, y - other.y)
        operator fun times(scalar: Int) = Vector(x * scalar, y * scalar)
    }
    private data class Antenna(
        val position: Vector,
        val frequency: Frequency
    )
    override suspend fun part1(): Any {
        val antennas = inputLinesArray.mapIndexed { y, line ->
            buildList {
                line.mapIndexed { x, frequency ->
                    if (frequency == '.') {
                        return@mapIndexed
                    }
                    add(Antenna(Vector(x, y), Frequency(frequency)))
                }
            }
        }.flatten().groupBy { it.frequency }

        return buildSet<Vector> {
            antennas.values.forEach { sameFreqAntennas ->
                for (i in sameFreqAntennas.indices) {
                    for (j in sameFreqAntennas.indices) {
                        if (i == j) continue

                        val antenna1 = sameFreqAntennas[i]
                        val antenna2 = sameFreqAntennas[j]

                        val walkDiff = antenna1.position.let {
                            Vector(antenna2.position.x - it.x, antenna2.position.y - it.y)
                        }

                        add(antenna2.position + walkDiff)
                        add(antenna1.position - walkDiff)
                    }
                }
            }
        }.count { it.x in inputLinesArray[0].indices && it.y in inputLinesArray.indices }
    }

    override suspend fun part2(): Any {
        val antennas = inputLinesArray.mapIndexed { y, line ->
            buildList {
                line.mapIndexed { x, frequency ->
                    if (frequency == '.') {
                        return@mapIndexed
                    }
                    add(Antenna(Vector(x, y), Frequency(frequency)))
                }
            }
        }.flatten().groupBy { it.frequency }

        return buildSet<Vector> {
            antennas.values.forEach { sameFreqAntennas ->
                for (i in sameFreqAntennas.indices) {
                    for (j in sameFreqAntennas.indices) {
                        if (i == j) continue

                        val antenna1 = sameFreqAntennas[i]
                        val antenna2 = sameFreqAntennas[j]

                        val walkDiff = antenna1.position.let {
                            Vector(antenna2.position.x - it.x, antenna2.position.y - it.y)
                        }

                        for (i in 0..5000) {
                            add(antenna2.position + walkDiff * i)
                            add(antenna1.position - walkDiff * i)
                        }
                    }
                }
            }
        }.count { it.x in inputLinesArray[0].indices && it.y in inputLinesArray.indices }
    }
}