package org.example

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

inline fun <reified T> simpleCombine(flows: List<Flow<T>>): Flow<List<T>> {
    return flow {
        val valueArray = Array<T?>(flows.size) { null }
        val counter = AtomicInteger(0)
        val updatedFlow = MutableStateFlow(0)
        coroutineScope {
            flows.forEachIndexed { index, flow ->
                launch {
                    var isFirst = true
                    flow.collect {
                        if (isFirst) {
                            isFirst = false
                            counter.incrementAndGet()
                        }
                        valueArray[index] = it
                        updatedFlow.value++
                    }
                }
            }
            updatedFlow.collect {
                if (counter.get() >= flows.size) {
                    emit(valueArray.filterNotNull())
                }
            }
        }
    }
}