package dev.mtib.aoc.aoc24.days

import io.kotest.core.spec.style.FunSpec
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class Day4Test: FunSpec({
    context("part1") {
        test("small") {
            Day4.withInput("""
                XMAS
                MAST
                XMAS
            """.trimIndent()) {
                assertEquals(2, Day4.part1())
            }
        }
        test("small 2") {
            Day4.withInput("""
                SAMX
            """.trimIndent()) {
                assertEquals(1, Day4.part1())
            }
        }
        test("small 3") {
            Day4.withInput("""
                S...
                .A..
                ..M.
                ...X
            """.trimIndent()) {
                assertEquals(1, Day4.part1())
            }
        }
        test("small 4") {
            Day4.withInput("""
                S..S
                .A.A
                ..MM
                SAMX
            """.trimIndent()) {
                assertEquals(3, Day4.part1())
            }
        }
        test("given") {
            Day4.withInput("""
                MMMSXXMASM
                MSAMXMSMSA
                AMXSXMAAMM
                MSAMASMSMX
                XMASAMXAMM
                XXAMMXXAMA
                SMSMSASXSS
                SAXAMASAAA
                MAMMMXMMMM
                MXMXAXMASX
            """.trimIndent()) {
                assertEquals(18, Day4.part1())
            }
        }
    }

    context("part2") {
        test("example") {
            Day4.withInput("""
                .M.S......
                ..A..MSMS.
                .M.S.MAA..
                ..A.ASMSM.
                .M.S.M....
                ..........
                S.S.S.S.S.
                .A.A.A.A..
                M.M.M.M.M.
                ..........            
            """.trimIndent()) {
                assertEquals(9, Day4.part2())
            }
        }
    }
})