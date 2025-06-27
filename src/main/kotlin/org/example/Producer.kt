package org.example

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Producer : Printable {
    val produced: StateFlow<Float> // Производимое значение

    val value: StateFlow<Float> // Реально потребляемое значение

    val producing: Flow<Float> // Флоу производимых в реальном времени

    fun onConsumerChanged(requiredProduced: Float)
}