package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.AocLogger.Companion.logger
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

    private fun blockMemCompress(): BigInteger {
        val malloc = buildList<Descriptor> {
            for ((i, c) in input.withIndex()) {
                val isFile = i % 2 == 0
                val size = c - '0'
                if (size > 0) {
                    if (isFile) {
                        add(File(i/2, size))
                    } else {
                        add(Free(size))
                    }
                }
            }

            while (true) {
                val lastFileIndex = indexOfLast { it is File }
                val firstFreeIndex = indexOfFirst { it is Free }

                if (firstFreeIndex == -1) {
                    break
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
            }
        }

        return buildList<Int> {
            for (file in malloc as List<File>) {
                repeat(file.size) {
                    add(file.ID)
                }
            }
        }.withIndex().fold(BigInteger.ZERO) { acc, (i, v) -> (i * v).toBigInteger() + acc }
    }

    private fun fileMemCompress(): BigInteger {
        val malloc = buildList<Descriptor> {
            for ((i, c) in input.withIndex()) {
                val isFile = i % 2 == 0
                val size = c - '0'
                if (size > 0) {
                    if (isFile) {
                        add(File(i/2, size))
                    } else {
                        add(Free(size))
                    }
                }
            }

            for (lastFile in filterIsInstance<File>().reversed()) {
                val free = withIndex().firstOrNull { it.value is Free && it.value.size >= lastFile.size } as IndexedValue<Free>?

                if (free == null) {
                    continue
                }

                val lastFileIndex = indexOfLast { it is File && it.ID == lastFile.ID }
                if (lastFileIndex < free.index) {
                    continue
                }

                val beforeLastFile = getOrNull(lastFileIndex - 1)
                val afterLastFile = getOrNull(lastFileIndex + 1)

                if (beforeLastFile != free.value && beforeLastFile is Free && afterLastFile is Free) {
                    removeAt(lastFileIndex + 1)
                    removeAt(lastFileIndex)
                    set(lastFileIndex - 1, Free(beforeLastFile.size + lastFile.size + afterLastFile.size))
                } else if (beforeLastFile != free.value && beforeLastFile is Free) {
                    removeAt(lastFileIndex)
                    set(lastFileIndex - 1, Free(beforeLastFile.size + lastFile.size))
                } else if (afterLastFile is Free) {
                    removeAt(lastFileIndex + 1)
                    set(lastFileIndex, Free(lastFile.size + afterLastFile.size))
                } else {
                    set(lastFileIndex, Free(lastFile.size))
                }

                val afterFree = getOrNull(free.index + 1)

                val newFree = Free(free.value.size - lastFile.size + if (afterFree is Free) afterFree.size else 0)
                if(newFree.size == 0) {
                    removeAt(free.index)
                } else {
                    set(free.index, newFree)
                    if(afterFree is Free) {
                        removeAt(free.index + 1)
                    }
                }

                add(free.index, lastFile)
            }

            while (last() is Free) {
                removeLast()
            }
        }

        return buildList<Int> {
            for (descriptor in malloc) {
                repeat(descriptor.size) {
                    when(descriptor) {
                        is File -> add(descriptor.ID)
                        is Free -> {
                            add(0)
                        }
                    }
                }
            }
        }.withIndex().fold(BigInteger.ZERO) { acc, (i, v) -> (i * v).toBigInteger() + acc }
    }

    override suspend fun part1(): Any {
        return blockMemCompress()
    }

    override suspend fun part2(): Any {
        return fileMemCompress()
    }
}