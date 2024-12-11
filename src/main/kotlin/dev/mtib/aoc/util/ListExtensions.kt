package dev.mtib.aoc.util

import kotlinx.coroutines.*

suspend fun <T, R> Iterable<T>.chunkedParMap(chunkSize: Int, block: suspend (List<T>) -> R): List<R> = coroutineScope {
    chunked(chunkSize.coerceAtLeast(1)).map { chunk ->
        async {
            block(chunk)
        }
    }.awaitAll()
}

suspend fun <T, R> List<T>.chunkedParMapZ(chunkSize: Int, block: suspend (List<T>) -> R): List<R> = coroutineScope {
    val awaits = mutableListOf<Deferred<R>>()
    val actualChunkSize = chunkSize.coerceAtLeast(1)
    var startIndex = 0
    while (startIndex <= size) {
        val slice = subList(startIndex, (startIndex + actualChunkSize).coerceAtMost(size))
        async {
            block(slice)
        }.also { awaits.add(it) }
        startIndex += actualChunkSize
    }
    return@coroutineScope awaits.awaitAll()
}

suspend fun <T> List<T>.chunkedParLaunchZ(chunkSize: Int, block: suspend (List<T>) -> Unit): List<Job> = coroutineScope {
    val jobs = mutableListOf<Job>()
    val actualChunkSize = chunkSize.coerceAtLeast(1)
    var startIndex = 0
    while (startIndex <= size) {
        val slice = subList(startIndex, (startIndex + actualChunkSize).coerceAtMost(size))
        launch {
            block(slice)
        }.also { jobs.add(it) }
        startIndex += actualChunkSize
    }
    return@coroutineScope jobs
}

suspend fun <T> Iterable<T>.chunkedParLaunch(chunkSize: Int, block: suspend (List<T>) -> Unit): List<Job> = coroutineScope {
    chunked(chunkSize.coerceAtLeast(1)).map { chunk ->
        launch {
            block(chunk)
        }
    }
}

/**
 * This is stateless and terminal.
 */
suspend fun <T, R> Sequence<T>.chunkedParMap(chunkSize: Int, block: suspend (List<T>) -> R): List<R> = coroutineScope {
    chunked(chunkSize.coerceAtLeast(1)).map { chunk ->
        async {
            block(chunk)
        }
    }.toList().awaitAll()
}
