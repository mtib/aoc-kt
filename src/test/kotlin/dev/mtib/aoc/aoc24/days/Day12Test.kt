package dev.mtib.aoc.aoc24.days

import io.kotest.core.spec.style.FunSpec

class Day12Test : FunSpec({
    val snippet0 = """
        AAAA
        BBCD
        BBCC
        EEEC
    """.trimIndent()
    val snippet1 = """
        +-+-+-+-+
        |A A A A|
        +-+-+-+-+     +-+
                      |D|
        +-+-+   +-+   +-+
        |B B|   |C|
        +   +   + +-+
        |B B|   |C C|
        +-+-+   +-+ +
                  |C|
        +-+-+-+   +-+
        |E E E|
        +-+-+-+
    """.trimIndent()
    val snippet2 = """
        OOOOO
        OXOXO
        OOOOO
        OXOXO
        OOOOO
    """.trimIndent()
    val snippet3 = """
        RRRRIICCFF
        RRRRIICCCF
        VVRRRCCFFF
        VVRCCCJFFF
        VVVVCJJCFE
        VVIVCCJJEE
        VVIIICJJEE
        MIIIIIJJEE
        MIIISIJEEE
        MMMISSJEEE
    """.trimIndent()
    context("part1") {
        test("doesn't throw") {
            try {
                Day12.part1()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
    }
    context("part2") {
        test("doesn't throw") {
            try {
                Day12.part2()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
    }
})
