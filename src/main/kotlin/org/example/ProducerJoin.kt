package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProducerJoin(
    private val scope: CoroutineScope,
    val name: String
) : Producer {
    private var collectingJob: Job? = null
    private val _producers = mutableMapOf<String, Producer>()
    val producers: Map<String, Producer> get() = _producers

    private val _produced = MutableStateFlow(0f)
    override val produced: StateFlow<Float> = _produced

    private val _value = MutableStateFlow(0f)
    override val value: StateFlow<Float> = _value

    override fun onConsumerChanged(requiredProduced: Float) {
        val produced = _produced.value
        val ratio = requiredProduced / produced
        _producers.forEach { (_, producer) ->
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
        _producers[key] = producer
        updateSubscriptionJob()
    }

    private fun updateSubscriptionJob() {
        collectingJob?.cancel()
        collectingJob = scope.launch(Dispatchers.Main.immediate) {
            val producersList = _producers.values.toList()
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
        }
    }
}