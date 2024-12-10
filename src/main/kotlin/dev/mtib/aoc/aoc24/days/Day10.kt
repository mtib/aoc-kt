package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.chunkedParMap

object Day10 : AocDay(2024, 10) {
    private val directions = listOf(
        Pair(0, -1),
        Pair(1, 0),
        Pair(0, 1),
        Pair(-1, 0),
    )
    private fun countReachableNines(start: Pair<Int, Int>): Int {
        val heads = mutableSetOf(start)
        val endings = mutableSetOf<Pair<Int, Int>>()
        while (heads.isNotEmpty()) {
            val current = heads.first()

            val currentChar = getChar(current)
            if (currentChar == '9') {
                endings.add(current)
            } else {
                for (direction in directions) {
                    val newHead = Pair(current.first + direction.first, current.second + direction.second)
                    if ((getCharOrNull(newHead) ?: continue) - currentChar == 1) {
                        heads.add(newHead)
                    }
                }
            }

            heads.remove(current)
        }
        return endings.count()
    }

    private fun countPathsToNines(start: Pair<Int, Int>): Int {
        val heads = mutableListOf(start)
        val endings = mutableListOf<Pair<Int, Int>>()
        while (heads.isNotEmpty()) {
            val current = heads.first()

            val currentChar = getChar(current)
            if (currentChar == '9') {
                endings.add(current)
            } else {
                for (direction in directions) {
                    val newHead = Pair(current.first + direction.first, current.second + direction.second)
                    if ((getCharOrNull(newHead) ?: continue) - currentChar == 1) {
                        heads.add(newHead)
                    }
                }
            }

            heads.remove(current)
        }
        return endings.count()
    }

    override suspend fun part1(): Any {
        val positionOfZeros = buildList {
            val newLineLenght = lineLength + 1
            for(i in inputArray.indices) {
                if (inputArray[i] == '0') {
                    add(Pair(i % newLineLenght, i / newLineLenght))
                }
            }
        }
        return positionOfZeros.chunkedParMap(100) { starts -> starts.sumOf{ countReachableNines(it) } }.sum()
    }

    override suspend fun part2(): Any {
        val positionOfZeros = buildList {
            val newLineLenght = lineLength + 1
            for(i in inputArray.indices) {
                if (inputArray[i] == '0') {
                    add(Pair(i % newLineLenght, i / newLineLenght))
                }
            }
        }
        return positionOfZeros.chunkedParMap(100) { starts -> starts.sumOf{ countPathsToNines(it) } }.sum()
    }
}