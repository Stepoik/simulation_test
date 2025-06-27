package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProducerJoin(
    private val scope: CoroutineScope,
    bufferCapacity: Float,
    val name: String
) : Producer {
    private val _producers = MutableStateFlow<Map<String, Producer>>(mapOf())
    val producers: Map<String, Producer> get() = _producers.value

    private val _produced = MutableStateFlow(0f)
    override val produced: StateFlow<Float> = _produced

    private val _value = MutableStateFlow(0f)
    override val value: StateFlow<Float> = _value

    private val _producing = BufferFlow(bufferCapacity)
    override val producing: Flow<Float> = _producing

    init {
        subscribeOnProducers()
    }

    override fun onConsumerChanged(requiredProduced: Float) {
        val produced = _produced.value
        val ratio = requiredProduced / produced
        _producers.value.forEach { (_, producer) ->
            producer.onConsumerChanged(producer.produced.value * ratio)
        }
    }

    override fun print(prefix: String) {
        println("$prefix$name(${value.value}, ${produced.value})")
        val spacers = prefix.replace("└──", "   ").replace("├──", "│  ")
        val values = producers.values.toList()
        values.forEachIndexed { index, producer ->
            val newPrefix = if (index == values.lastIndex) "$spacers└── " else "$spacers├── "
            producer.print(newPrefix)
        }
    }

    fun addProducer(producer: Producer, key: String) {
        _producers.update { it + (key to producer) }
    }

    private fun subscribeOnProducers() {
        scope.launch {
            var collectionJob: Job? = null
            _producers.collect { producers ->
                collectionJob?.cancel()
                val producersList = producers.values.toList()
                collectionJob = launch {
                    launch {
                        simpleCombine(producersList.map { it.produced }).collect {
                            _produced.emit(it.sum())
                        }
                    }
                    launch {
                        simpleCombine(producersList.map { it.value }).collect {
                            _value.emit(it.sum())
                        }
                    }

                    producersList.map { it.producing }.merge().collect {
                        _producing.emit(it)
                    }
                }
            }
        }
    }
}