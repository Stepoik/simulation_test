package org.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class Consumer(
    initialValue: Float,
    private val scope: CoroutineScope,
    val name: String
): Printable {
    private var producer: Producer? = null

    var value = initialValue
        private set

    fun setProducer(producer: Producer) {
        if (this.producer != null) return

        this.producer = producer

        scope.launch(Dispatchers.Main.immediate) {
            producer.produced.collect {
                producer.onConsumerChanged(value)
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