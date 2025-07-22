package evaluation

import geodes.sms.nmf.editor.graph.CompositeVertex
import geodes.sms.nmf.editor.graph.Graph
import geodes.sms.nmf.editor.graph.Vertex
import geodes.sms.nmf.editor.graph.VertexType
import geodes.sms.nmf.editor.graph.neo4jImpl.ModelManagerImpl
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

// test file to evaluate RQ2
//  To what extent can our approach perform CRUD operations on metamodels and models of different domains, complexity and sizes?
// metrics (time and memory consumed when performing each operation for different configurations)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RQ2Eval {
    private val dbUri = DBCredentials.dbUri
    private val username = DBCredentials.username
    private val password = DBCredentials.password
    private val manager = ModelManagerImpl(dbUri, username, password)
    private val sizesDebug = listOf( // only for debugging
        10, 50, 100, 500, 1000, 5000, 10000, 50000, 100000
    )
    private val sizesEval = listOf( // for the evaluation
        10, 50, 100, 500, 1000, 5000, 10000, 50000, 100000,500000, 1000000,5000000, 10000000,50000000
    )

    var sizes = sizesDebug;
    val isEval = false // to be set in case eval data needs to be collected
    val maxSize = sizes[sizes.size-1]
    @BeforeEach
    fun beforeEach() {
        manager.clearCache()
        manager.clearDB()
        if (isEval)
            sizes = sizesEval
    }
    private val evalCount = 2 // we run the evaluation multiple times to mitigate threats
    // ---------------------------- CREATE ---------------------------- //
    @Test fun createSingle() {
        val resWriter = getFile("CreateSingle")
        for (i in sizes) {
            var mem: Long = 0;
            val startTime = System.currentTimeMillis()
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            for (j in 1..i) {
                manager.createVertex()
            }
            manager.saveChanges()
            mem = getUsedMemoryKB() - beforeMemory
            val endTime = System.currentTimeMillis()
            val time = endTime - startTime
            manager.clearDB()
            resWriter.appendText("$i,$time,$mem\n")
        }
    }

    @Test fun createContainmentWidth() {
        val resWriter = getFile("CreateContainmentWidth")
        for (i in sizes) {
            val graph = manager.createGraph()
            var mem: Long = 0;
            val startTime = System.currentTimeMillis()
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            for (j in 1..i) {
                graph.addVertices(VertexType.CompositeVertex)
            }
            manager.saveChanges()
            mem = getUsedMemoryKB() - beforeMemory
            val endTime = System.currentTimeMillis()
            val time = endTime - startTime

            manager.clearDB()
            resWriter.appendText("$i,$time,$mem\n")
        }
    }
    //
    @Test fun createContainmentDepth() {
        val resWriter = getFile("CreateContainmentDepth")
        for (i in sizes) {
            val graph = manager.createGraph()
            var lastVertex = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex
            var mem: Long = 0;
            val startTime = System.currentTimeMillis()
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            for (j in 2..i) {
                lastVertex = lastVertex.addSub_vertices(VertexType.CompositeVertex) as CompositeVertex
            }
            manager.saveChanges()
            mem = getUsedMemoryKB() - beforeMemory
            val endTime = System.currentTimeMillis()
            val time = endTime - startTime
            //clear db
            manager.clearDB()
            resWriter.appendText("$i,$time,$mem\n")
        }

    }

    @Test fun createCrossRef() {
        val resWriter = getFile("CreateCrossRef")
        for (i in sizes) {
            //----- preparation step -----
            val cv1 = manager.createCompositeVertex()
            val cv2 = manager.createCompositeVertex()
            manager.saveChanges()
            //--- preparation step end ----

            var mem: Long = 0;
            val startTime = System.currentTimeMillis()
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            for (j in 1..i) {
                cv1.setEdge(cv2)
            }
            manager.saveChanges()
            mem = getUsedMemoryKB() - beforeMemory
            val endTime = System.currentTimeMillis()
            val time = endTime - startTime
            //clear db
            manager.clearDB()
            resWriter.appendText("$i,$time,$mem\n")
        }
    }

    // ---------------------------- READ ---------------------------- //
    @Test fun read() {
        val resWriter = getFile("Read")
        //----- preparation step -----
        for (j in 1..maxSize) {
            manager.createCompositeVertex()
        }
        manager.saveChanges()
        manager.clearCache()
        //--- preparation step end ----
        for (i in sizes) {
            var mem: Long = 0;
            val startTime = System.currentTimeMillis()
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            manager.loadCompositeVertexList(i)
            mem = getUsedMemoryKB() - beforeMemory
            val endTime = System.currentTimeMillis()
            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
        //clear db
        manager.clearDB()
    }

    @Test fun readContainmentWidth() {
        val resWriter = getFile("ReadContainmentWidth")
        //----- preparation step -----
        generateContainmentWidthGraph()
        //--- preparation step end ----
        for (i in sizes) {
            var mem: Long = 0;
            val startTime = System.currentTimeMillis()
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val vertices = manager.loadCompositeVertexList(i)
            mem = getUsedMemoryKB() - beforeMemory
            val endTime = System.currentTimeMillis()
            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
        //clear db
        manager.clearDB()
    }

    @Test fun readContainmentDepth() {
        val resWriter = getFile("ReadContainmentDepth")
        //----- preparation step -----
        val graph = manager.createGraph()
        var lastVertex = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex
        for (j in 1..maxSize) {
            lastVertex = lastVertex.addSub_vertices(VertexType.CompositeVertex) as CompositeVertex
        }
        manager.saveChanges()
        manager.clearCache()
        //--- preparation step end ----
        for (i in sizes) {
            var mem: Long = 0;
            val startTime = System.currentTimeMillis()
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val vertices = manager.loadCompositeVertexList(i)
            mem = getUsedMemoryKB() - beforeMemory
            val endTime = System.currentTimeMillis()
            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
        //clear db
        manager.clearDB()
    }

    @Test fun readCrossRef() {
        val resWriter = getFile("ReadCrossRef")
        //----- preparation step -----
        for (j in 1..maxSize) {
            val cv1 = manager.createCompositeVertex()
            val cv2 = manager.createCompositeVertex()
            cv1.setEdge(cv2)
        }
        manager.saveChanges()
        manager.clearCache()
        //--- preparation step end ----
        for (i in sizes) {
            var mem: Long = 0;
            val startTime = System.currentTimeMillis()
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val vertices = manager.loadCompositeVertexList(i)
            mem = getUsedMemoryKB() - beforeMemory
            val endTime = System.currentTimeMillis()
            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
        manager.clearDB()
    }

    // ---------------------------- UPDATE ---------------------------- //
    @Test fun update() {
        val resWriter = getFile("Update")
        //----- preparation step -----
        val vertices = ArrayList<CompositeVertex>(maxSize)
        for (i in 1..maxSize) {
            val vertex = manager.createCompositeVertex()
            vertex.setCapacity(Random(42).nextInt(10)) // random value (seed makes sure it is always the same for all runs)
            vertex.setIs_initial(true)
            vertex.setName("x$i")
            vertices.add(vertex)
        }
        manager.saveChanges()
        //---- preparation step end ----
        for (i in sizes) {
            var mem: Long = 0;
            val startTime = System.currentTimeMillis()
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            for (j in 0 until i) {
                val vertex = vertices[j]
                vertex.setCapacity(-1 * vertex.getCapacity()!!)
                vertex.setIs_initial(!vertex.getIs_initial()!!) //change boolean to opposite value
                vertex.setName("y$j")
            }
            manager.saveChanges()
            mem = getUsedMemoryKB() - beforeMemory
            val endTime = System.currentTimeMillis()
            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
        manager.clearDB()
    }

    // ---------------------------- DELETE ---------------------------- //
    @Test fun deleteSingle() {
        val resWriter = getFile("DeleteSingle")
        //----- preparation step -----
        val vertices = LinkedList<CompositeVertex>()
        for (j in 1..maxSize*2) {
            vertices.add(manager.createCompositeVertex())
        }
        manager.saveChanges()
        //--- preparation step end ----
        for (i in sizes) {
            var mem: Long = 0;
            val startTime = System.currentTimeMillis()
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            for (j in 1..i) {
                manager.remove(vertices.pop()) // TODO bug with maxSize delete

            }
            manager.saveChanges()
            mem = getUsedMemoryKB() - beforeMemory
            val endTime = System.currentTimeMillis()
            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
        manager.clearDB() // not needed since the remove will delete everything
    }

//    @Test fun deleteContainmentWidth() {
//        val resWriter = getFile("DeleteContainmentWidth")
//        for (i in sizes) {
//            //----- preparation step -----
//            val graph = generateContainmentWidthGraph()
//            val vertices:LinkedList<Vertex> = manager.loadVertexList(maxSize) as LinkedList<Vertex>
//            //--- preparation step end ----
//            var mem: Long = 0;
//            val startTime = System.currentTimeMillis()
//            garbageCollector()
//            val beforeMemory = getUsedMemoryKB()
//            for (j in 1..i) {
//                graph.unsetVertices(vertices.pop())
//            }
//            manager.saveChanges()
//            mem = getUsedMemoryKB() - beforeMemory
//            val endTime = System.currentTimeMillis()
//            val time = endTime - startTime
//            resWriter.appendText("$i,$time,$mem\n")
//        }
//        println("end")
//    }

    //    @Test fun removeContainmentsDepthTest() {
//        val resWriter = File(resDirectory,"RemoveContainmentsDepth.csv").bufferedWriter()
//        for (i in sizes) {
//            val times = mutableListOf<Double>()
//            for (k in 1..calibration) {
//                //----- preparation step -----
//                val graph = manager.createGraph()
//                var lastVertex = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex
//                for (j in 2..i) {
//                    val cv = lastVertex.addSub_vertices(VertexType.CompositeVertex) as CompositeVertex
//                    manager.unload(lastVertex)
//                    lastVertex = cv
//                }
//                manager.saveChanges()
//                //--- preparation step end ----
//
//                val cv = graph.getVertices(1)[0]   //find first vertex
//                val startTime = System.currentTimeMillis()
//                graph.unsetVertices(cv)
//                manager.saveChanges()
//                val endTime = System.currentTimeMillis()
//                times.add((endTime - startTime).toDouble() / 1000)
//            }
//            resWriter.write("$i;${times.average()}\n")
//            resWriter.flush()
//        }
//        resWriter.close()
//    }
//
//    @Test fun removeCrossReferenceTest() {
//        val resWriter = File(resDirectory,"RemoveCrossRef.csv").bufferedWriter()
//        //----- preparation step1 -----
//        val cv1 = manager.createCompositeVertex()
//        val cv2 = manager.createCompositeVertex()
//        manager.saveChanges()
//        //--- preparation step1 end ----
//
//        for (i in sizes) {
//            val times = mutableListOf<Double>()
//            for (k in 1..calibration) {
//                //--- preparation step2 ----
//                for (j in 1..(i + 1)) {
//                    cv1.setEdge(cv2)
//                }
//                manager.saveChanges()
//                //--- preparation step2 end ----
//
//                val startTime = System.currentTimeMillis()
//                for (j in 1..i) {
//                    cv1.unsetEdge(cv2)
//                }
//                manager.saveChanges()
//                val endTime = System.currentTimeMillis()
//                times.add((endTime - startTime).toDouble() / 1000)
//            }
//            resWriter.write("$i;${times.average()}\n")
//            resWriter.flush()
//        }
//        resWriter.close()
//    }
//
//
    private fun generateContainmentWidthGraph() : Graph{
        val graph = manager.createGraph()
        for (j in 1..maxSize) {
            graph.addVertices(VertexType.CompositeVertex)
        }
        manager.saveChanges()
        manager.clearCache()
        return graph
    }
    @AfterAll fun close() {
        manager.clearDB()
        manager.clearCache()
        manager.close()
    }

    fun getFile(operationName: String): File {
        // create csv file to store the results
        val resFile = File("../ECMFA-2026-Evaluation/results/RQ2/${operationName}/${operationName}_run_$evalCount.csv")
        resFile.writeText("") // clear file in case it existed before
        resFile.appendText("element_count,time,mem\n")
        println("file created: ${resFile.name}")
        return resFile
    }


    fun getUsedMemoryKB(): Long {
        // return memory used in KB
        val runtime = Runtime.getRuntime()
        val usedBytes = runtime.totalMemory() - runtime.freeMemory()
        return usedBytes / 1024 // memory is in KB
    }

    fun garbageCollector() {
        System.gc()
        Thread.sleep(100)
    }
}