package dev.mtib.aoc.aoc24.days

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Day3Test : FunSpec({
    test("part1") {
        Day3.withInput(
            """
            xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))
            """.trimIndent()
        ) {
            part1() shouldBe 161
        }
    }

    test("part2") {
        Day3.withInput(
            """
            xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))
            """.trimIndent()
        ) {
            part2() shouldBe 48
        }
    }
})
