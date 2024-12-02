package dev.mtib.aoc24.days

import dev.mtib.aoc.aoc24.days.AocDay02
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AocDay02Test : FunSpec({

    test("part1") {
        AocDay02.withInput(
            """
            7 6 4 2 1
            1 2 7 8 9
            9 7 6 2 1
            1 3 2 4 5
            8 6 4 4 1
            1 3 6 7 9
        """.trimIndent()
        ) {
            part1() shouldBe "2"
        }
    }

    test("part2") {
        AocDay02.withInput(
            """
            7 6 4 2 1
            1 2 7 8 9
            9 7 6 2 1
            1 3 2 4 5
            8 6 4 4 1
            1 3 6 7 9
        """.trimIndent()
        ) {
            part2() shouldBe "4"
        }
    }
})
