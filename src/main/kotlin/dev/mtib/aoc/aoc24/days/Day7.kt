package dev.mtib.aoc.aoc24.days

import dev.mtib.aoc.day.AocDay
import dev.mtib.aoc.util.chunkedParMap
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select
import java.math.BigInteger

object Day7: AocDay(2024, 7) {

    private suspend fun bruteForce(goal: BigInteger, parts: List<BigInteger>, current: BigInteger? = null, withPart2: Boolean = false, scope: CoroutineScope, channel: SendChannel<Unit>? = null): Boolean {
        if (current == null) {
            val channel = Channel<Unit>(Channel.BUFFERED)
            val work = scope.launch {
                bruteForce(
                    goal = goal,
                    parts = parts.subList(1, parts.size),
                    current = parts[0],
                    withPart2 = withPart2,
                    scope = scope,
                    channel = channel
                )
            }
            return select<Boolean> {
                channel.onReceive {
                    work.cancel()
                    true
                }
                work.onJoin {
                    false
                }
            }
        }

        if (current > goal) {
            return false
        }

        if (parts.isEmpty()) {
            if (current == goal) {
                channel?.send(Unit)
            }
            return current == goal
        }

        val jobs = buildSet<Deferred<Boolean>> {
             add(scope.async { bruteForce(goal, parts.subList(1, parts.size), current + parts[0], withPart2 = withPart2, scope = scope, channel = channel)})
             add(scope.async { bruteForce(goal, parts.subList(1, parts.size), current * parts[0], withPart2 = withPart2, scope = scope, channel = channel)})
            if (withPart2){
                add(scope.async { bruteForce(goal, parts.subList(1, parts.size), (current.toString() + parts[0].toString()).toBigInteger(), withPart2 = true, scope = scope, channel = channel)})
            }
        }.toMutableSet()

        while (jobs.isNotEmpty()) {
            select {
                jobs.forEach { job ->
                    job.onAwait { result ->
                        jobs.remove(job)
                        if (result) {
                            channel?.send(Unit)
                        }
                        result
                    }
                }
            }.let {
                if (it) {
                    jobs.forEach { it.cancel() }
                    return true
                }
            }
        }

        return false
    }

    private val parseRegex = Regex("""^(\d+):(?: (\d+))*""")
    override suspend fun part1(): Any = coroutineScope {
        inputLinesList.chunkedParMap(inputLinesList.size / cpu) {lines ->
            lines.map {
                val colon = it.indexOf(':')
                val goal = it.substring(0, colon).toBigInteger()
                val parts = it.substring(colon + 2).split(" ").map { it.toBigInteger() }
                goal to parts
            }.filter { bruteForce(it.first, it.second, scope = this@coroutineScope) }
                .map { it.first }
                .fold (BigInteger.ZERO) { acc, i -> acc + i }
        }.fold(BigInteger.ZERO) { acc, i -> acc + i }
    }

    override suspend fun part2() = coroutineScope {
        inputLinesList.chunkedParMap(inputLinesList.size / cpu) {lines ->
            lines.map {
                val colon = it.indexOf(':')
                val goal = it.substring(0, colon).toBigInteger()
                val parts = it.substring(colon + 2).split(" ").map { it.toBigInteger() }
                goal to parts
            }.filter { bruteForce(it.first, it.second, withPart2 = true, scope = this@coroutineScope) }
                .map { it.first }
                .fold (BigInteger.ZERO) { acc, i -> acc + i }
        }.fold(BigInteger.ZERO) { acc, i -> acc + i }
    }
}