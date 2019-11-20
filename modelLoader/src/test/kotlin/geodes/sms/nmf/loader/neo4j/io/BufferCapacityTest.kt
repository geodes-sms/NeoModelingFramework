package geodes.sms.nmf.loader.neo4j.io

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import geodes.sms.nmf.neo4j.io.Neo4jGraph
import geodes.sms.nmf.neo4j.io.INode
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BufferCapacityTest {

    private val graph = Neo4jGraph.create(jacksonObjectMapper().readValue(File("../Neo4jDBCredentials.json")))

    /**
     * To create 1 ref we must match or create startNode and endNode.
     * The worst case is when we need to match both for 1 ref creating.
     *  MATCH (a) WHERE ID(a)=...
     *  MATCH (b) WHERE ID(b)=...
     *  CREATE (a)--(b)
     * This test determines the most optimal number of references (i.e. buffer size for refs)
     * to be created at a time (with single query)
     */
    @Test
    fun referencesTest() {
        graph.clearDB()

        val resWriter = File("../TestResults/BufferCapacityRefs.csv").bufferedWriter()
        val sizes = listOf(25, 40, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325)
        val max = sizes.max()!!

        //create nodes
        val nodes1 = ArrayList<INode>(max)
        val nodes2 = ArrayList<INode>(max)
        for (n in 1..max) {
            val node1 = graph.createNode("A")
            nodes1.add(node1)
            node1.setProperty("a", 500)

            val node2 = graph.createNode("B")
            nodes2.add(node2)
        }
        graph.save()

        for (i in sizes) {  //all possible sizes; 30, 50, 70 ...
            println("testing bufferSize: $i")
            val times = mutableListOf<Long>()
            for (j in 1..30) {  //test attempts per size

                /* create refs.  2 query per ref creating: Match endNode; Create ref */
                for (k in 0 until i) {    //0..29, 0..49
                    graph.createRelation("r", nodes1[k], nodes2[k])
                }

                val startTime = System.currentTimeMillis()
                    graph.save()    //calculate this function !!!
                val endTime = System.currentTimeMillis()

                val time = endTime - startTime
                times.add(time)
                val timeSec = time.toDouble() / 1000
                resWriter.write("$i; $timeSec;\n")
                graph.clearDB()
            }
            val avgTime = times.reduce { acc, time -> acc + time } / times.size
            val avgTimeSec  = avgTime.toDouble() / 1000
            //resWriter.write("$i; $avgTimeSec; avg;\n")
            println("avg:  $avgTimeSec\n")
        }
        resWriter.close()
    }

    /**
     * This test determines the most optimal number of nodes (i.e. buffer size for nodes)
     * to be created at a time (with single query)
     */
    @Test
    fun nodesTest() {
        graph.clearDB()

        val resWriter = File("../TestResults/BufferCapacityNodes.csv").bufferedWriter()
        resWriter.write("nodes count; timeMillis; timeSec;\n")
        val sises = listOf(25, 40, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300,
            325, 350, 375, 400, 425, 450, 475, 500, 525, 550)

        for (i in sises) {  //all possible sizes; 30, 50, 70 ...
            println("testing bufferSize: $i")
            //val times = mutableListOf<Long>()
            graph.createNode("")

            val startTime = System.currentTimeMillis()
            graph.save()    //calculate this function !!!
            val endTime = System.currentTimeMillis()

            val time = endTime - startTime
            //times.add(time)
            val timeSec = time.toDouble() / 1000
            //resWriter.write("$i; $timeSec;\n")
            graph.clearDB()

            //val avgTime = times.reduce { acc, time -> acc + time } / times.size
            //val avgTimeSec  = avgTime.toDouble() / 1000
            resWriter.write("$i; $time; $timeSec; \n")
            println("avg:  $time\n")
        }
        resWriter.close()
    }

    @AfterAll
    fun close() {
        graph.close()
    }
}