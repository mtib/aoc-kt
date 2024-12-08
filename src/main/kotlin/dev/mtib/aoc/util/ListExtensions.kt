package dev.mtib.aoc.util

import kotlinx.coroutines.*

suspend fun <T, R> Iterable<T>.chunkedParMap(chunkSize: Int, block: suspend (List<T>) -> R): List<R> = coroutineScope {
    chunked(chunkSize.coerceAtLeast(1)).map { chunk ->
        async {
            block(chunk)
        }
    }.awaitAll()
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
