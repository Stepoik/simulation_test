package org.example

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.min

class ValueProducer(
    initialValue: Float,
    val name: String
) : Producer {
    private val _produced = MutableStateFlow(initialValue)
    override val produced: StateFlow<Float> = _produced

    private val _value = MutableStateFlow(initialValue)
    override val value: StateFlow<Float>
        get() = _value

    override fun onConsumerChanged(requiredProduced: Float) {
        val realProducedValue = min(requiredProduced, _produced.value)
        _value.value = realProducedValue
    }

    override fun print(prefix: String) {
        println("$prefix$name(${value.value}, ${produced.value})")
    }

    fun setProduced(value: Float) {
        _produced.value = value
    }
}