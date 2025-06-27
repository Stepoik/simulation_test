package org.example

import kotlinx.coroutines.*
import kotlin.math.max
import kotlin.math.min

class Consumer(
    private val scope: CoroutineScope,
    private val consumption: Float,
    private val consumptionDelay: Long,
    val name: String
): Printable {
    private var producer: Producer? = null

    var value = consumption
        private set

    fun setProducer(producer: Producer) {
        if (this.producer != null) return

        this.producer = producer

        scope.launch(Dispatchers.Main.immediate) {
            launch {
                producer.produced.collect {
                    producer.onConsumerChanged(value)
                }
            }

            launch {
                producer.producing.bufferCollection(consumption) {
                    println("Consumed $it")
                    delay(consumptionDelay)
                }
            }
        }
    }

    fun updateValue(newValue: Float) {
        value = newValue
        producer?.onConsumerChanged(value)
    }

    override fun print(prefix: String) {
        println("$name($value)")
        producer?.print("└── ")
    }
}