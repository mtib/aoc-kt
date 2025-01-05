package dev.mtib.aoc.aoc24.days

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigInteger

class Day13Test : FunSpec({
    val snippet0 = """
        Button A: X+94, Y+34
        Button B: X+22, Y+67
        Prize: X=8400, Y=5400
        
        Button A: X+26, Y+66
        Button B: X+67, Y+21
        Prize: X=12748, Y=12176
        
        Button A: X+17, Y+86
        Button B: X+84, Y+37
        Prize: X=7870, Y=6450
        
        Button A: X+69, Y+23
        Button B: X+27, Y+71
        Prize: X=18641, Y=10279
        """.trimIndent()
    context("part1") {
        test("doesn't throw") {
            try {
                Day13.part1()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
        test("example") {
            Day13.part1(snippet0).shouldBe(BigInteger("480"))
        }
    }
    context("part2") {
        test("doesn't throw") {
            try {
                Day13.part2()
            } catch (e: NotImplementedError) {
                // Ignore allowed exception
            }
        }
        test("complete") {
            Day13.part2(snippet0).shouldNotBe(BigInteger("0"))
        }
        context("successful") {
            test("example 2"){
                Day13.part2("""
                Button A: X+26, Y+66
                Button B: X+67, Y+21
                Prize: X=12748, Y=12176
                """.trimIndent()).let {
                    if (it !is BigInteger) {
                        throw AssertionError("Expected BigInteger, got $it")
                    }
                    it shouldNotBe BigInteger("0")
                    (it > BigInteger("100")) shouldBe true
                }
            }
            test("example 4"){
                Day13.part2("""
                Button A: X+69, Y+23
                Button B: X+27, Y+71
                Prize: X=18641, Y=10279
                """.trimIndent()).let {
                    if (it !is BigInteger) {
                        throw AssertionError("Expected BigInteger, got $it")
                    }
                    it shouldNotBe BigInteger("0")
                    (it > BigInteger("100")) shouldBe true
                }
            }
            test("constructed 1") {
                Day13.part2("""
                Button A: X+10000000000000, Y+0
                Button B: X+0, Y+10000000000000
                Prize: X=0, Y=0
                """.trimIndent()).let {
                    if (it !is BigInteger) {
                        throw AssertionError("Expected BigInteger, got $it")
                    }
                    it shouldBe BigInteger("4")
                }
            }
            test("constructed 2") {
                Day13.part2("""
                Button A: X+10000000000001, Y+0
                Button B: X+0, Y+10000000000002
                Prize: X=1, Y=2
                """.trimIndent()).let {
                    if (it !is BigInteger) {
                        throw AssertionError("Expected BigInteger, got $it")
                    }
                    it shouldBe BigInteger("4")
                }
            }
            test("constructed 3") {
                Day13.part2("""
                Button A: X+5000000000000, Y+0
                Button B: X+0, Y+5000000000000
                Prize: X=0, Y=0
                """.trimIndent()).let {
                    if (it !is BigInteger) {
                        throw AssertionError("Expected BigInteger, got $it")
                    }
                    it shouldBe BigInteger("8")
                }
            }
            test("constructed 4") {
                Day13.part2("""
                Button A: X+0, Y+0
                Button B: X+5000000000000, Y+5000000000000
                Prize: X=0, Y=0
                """.trimIndent()).let {
                    if (it !is BigInteger) {
                        throw AssertionError("Expected BigInteger, got $it")
                    }
                    it shouldBe BigInteger("0")
                }
            }
            test("constructed 5") {
                Day13.part2("""
                Button A: X+0, Y+0
                Button B: X+1, Y+1
                Prize: X=0, Y=0
                """.trimIndent()).let {
                    if (it !is BigInteger) {
                        throw AssertionError("Expected BigInteger, got $it")
                    }
                    it shouldBe BigInteger("0")
                }
            }
        }
        context("no match") {
            test("example 3"){
                Day13.part2("""
            Button A: X+17, Y+86
            Button B: X+84, Y+37
            Prize: X=7870, Y=6450
            """.trimIndent()) shouldBe BigInteger("0")
            }
            test("example 1"){
                Day13.part2("""
            Button A: X+94, Y+34
            Button B: X+22, Y+67
            Prize: X=8400, Y=5400
            """.trimIndent()) shouldBe BigInteger("0")
            }
        }
    }
})
