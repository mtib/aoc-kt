package dev.mtib.aoc.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <T, R> Iterable<T>.chunkedParMap(chunkSize: Int, block: suspend (List<T>) -> R): List<R> = coroutineScope {
    chunked(chunkSize.coerceAtLeast(1)).map { chunk ->
        async {
            block(chunk)
        }
    }.awaitAll()
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
