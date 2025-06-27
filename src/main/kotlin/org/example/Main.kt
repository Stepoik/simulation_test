package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking(Dispatchers.Main.immediate) {
    val aProd = ValueProducer(1f, scope = this, name = "a")
    val bProd = ValueProducer(2f, scope = this, name = "b")
    val cProd = ValueProducer(2f, scope = this, name = "c")

    val abJoin = ProducerJoin(this, bufferCapacity = 12f, name = "ab")
    abJoin.addProducer(aProd, "a")
    abJoin.addProducer(bProd, "b")

    val abcJoin = ProducerJoin(this, bufferCapacity = 12f, name = "abc")
    abcJoin.addProducer(abJoin, "ab")
    abcJoin.addProducer(cProd, "c")


    val consumer = Consumer(consumption = 6f, scope = this, consumptionDelay = 3000L, name = "d")
    consumer.setProducer(abcJoin)
    consumer.print()
    println("-----------------")
}

