import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.test.runTest
import org.example.Consumer
import org.example.ProducerJoin
import org.example.ValueProducer
import kotlin.test.Test

class ModelTest {
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `3 producers test`() = runTest {
        val aProd = ValueProducer(1f)
        val bProd = ValueProducer(2f)
        val cProd = ValueProducer(3f)

        val abJoin = ProducerJoin(GlobalScope)
        abJoin.addProducer(aProd, "a")
        abJoin.addProducer(bProd, "b")

        val abcJoin = ProducerJoin(GlobalScope)
        abcJoin.addProducer(abJoin, "ab")
        abcJoin.addProducer(cProd, "c")

        val consumer = Consumer(6f, GlobalScope)
        consumer.setProducer(abcJoin)

        consumer.value = 3f

        aProd.setProduced(3f)

        Thread.sleep(100)
        println(abJoin.producers["a"]!!.value.value)
        println(abJoin.producers["b"]!!.value.value)
        println(abcJoin.producers["c"]!!.value.value)
        println(abcJoin.producers["ab"]!!.value.value)
    }
}