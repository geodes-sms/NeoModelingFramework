package geodes.sms.nmf.loader.neo4j.io

import geodes.sms.nmf.neo4j.DBCredentials
import geodes.sms.nmf.neo4j.Values
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import geodes.sms.nmf.neo4j.io.IDHolder
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File


@TestInstance(TestInstance.Lifecycle.PER_CLASS) //Enables non-static BeforeAll and AfterAll
class BatchBufferCapacityTest {

    private val dbUri = "bolt://localhost:7687"
    private val username = "neo4j"
    private val password = "admin"
    private val graphWriter = GraphBatchWriter(DBCredentials(dbUri, username, password))

    @Test
    fun nodesTest() {
        graphWriter.clearDB()

        val resWriter = File("../TestResults/BatchBufferCapacityNodes.csv").bufferedWriter()
        resWriter.write("nodes count; timeMillis; timeSec;\n")
        val sizes = listOf(10, 5000, 6000, 7000, 8000, 9000, 10000, 11000, 12000, 13000, 14000,
            15000, 16000, 17000, 18000, 19000, 20000, 21000, 22000, 23000, 24000, 25000, 26000, 27000,
            28000, 29000, 30000, 35000, 40000, 45000, 50000, 60000, 65000, 70000, 75000, 80000,
            100000, 150000, 200000)

        for (i in sizes) {  //all possible sizes
            println("testing bufferSize: $i")

            //for (j in 1..20) {
            for (k in 0 until i) {
                graphWriter.createNode("TestNode", mapOf(
                    "int" to Values.value(10),
                    "string" to Values.value("test String"))
                )
            }

            val startTime = System.currentTimeMillis()
                graphWriter.saveNodes()    //calculate time for this function !!!
            val endTime = System.currentTimeMillis()

            val time = endTime - startTime
            val timeSec  = time.toDouble() / 1000
            resWriter.write("$i; $time; $timeSec; \n")
            println("time sec:  $timeSec\n")
        }
        resWriter.close()
        graphWriter.clearDB()
    }

    @Test
    fun refsTest() {
        graphWriter.clearDB()

        val resWriter = File("../TestResults/BatchBufferCapacityRefs.csv").bufferedWriter()
        resWriter.write("nodes count; timeMillis; timeSec;\n")
        val sizes = listOf(100, 1000, 3000, 5000, 6000, 7000, 8000, 9000, 10000, 11000, 12000, 13000, 14000, 15000,
            20000, 22000, 24000, 26000, 28000, 30000, 32000, 35000, 40000, 50000, 60000, 70000)
        val max = sizes.max()!!

        //create nodes
        val nodes1 = ArrayList<IDHolder>(max)
        val nodes2 = ArrayList<IDHolder>(max)
        (1..max).chunked(15000).forEach {
            for (i in 1..it.size) {
                nodes1.add(graphWriter.createNode("TestNode1", emptyMap()))
                nodes2.add(graphWriter.createNode("TestNode2", emptyMap()))
            }
            graphWriter.saveNodes()
        }

        //test refs
        for (i in sizes) {  //all possible sizes
            println("testing bufferSize: $i")

            for (k in 0 until i) {    //0..29, 0..49
                graphWriter.createRef(
                    type = "r", startID = nodes1[k].id, endID = nodes2[k].id)
            }

            val startTime = System.currentTimeMillis()
                graphWriter.saveRefs()    //calculate this function !!!
            val endTime = System.currentTimeMillis()

            val time = endTime - startTime
            val timeSec  = time.toDouble() / 1000
            resWriter.write("$i; $timeSec; \n")
            println("time sec:  $timeSec\n")
        }
        resWriter.close()
        graphWriter.clearDB()
    }


    @Test
    fun refsTest2() {
        graphWriter.clearDB()

        val buffersizes = listOf(1000, 3000, 5000, 6000, 7000, 8000, 9000, 10000, 11000, 12000, 13000, 14000, 15000,
            20000, 22000, 24000, 26000, 28000, 30000)

        val modelSizes = listOf(1000, 3000, 5000, 6000, 7000, 8000, 9000, 10000, 11000, 12000, 13000, 14000, 15000,
            20000, 22000, 24000, 26000, 28000, 30000, 32000, 35000, 40000, 50000, 55000, 60000)
            //val sizes = listOf(100, 500, 1000)
        val max = modelSizes.max()!!

        val resWriter = File("../TestResults/BatchBufferCapacityRefs.csv").bufferedWriter()
        resWriter.write(";")
        modelSizes.forEach { resWriter.write("$it; ") }

        //create nodes
        val nodes1 = ArrayList<IDHolder>(max)
        val nodes2 = ArrayList<IDHolder>(max)
        (1..max).chunked(15000).forEach {
            for (i in 1..it.size) {
                nodes1.add(graphWriter.createNode("TestNode1", emptyMap()))
                nodes2.add(graphWriter.createNode("TestNode2", emptyMap()))
            }
            graphWriter.saveNodes()
        }

        //cold start
        for (i in 1..2) {
            graphWriter.createRef(type = "r", startID = nodes1[0].id, endID = nodes2[0].id)
        }
        graphWriter.saveRefs()


        //test refs
        for (i in modelSizes) {  //all possible sizes; model size
            println("testing model Size: $i")
            resWriter.write("$i; ")

            for (j in buffersizes) {  //buffer size
                val chunks = (0 until i).chunked(j)

                val times = mutableListOf<Double>()
                for (l in 1..5) {

                    val startTime = System.currentTimeMillis()
                    chunks.forEach {
                        for (k in it) {
                            graphWriter.createRef(
                                type = "r",
                                startID = nodes1[k].id,
                                endID = nodes2[k].id
                            )
                        }
                        graphWriter.saveRefs()
                    }
                    val endTime = System.currentTimeMillis()

                    val time = endTime - startTime
                    val timeSec = time.toDouble() / 1000
                    times.add(timeSec)
                }
                val avg = times.average()

                resWriter.write("$avg; ")
                //print("$avg ")
            }
            println("\n")
            resWriter.write("\n")

        }
        resWriter.close()
        graphWriter.clearDB()
    }

    @AfterAll
    fun close() {
        graphWriter.close()
    }
}