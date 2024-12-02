package dev.mtib.aoc24

import dev.mtib.aoc24.util.AocLogger
import dev.mtib.aoc24.util.Results

private val logger = AocLogger.new {}
fun main() {
    logger.log { "verify tool will mark the last result as verified" }
    Results.verifyLast()
    logger.log { "done" }
}
