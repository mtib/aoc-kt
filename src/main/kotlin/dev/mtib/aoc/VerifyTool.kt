package dev.mtib.aoc

import dev.mtib.aoc.util.AocLogger
import dev.mtib.aoc.util.Results

private val logger = AocLogger.new {}
fun main() {
    logger.log(AocLogger.Main) { "verify tool will mark the last result as verified" }
    Results.verifyLast()
    logger.log(AocLogger.Main) { "done" }
}
