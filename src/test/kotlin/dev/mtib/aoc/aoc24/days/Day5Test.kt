package dev.mtib.aoc.aoc24.days

import io.kotest.core.spec.style.FunSpec

class Day5Test : FunSpec({
    val snippet0 = """
        47|53
        97|13
        97|61
        97|47
        75|29
        61|13
        75|53
        29|13
        97|29
        53|29
        61|53
        97|53
        61|29
        47|13
        75|47
        97|75
        47|61
        75|61
        47|29
        75|13
        53|13
        
        75,47,61,53,29
        97,61,53,29,13
        75,29,13
        75,97,47,61,53
        61,13,29
        97,13,75,29,47
    """.trimIndent()
    context("part1") {
        test("doesn't throw") {
            try {
                Day5.part1()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
    }
    context("part2") {
        test("doesn't throw") {
            try {
                Day5.part2()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
    }
})
