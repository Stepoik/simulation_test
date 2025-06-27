package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

class BufferCollector(
    internal val consumption: Float,
    private val collector: FlowCollector<Float>
) : FlowCollector<Float> {
    init {
        require(consumption > 0 || consumption == UNLIMITED)
    }

    override suspend fun emit(value: Float) {
        collector.emit(value)
    }

    companion object {
        const val UNLIMITED = -1f
    }
}

typealias BufferContinuation = () -> Unit

class BufferFlow(
    private val bufferCapacity: Float
) : Flow<Float> {
    private var buffered: Float = 0f

    private val continuations = LinkedList<BufferContinuation>()
    private val waiter = Waiter()

    override suspend fun collect(collector: FlowCollector<Float>) {
        val bufferCollector = (collector as? BufferCollector) ?: BufferCollector(BufferCollector.UNLIMITED, collector)

        coroutineScope {
            while (isActive) {
                if (buffered == 0f) {
                    waiter.await()
                }
                val emitedValue = if (bufferCollector.consumption == BufferCollector.UNLIMITED) {
                    buffered
                } else {
                    min(buffered, bufferCollector.consumption)
                }
                buffered -= emitedValue
                bufferCollector.emit(emitedValue)

                var consumedCount = 0
                continuations.toList().forEach {
                    it.invoke()
                    consumedCount++
                }
                repeat(consumedCount) {
                    continuations.removeFirst()
                }
            }
        }
    }

    suspend fun emit(value: Float) {
        var consumedValue = 0f
        while (value - consumedValue > CONSUMPTION_THRESHOLD) {
            val consumed = consumeValue(value - consumedValue)
            consumedValue += consumed
        }
    }

    private suspend fun consumeValue(value: Float): Float {
        return suspendCoroutine { cont ->
            if (buffered >= bufferCapacity) {
                continuations.add {
                    cont.resume(0f)
                }
                waiter.resume()
            } else {
                val additionValue = min(value, bufferCapacity - buffered)
                buffered += additionValue
                if (additionValue != value) {
                    continuations.add {
                        cont.resume(additionValue)
                    }
                    waiter.resume()
                } else {
                    waiter.resume()
                    cont.resume(value)
                }
            }
        }
    }

    companion object {
        private const val CONSUMPTION_THRESHOLD = 0.001f
    }
}

suspend fun Flow<Float>.bufferCollection(consumption: Float, collector: FlowCollector<Float>) {
    val bufferCollector = BufferCollector(consumption, collector)
    collect(bufferCollector)
}