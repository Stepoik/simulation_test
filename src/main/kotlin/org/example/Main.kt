package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking(Dispatchers.Main.immediate) {
    val aProd = ValueProducer(1f, name = "a")
    val bProd = ValueProducer(2f, name = "b")
    val cProd = ValueProducer(3f, name = "c")

    val abJoin = ProducerJoin(this, name = "ab")
    abJoin.addProducer(aProd, "a")
    abJoin.addProducer(bProd, "b")

    val abcJoin = ProducerJoin(this, name = "abc")
    abcJoin.addProducer(abJoin, "ab")
    abcJoin.addProducer(cProd, "c")


    val consumer = Consumer(6f, this, name = "d")
    consumer.setProducer(abcJoin)
    consumer.print()
    println("-----------------")

    consumer.updateValue( 3f)
    consumer.print()
    println("-----------------")

    aProd.setProduced(3f)
    consumer.print()
    println("-----------------")

    consumer.updateValue( 5f)
    consumer.print()
}

