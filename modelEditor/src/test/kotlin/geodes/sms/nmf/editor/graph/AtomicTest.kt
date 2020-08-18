package geodes.sms.nmf.editor.graph

import geodes.sms.nmf.editor.graph.neo4jImpl.ModelManagerImpl
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AtomicTest {
    private val dbUri = "bolt://localhost:7687"
    private val username = "neo4j"
    private val password = "admin"
    private val manager = ModelManagerImpl(dbUri, username, password)

    
    @Test fun widthCreateTest() {
        val sizes = listOf(1000, 5000, 10000, 25000, 50000, 75000, 100000, 200000, 400000, 1000)

        //val sizes= listOf(10, 20, 30)
        val resWriter = File("../TestResultsDomains/Graph/Memory/CreateWidth.csv").bufferedWriter()
        val graph = manager.createGraph()

        for (i in sizes) {
            println("sizes: $i")

            val times = mutableListOf<Double>()
            for (k in 1..20) {    //repeat 20 times to callibrate
                val startTime = System.currentTimeMillis()
                for (j in 1..i) {
                    graph.addCompositeVertex()
                }

                //val startTime = System.currentTimeMillis()
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)

                //clear db
                manager.clearDB()
                manager.clearCache()
            }
            resWriter.write("$i; ${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @Test fun depthCreateTest() {
        val sizes = listOf(1000, 5000, 10000, 25000, 50000, 75000, 100000, 200000, 300000)

        //val sizes= listOf(10, 20, 30)
        val resWriter = File("../TestResultsDomains/Graph/Memory/CreateDepth.csv").bufferedWriter()
        val graph = manager.createGraph()

        for (i in sizes) {
            println("sizes: $i")

            val times = mutableListOf<Double>()
            for (k in 1..10) {    //repeat 20 times to calibrate
                val parents = LinkedList<CompositeVertex>()

                val startTime = System.currentTimeMillis()
                    parents.add(graph.addCompositeVertex())
                    for (j in 1..i) {
                        parents.add(parents.last.addCompositeVertex())
                    }
                    manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)

                //clear db
                manager.clearDB()
                manager.clearCache()
            }
            resWriter.write("$i; ${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @AfterAll fun close() { manager.close() }
}