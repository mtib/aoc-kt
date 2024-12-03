package dev.mtib.aoc.day

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DayHintTest : FunSpec({
    test("rejects on exact match when unknown") {
        val retval = 1
        val aoc = object : AocDay(1, 1) {
            override suspend fun part1(): Any {
                not(retval)
                return retval
            }
        }

        aoc.partMode = 1
        aoc.part1() shouldBe retval
        val hint = aoc.compareHints(1, retval)
        hint.leftOrNull() shouldBe AocDay.Hint.Direction.Unknown
    }

    test("accepts when miss match when unknown") {
        val retval = 1
        val aoc = object : AocDay(1, 1) {
            override suspend fun part1(): Any {
                not(retval + 1)
                return retval
            }
        }

        aoc.partMode = 1
        aoc.part1() shouldBe retval
        val hint = aoc.compareHints(1, retval)
        hint.leftOrNull() shouldBe null
    }
    test("accepts when miss match when too low") {
        val retval = 1
        val aoc = object : AocDay(1, 1) {
            override suspend fun part1(): Any {
                not(retval - 1, Hint.Direction.TooLow)
                return retval
            }
        }

        aoc.partMode = 1
        aoc.part1() shouldBe retval
        val hint = aoc.compareHints(1, retval)
        hint.leftOrNull() shouldBe null
    }
    test("accepts when miss match when too high") {
        val retval = 1
        val aoc = object : AocDay(1, 1) {
            override suspend fun part1(): Any {
                not(retval + 1, Hint.Direction.TooHigh)
                return retval
            }
        }

        aoc.partMode = 1
        aoc.part1() shouldBe retval
        val hint = aoc.compareHints(1, retval)
        hint.leftOrNull() shouldBe null
    }
    test("rejects when too low") {
        val retval = 1
        val aoc = object : AocDay(1, 1) {
            override suspend fun part1(): Any {
                not(retval + 1, Hint.Direction.TooLow)
                return retval
            }
        }

        aoc.partMode = 1
        aoc.part1() shouldBe retval
        val hint = aoc.compareHints(1, retval)
        hint.leftOrNull() shouldBe AocDay.Hint.Direction.TooLow
    }
    test("rejects when too high") {
        val retval = 1
        val aoc = object : AocDay(1, 1) {
            override suspend fun part1(): Any {
                not(retval - 1, Hint.Direction.TooHigh)
                return retval
            }
        }

        aoc.partMode = 1
        aoc.part1() shouldBe retval
        val hint = aoc.compareHints(1, retval)
        hint.leftOrNull() shouldBe AocDay.Hint.Direction.TooHigh
    }
    test("accept when miss in middle") {
        val retval = 1
        val aoc = object : AocDay(1, 1) {
            override suspend fun part1(): Any {
                not(retval - 1, Hint.Direction.TooLow)
                not(retval + 1, Hint.Direction.TooHigh)
                return retval
            }
        }

        aoc.partMode = 1
        aoc.part1() shouldBe retval
        val hint = aoc.compareHints(1, retval)
        hint.leftOrNull() shouldBe null
    }

    test("throws if impossible") {
        val aoc = object : AocDay(1, 1) {
            override suspend fun part1(): Any {
                not(10, Hint.Direction.TooLow)
                not(0, Hint.Direction.TooHigh)
                return 0
            }
        }

        aoc.partMode = 1

        shouldThrowAny {
            aoc.part1()
        }
    }

    test("throws if impossible (exact)") {
        val aoc = object : AocDay(1, 1) {
            override suspend fun part1(): Any {
                not(0, Hint.Direction.TooLow)
                not(0, Hint.Direction.TooHigh)
                return 0
            }
        }

        aoc.partMode = 1

        shouldThrowAny {
            aoc.part1()
        }
    }

    test("throws if impossible (none left)") {
        val aoc = object : AocDay(1, 1) {
            override suspend fun part1(): Any {
                not(0, Hint.Direction.TooLow)
                not(1, Hint.Direction.TooHigh)
                return 0
            }
        }

        aoc.partMode = 1

        shouldThrowAny {
            aoc.part1()
        }
    }
})
