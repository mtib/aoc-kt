package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay

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

        fun inRange(xRange: IntRange, yRange: IntRange) = x in xRange && y in yRange
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

        val xRange = inputLinesArray[0].indices
        val yRange = inputLinesArray.indices

        return buildSet<Vector> {
            antennas.values.forEach { sameFreqAntennas ->
                for (i in sameFreqAntennas.indices) {
                    for (j in (i+1)..sameFreqAntennas.lastIndex) {
                        val antenna1 = sameFreqAntennas[i]
                        val antenna2 = sameFreqAntennas[j]

                        val walkDiff = antenna1.position.let {
                            Vector(antenna2.position.x - it.x, antenna2.position.y - it.y)
                        }

                        (antenna2.position + walkDiff).let {
                            if (it.inRange(xRange, yRange)) {
                                add(it)
                            }
                        }
                        (antenna1.position - walkDiff).let {
                            if (it.inRange(xRange, yRange)) {
                                add(it)
                            }
                        }
                    }
                }
            }
        }.count()
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

        val xRange = inputLinesArray[0].indices
        val yRange = inputLinesArray.indices

        return buildSet<Vector> {
            antennas.values.forEach { sameFreqAntennas ->
                for (i in sameFreqAntennas.indices) {
                    for (j in (i+1)..sameFreqAntennas.lastIndex) {
                        val antenna1 = sameFreqAntennas[i]
                        val antenna2 = sameFreqAntennas[j]

                        val walkDiff = antenna1.position.let {
                            Vector(antenna2.position.x - it.x, antenna2.position.y - it.y)
                        }


                        var found = false
                        var stepLength = 0
                        while (found || stepLength == 0) {
                            found = false
                            val step = walkDiff * stepLength
                            (antenna2.position + step).let {
                                if (it.inRange(xRange, yRange)) {
                                    found = true
                                    add(it)
                                }
                            }
                            (antenna1.position - step).let {
                                if (it.inRange(xRange, yRange)) {
                                    found = true
                                    add(it)
                                }
                            }
                            stepLength++
                        }
                    }
                }
            }
        }.count()
    }
}