package dev.mtib.aoc.aoc24.days

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Day6Test : FunSpec({

    test("part1") {
     Day6.withInput("""
      ....#.....
      .........#
      ..........
      ..#.......
      .......#..
      ..........
      .#..^.....
      ........#.
      #.........
      ......#...
     """.trimIndent()) {
      Day6.part1() shouldBe 41
     }
    }

    test("part2") {
     Day6.withInput("""
      ....#.....
      .........#
      ..........
      ..#.......
      .......#..
      ..........
      .#..^.....
      ........#.
      #.........
      ......#...
     """.trimIndent()) {
        Day6.part2() shouldBe 6
     }
    }

    test("part2 1") {
        Day6.withInput("""
      ........
      ..#.....
      ..^..#..
      ........
      ....#...
     """.trimIndent()) {
            Day6.part2() shouldBe 1
        }
    }

    test("part2 1 reversed") {
        Day6.withInput("""
      ..#.....
      ........
      ........
      .#...v..
      .....#..
      ........
     """.trimIndent()) {
            Day6.part2() shouldBe 1
        }
    }

    test("part2 2") {
        Day6.withInput("""
      ........
      ..#.....
      ...v.#..
      .#......
      ....#...
     """.trimIndent()) {
            Day6.part2() shouldBe 0
        }
    }

    test("part2 2 reversed") {
        Day6.withInput("""
      ........
      ..#.....
      ......#.
      ...v....
      .#......
      .....#..
      ........
     """.trimIndent()) {
            Day6.part2() shouldBe 0
        }
    }

    test("part2 3") {
        Day6.withInput("""
      ..#.....
      .....>#.
      .#......
      ........
      .....#..
      ........
     """.trimIndent()) {
            Day6.part2() shouldBe 2
        }
    }
})
