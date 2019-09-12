package org.jetbrains.kotlinconf

import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun <T> Flow<T>.wrap(): CFlow<T> = CFlow(this)

class CFlow<T>(origin: Flow<T>) : Flow<T> by origin {
    fun watch(block: (T) -> Unit): Closeable {
        val job = Job(ConferenceService.coroutineContext[Job])

        onEach {
            block(it)
        }.launchIn(CoroutineScope(dispatcher() + job))

        return object : Closeable {
            override fun close() {
                job.cancel()
            }
        }
    }
}