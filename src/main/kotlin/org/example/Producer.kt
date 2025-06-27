package org.example

import kotlinx.coroutines.flow.StateFlow

interface Producer : Printable {
    val produced: StateFlow<Float>

    val value: StateFlow<Float>

    fun onConsumerChanged(requiredProduced: Float)
}