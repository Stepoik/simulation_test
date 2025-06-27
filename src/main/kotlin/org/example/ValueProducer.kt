package org.example

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.min

class ValueProducer(
    initialValue: Float,
    private val scope: CoroutineScope,
    val name: String
) : Producer {
    private val _produced = MutableStateFlow(initialValue)
    override val produced: StateFlow<Float> = _produced

    private val _value = MutableStateFlow(initialValue)
    override val value: StateFlow<Float>
        get() = _value

    private val _producing = MutableSharedFlow<Float>()
    override val producing: Flow<Float>
        get() = _producing

    init {
        scope.launch(Dispatchers.Main.immediate) {
            while (isActive) {
                delay(PRODUCING_DELAY)
                _producing.emit(_value.value)
                println("$name produced: ${value.value}")
            }
        }
    }

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

    companion object {
        private const val PRODUCING_DELAY = 3000L
    }
}