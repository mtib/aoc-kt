package dev.mtib.aoc.aoc24.days

import io.kotest.core.spec.style.FunSpec

class Day10Test : FunSpec({
    val snippet0 = """
        0123
        1234
        8765
        9876
    """.trimIndent()
    val snippet1 = """
        ...0...
        ...1...
        ...2...
        6543456
        7.....7
        8.....8
        9.....9
    """.trimIndent()
    val snippet2 = """
        ..90..9
        ...1.98
        ...2..7
        6543456
        765.987
        876....
        987....
    """.trimIndent()
    val snippet3 = """
        10..9..
        2...8..
        3...7..
        4567654
        ...8..3
        ...9..2
        .....01
    """.trimIndent()
    val snippet4 = """
        89010123
        78121874
        87430965
        96549874
        45678903
        32019012
        01329801
        10456732
    """.trimIndent()
    context("part1") {
        test("doesn't throw") {
            try {
                Day10.part1()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
    }
    context("part2") {
        test("doesn't throw") {
            try {
                Day10.part2()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
    }
})
