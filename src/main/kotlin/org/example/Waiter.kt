package org.example

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Waiter {
    private var continuation: Continuation<Unit>? = null
    suspend fun await() = suspendCoroutine { continuation ->
        this.continuation = continuation
    }

    fun resume() {
        continuation?.resume(Unit)
        continuation = null
    }
}