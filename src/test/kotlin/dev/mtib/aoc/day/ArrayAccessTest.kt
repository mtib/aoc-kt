package dev.mtib.aoc.day

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ArrayAccessTest: FunSpec({
    test("Array access") {
        val day = object: AocDay(2024, 9) {
            override suspend fun part1(): Any {
                return super.part1()
            }

            override suspend fun part2(): Any {
                return super.part2()
            }
        }

        day.withInput("abc\ndef\nghi") {
            day.getChar(0, 0) shouldBe 'a'
            day.getChar(1, 0) shouldBe 'b'
            day.getChar(2, 0) shouldBe 'c'

            day.getChar(0, 1) shouldBe 'd'
            day.getChar(1, 1) shouldBe 'e'
            day.getChar(2, 1) shouldBe 'f'

            day.getChar(0, 2) shouldBe 'g'
            day.getChar(1, 2) shouldBe 'h'
            day.getChar(2, 2) shouldBe 'i'
        }
    }
})