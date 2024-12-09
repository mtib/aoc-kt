package dev.mtib.aoc.aoc24.days

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Day9Test : FunSpec({
    fun String.checksum(): Long = withIndex().sumOf { (i, c) -> if (c == '.') 0L else i * (c - '0').toLong() }
    val snippet0 = """
        2333133121414131402
    """.trimIndent()
    val snippet1 = """
        0..111....22222
    """.trimIndent()
    val snippet2 = """
        00...111...2...333.44.5555.6666.777.888899
    """.trimIndent()
    val snippet3 = """
        0..111....22222
        02.111....2222.
        022111....222..
        0221112...22...
        02211122..2....
        022111222......
    """.trimIndent()
    val snippet4 = """
        00...111...2...333.44.5555.6666.777.888899
        009..111...2...333.44.5555.6666.777.88889.
        0099.111...2...333.44.5555.6666.777.8888..
        00998111...2...333.44.5555.6666.777.888...
        009981118..2...333.44.5555.6666.777.88....
        0099811188.2...333.44.5555.6666.777.8.....
        009981118882...333.44.5555.6666.777.......
        0099811188827..333.44.5555.6666.77........
        00998111888277.333.44.5555.6666.7.........
        009981118882777333.44.5555.6666...........
        009981118882777333644.5555.666............
        00998111888277733364465555.66.............
        0099811188827773336446555566..............
    """.trimIndent()
    context("part1") {
        test("doesn't throw") {
            try {
                Day9.part1()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
        test("example") {
            Day9.withInput(snippet0) {
                Day9.part1().toString() shouldBe "1928"
            }
        }
        test("smaller example") {
            Day9.withInput("12345") {
                Day9.part1().toString() shouldBe "022111222".withIndex().sumOf { (i, c) -> i * (c - '0') }.toString()
            }
        }
        test("smaller example 2") {
            Day9.withInput("11") {
                Day9.part1().toString() shouldBe "0"
            }
        }
        test("smaller example 3") {
            Day9.withInput("11112") {
                // expands to "0.1.22" compresses to "0212"
                // sum is 0*0 + 1*2 + 2*1 = 4
                Day9.part1().toString() shouldBe "10"
            }
        }
        test("smaller example 4") {
            Day9.withInput("111") {
                // expands to "01"
                Day9.part1().toString() shouldBe "1"
            }
        }
        test("smaller example 5") {
            Day9.withInput("101") {
                // expands to "01"
                Day9.part1().toString() shouldBe "1"
            }
        }
        test("smaller example 6") {
            Day9.withInput("201") {
                // expands to "001"
                // sum is 0*1 + 1*0 + 2*1 = 2
                Day9.part1().toString() shouldBe "2"
            }
        }
    }
    context("part2") {
        test("doesn't throw") {
            try {
                Day9.part2()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
        test("example") {
            Day9.withInput(snippet0) {
                Day9.part2().toString() shouldBe "2858"
            }
        }
        test("smaller example 1") {
            Day9.withInput("11112") {
                // expands to "0.1.22" compresses to "01..22"
                // sum is 0*0 + 1*1 + 2*. + 3*. + 4*2 + 5*2 = 19
                Day9.part2().toString() shouldBe "19"
            }
        }
        test("smaller example 2") {
            Day9.withInput("12112") {
                // expands to "0..1.22" compresses to "0221"
                Day9.part2().toString() shouldBe "0221".checksum().toString()
            }
        }
        test("smaller example 3") {
            Day9.withInput("13112") {
                // expands to "0...1.22" compresses to "0221"
                // sum is 0*0 + 1*2 + 2*2 + 3*1 = 9
                Day9.part2().toString() shouldBe "0221".checksum().toString()
            }
        }
        test("smaller example 4") {
            Day9.withInput("13212") {
                // expands to "0...11.22" compresses to "022.11"
                // sum is 0*0 + 1*2 + 2*2 + 3*0 + 4*1 + 5*1 = 13
                Day9.part2().toString() shouldBe "022.11".checksum().toString()
            }
        }
        test("smaller example 5") {
            Day9.withInput("1321201") {
                // expands to "0...11.223" compresses to "032211"
                // sum is 0*0 + 1*3 + 2*2 + 3*2 + 4*1 + 5*1 = 22
                Day9.part2().toString() shouldBe "22"
            }
        }
    }
})
