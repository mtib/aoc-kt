package dev.mtib.aoc.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <T, R> Iterable<T>.chunkedParMap(chunkSize: Int, block: suspend (List<T>) -> R): List<R> = coroutineScope {
    chunked(chunkSize).map { chunk ->
        async {
            block(chunk)
        }
    }.awaitAll()
}
