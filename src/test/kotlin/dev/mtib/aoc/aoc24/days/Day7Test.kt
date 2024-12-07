package dev.mtib.aoc.aoc24.days

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Day7Test : FunSpec({
    val snippet0 = """
        190: 10 19
        3267: 81 40 27
        83: 17 5
        156: 15 6
        7290: 6 8 6 15
        161011: 16 10 13
        192: 17 8 14
        21037: 9 7 18 13
        292: 11 6 16 20
    """.trimIndent()
    context("part1") {
        test("doesn't throw") {
            try {
                Day7.part1()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }

        test("example") {
            Day7.withInput(snippet0) {
                Day7.part1() shouldBe 3749.toBigInteger()
            }
        }
    }
    context("part2") {
        test("doesn't throw") {
            try {
                Day7.part2()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
        test("example") {
            Day7.withInput(snippet0) {
                Day7.part2() shouldBe 11387.toBigInteger()
            }
        }
    }
})
