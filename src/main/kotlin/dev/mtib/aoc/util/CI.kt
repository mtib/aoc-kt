package dev.mtib.aoc.util

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import dev.mtib.aoc.util.AocLogger.Companion.resultTheme
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

fun styleResult(code: String): String {
    return resultTheme(if (System.getenv("CI") == null) code else "*".repeat(code.length))
}

suspend inline fun <T> ciTimeout(
    puzzle: PuzzleIdentity,
    timeout: Duration,
    crossinline block: suspend () -> T
): Option<T> {
    return try {
        if (System.getenv("CI") != null) {
            withTimeout(timeout) {
                block()
            }
        } else {
            block()
        }.some()
    } catch (e: TimeoutCancellationException) {
        AocLogger.new { }
            .error(e, year = puzzle.year, day = puzzle.day, part = puzzle.part) { "timeout after $timeout" }
        None
    }
}
