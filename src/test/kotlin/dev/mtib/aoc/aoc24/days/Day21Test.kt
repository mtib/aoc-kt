package dev.mtib.aoc.aoc24.days

import io.kotest.core.spec.style.FunSpec

class Day21Test : FunSpec({
    val snippet0 = """
        +---+---+---+
        | 7 | 8 | 9 |
        +---+---+---+
        | 4 | 5 | 6 |
        +---+---+---+
        | 1 | 2 | 3 |
        +---+---+---+
            | 0 | A |
            +---+---+
    """.trimIndent()
    val snippet1 = """
            +---+---+
            | ^ | A |
        +---+---+---+
        | < | v | > |
        +---+---+---+
    """.trimIndent()
    val snippet2 = """
        <vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A
        v<<A>>^A<A>AvA<^AA>A<vAAA>^A
        <A^A>^^AvvvA
        029A
    """.trimIndent()
    val snippet3 = """
        029A
        980A
        179A
        456A
        379A
    """.trimIndent()
    val snippet4 = """
        029A: <vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A
        980A: <v<A>>^AAAvA^A<vA<AA>>^AvAA<^A>A<v<A>A>^AAAvA<^A>A<vA>^A<A>A
        179A: <v<A>>^A<vA<A>>^AAvAA<^A>A<v<A>>^AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
        456A: <v<A>>^AA<vA<A>>^AAvAA<^A>A<vA>^A<A>A<vA>^A<A>A<v<A>A>^AAvA<^A>A
        379A: <v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
    """.trimIndent()
    context("part1") {
        test("doesn't throw") {
            try {
                Day21.part1()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
    }
    context("part2") {
        test("doesn't throw") {
            try {
                Day21.part2()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
    }
})
