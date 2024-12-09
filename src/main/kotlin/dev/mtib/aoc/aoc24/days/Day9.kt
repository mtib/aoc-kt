package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.chunkedParMap
import java.math.BigInteger
import kotlin.math.min

object Day9: AocDay(2024, 9) {
    sealed class Descriptor(val size: Int)

    private class File(
        val ID: Int,
        size: Int,
    ): Descriptor(size)
    private class Free(
        size: Int,
    ) : Descriptor(size)

    private suspend fun MutableList<Descriptor>.readInput() {
        return input.withIndex().chunkedParMap(5000) { part ->
            part.mapNotNull { (i, c) ->
                val isFile = i % 2 == 0
                val size = c - '0'
                if (size > 0) {
                    if (isFile) {
                        File(i/2, size)
                    } else {
                        Free(size)
                    }
                } else {
                    null
                }
            }
        }.forEach { addAll(it) }
    }

    private fun MutableList<Descriptor>.moveBlocks(): Boolean {
        val lastFileIndex = indexOfLast { it is File }
        val firstFreeIndex = indexOfFirst { it is Free }

        if (firstFreeIndex == -1) {
            return false
        }

        val lastFile = get(lastFileIndex) as File
        val free = get(firstFreeIndex) as Free

        val blocksToMove = min(lastFile.size, free.size)

        val newFileLeft = File(lastFile.ID, blocksToMove)
        val newFileRight = File(lastFile.ID, lastFile.size - blocksToMove)
        val newFree = Free(free.size - blocksToMove)

        if (newFree.size == 0) {
            removeAt(firstFreeIndex)
        } else {
            set(firstFreeIndex, newFree)
        }
        add(firstFreeIndex, newFileLeft)
        if (newFileRight.size > 0) {
            set(lastFileIndex + if (newFree.size != 0) 1 else 0, newFileRight)
        } else {
            removeAt(lastFileIndex + if (newFree.size != 0) 1 else 0)
        }
        return true
    }

    private suspend fun blockMemCompress(): Long {
        val malloc = buildList<Descriptor> {
            readInput()

            while (true) {
                if (!moveBlocks()) {
                    break
                }
            }
        }

        return sequence<Long> {
            for (file in malloc as List<File>) {
                repeat(file.size) {
                    yield(file.ID.toLong())
                }
            }
        }.withIndex().fold(0L) { acc, (i, v) -> (i * v) + acc }
    }

    private fun MutableList<Descriptor>.moveFile(file: File) {
        val freeIndex = indexOfFirst { it is Free && it.size >= file.size }
        if (freeIndex == -1) {
            return
        }

        val free = get(freeIndex) as Free

        val lastFileIndex = indexOfLast { it is File && it.ID == file.ID }
        if (lastFileIndex < freeIndex) {
            return
        }

        val beforeLastFile = getOrNull(lastFileIndex - 1)
        val afterLastFile = getOrNull(lastFileIndex + 1)

        if (lastFileIndex - 1 != freeIndex && beforeLastFile is Free && afterLastFile is Free) {
            removeAt(lastFileIndex + 1)
            removeAt(lastFileIndex)
            set(lastFileIndex - 1, Free(beforeLastFile.size + file.size + afterLastFile.size))
        } else if (lastFileIndex - 1 != freeIndex && beforeLastFile is Free) {
            removeAt(lastFileIndex)
            set(lastFileIndex - 1, Free(beforeLastFile.size + file.size))
        } else if (afterLastFile is Free) {
            removeAt(lastFileIndex + 1)
            set(lastFileIndex, Free(file.size + afterLastFile.size))
        } else {
            set(lastFileIndex, Free(file.size))
        }

        val afterFree = getOrNull(freeIndex + 1)

        val newFree = Free(free.size - file.size + if (afterFree is Free) afterFree.size else 0)
        if(newFree.size == 0) {
            removeAt(freeIndex)
        } else {
            set(freeIndex, newFree)
            if(afterFree is Free) {
                removeAt(freeIndex + 1)
            }
        }

        add(freeIndex, file)
    }

    private suspend fun fileMemCompress(): Long {
        val malloc = buildList<Descriptor> {
            readInput()

            for (lastFile in filterIsInstance<File>().reversed()) {
                moveFile(lastFile)
            }
        }

        return sequence<Long> {
            for (descriptor in malloc) {
                repeat(descriptor.size) {
                    when(descriptor) {
                        is File -> yield(descriptor.ID.toLong())
                        is Free -> {
                            yield(0)
                        }
                    }
                }
            }
        }.withIndex().fold(0L) { acc, (i, v) -> (i * v) + acc }
    }

    override suspend fun part1(): Any {
        return blockMemCompress()
    }

    override suspend fun part2(): Any {
        return fileMemCompress()
    }
}