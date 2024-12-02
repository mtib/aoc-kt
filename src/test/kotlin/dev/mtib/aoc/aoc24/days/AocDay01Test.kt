package dev.mtib.aoc.aoc24.days

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AocDay01Test : FunSpec({
    test("Part 1") {
        AocDay01.withInput(
            """
            3   4
            4   3
            2   5
            1   3
            3   9
            3   3
        """.trimIndent()
        ) {
            part1() shouldBe "11"
        }
    }

    test("Part 2") {
        AocDay01.withInput(
            """
            3   4
            4   3
            2   5
            1   3
            3   9
            3   3
        """.trimIndent()
        ) {
            part2() shouldBe "31"
        }
    }
})
