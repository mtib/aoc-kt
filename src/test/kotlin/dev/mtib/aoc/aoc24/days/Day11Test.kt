package dev.mtib.aoc.aoc24.days

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Day11Test : FunSpec({
    val snippet0 = """
        Initial arrangement:
        125 17
        
        After 1 blink:
        253000 1 7
        
        After 2 blinks:
        253 0 2024 14168
        
        After 3 blinks:
        512072 1 20 24 28676032
        
        After 4 blinks:
        512 72 2024 2 0 2 4 2867 6032
        
        After 5 blinks:
        1036288 7 2 20 24 4048 1 4048 8096 28 67 60 32
        
        After 6 blinks:
        2097446912 14168 4048 2 0 2 4 40 48 2024 40 48 80 96 2 8 6 7 6 0 3 2
    """.trimIndent()
    context("part1") {
        test("doesn't throw") {
            try {
                Day11.part1()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
        test("0 to 1") {
            Day11.Bag(listOf(0)).stepChannel().count() shouldBe 1L
        }
        test("1 to 2024") {
            Day11.Bag(listOf(1)).stepChannel().count() shouldBe 1L
        }
        test("1,2 to 2024,4048") {
            Day11.Bag(listOf(1, 2)).stepChannel().count() shouldBe 2L
        }
        test("10 to (1, 0)") {
            Day11.Bag(listOf(10)).stepChannel().count() shouldBe 2L
        }
        test("10:2 to (1:2, 0:2)") {
            Day11.Bag(listOf(10, 10)).stepChannel().count() shouldBe 4L
        }
        test("10:2 to (1:2, 0:2)") {
            Day11.Bag(listOf(10, 10)).stepChannel().count() shouldBe 4L
        }
    }
    context("part2") {
        test("doesn't throw") {
            try {
                Day11.part2()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
    }
})
