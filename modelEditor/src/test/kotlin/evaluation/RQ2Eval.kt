package evaluation

import geodes.sms.nmf.editor.graph.CompositeVertex
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

    @BeforeEach
    fun beforeEach() {
        manager.clearCache()
        manager.clearDB()
        if (isEval)
            sizes = sizesEval
    }
    private val evalCount = 5 // we run the evaluation multiple times to mitigate threats
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

    @Test fun createContainmentsWidthTest() {
        val resWriter =  getFile("CreateContainmentsWidth")
        for (i in sizes) {
            val times = mutableListOf<Long>()
            var mem: Long = 0;
            val graph = manager.createGraph()
            val startTime = System.currentTimeMillis()
            val beforeMemory = getUsedMemoryKB()
            for (j in 1..i) {
                graph.addVertices(VertexType.CompositeVertex)
            }
            manager.saveChanges()
            mem = getUsedMemoryKB() - beforeMemory
            val endTime = System.currentTimeMillis()
            times.add(endTime - startTime)

            manager.clearDB()

            val timeResults = "${times.average()},${times.minOrNull()},${times.maxOrNull()}"
            resWriter.appendText("$i,$timeResults,$mem\n")
        }
    }
//
//    @Test fun createContainmentsDepthTest() {
//        val resWriter = File(resDirectory,"CreateContainmentsDepth.csv").bufferedWriter()
//        for (i in sizes) {
//            val times = mutableListOf<Double>()
//            for (k in 1..calibration) {
//                val graph = manager.createGraph()
//                var lastVertex = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex
//
//                val startTime = System.currentTimeMillis()
//                for (j in 2..i) {
//                    lastVertex = lastVertex.addSub_vertices(VertexType.CompositeVertex) as CompositeVertex
//                }
//                manager.saveChanges()
//                val endTime = System.currentTimeMillis()
//                times.add((endTime - startTime).toDouble() / 1000)
//
//                //clear db
//                manager.clearDB()
//                manager.clearCache()
//            }
//            resWriter.write("$i;${times.average()}\n")
//            resWriter.flush()
//        }
//        resWriter.close()
//    }
//
//    @Test fun createCrossRefTest() {
//        val resWriter = File(resDirectory,"CreateCrossRef.csv").bufferedWriter()
//        for (i in sizes) {
//            val times = mutableListOf<Double>()
//            for (k in 1..calibration) {
//                //----- preparation step -----
//                val cv1 = manager.createCompositeVertex()
//                val cv2 = manager.createCompositeVertex()
//                manager.saveChanges()
//                //--- preparation step end ----
//
//                val startTime = System.currentTimeMillis()
//                for (j in 1..i) {
//                    cv1.setEdge(cv2)
//                }
//                manager.saveChanges()
//                val endTime = System.currentTimeMillis()
//                times.add((endTime - startTime).toDouble() / 1000)
//
//                //clear db
//                manager.clearDB()
//                manager.clearCache()
//            }
//            resWriter.write("$i;${times.average()}\n")
//            resWriter.flush()
//        }
//        resWriter.close()
//    }
//
//    // ---------------------------- UPDATE ---------------------------- //
//    @Test fun updateTest() {
//        val resWriter = File(resDirectory,"Update.csv").bufferedWriter()
//        //----- preparation step -----
//        val vertices = ArrayList<CompositeVertex>(maxSize)
//        for (i in 1..maxSize) {
//            val vertex = manager.createCompositeVertex()
//            vertex.setCapacity(7)
//            vertex.setIs_initial(true)
//            vertex.setName("some name")
//            vertices.add(vertex)
//        }
//        manager.saveChanges()
//        //---- preparation step end ----
//
//        for (i in sizes) {
//            val times = mutableListOf<Double>()
//            for (k in 1..calibration) {
//                val startTime = System.currentTimeMillis()
//                for (j in 0 until i) {
//                    val vertex = vertices[j]
//                    vertex.setCapacity(-1 * vertex.getCapacity()!!)
//                    vertex.setIs_initial(!vertex.getIs_initial()!!) //change boolean to opposite value
//                    //vertex.setName("qwerty")
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
//    @Test fun updateUniqueTest() {
//        val resWriter = File(resDirectory,"UpdateUnique.csv").bufferedWriter()
//        //----- preparation step -----
//        val vertices = ArrayList<CompositeVertex>(maxSize)
//        for (i in 1..maxSize) {
//            val compositeVertex = manager.createCompositeVertex()
//            compositeVertex.setId(-i)
//            vertices.add(compositeVertex)
//        }
//        manager.saveChanges()
//        //--- preparation step end ----
//
//        for (i in sizes) {
//            val times = mutableListOf<Double>()
//            for (k in 1..calibration) {
//                val startTime = System.currentTimeMillis()
//                for (j in 0 until i) {
//                    val v = vertices[j]
//                    v.setId(v.getId()!! * -1)
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
//    // ---------------------------- REMOVE ---------------------------- //
//    @Test fun removeSingleTest() {
//        val resWriter = File(resDirectory,"RemoveSingle.csv").bufferedWriter()
//        for (i in sizes) {
//            //----- preparation step -----
//            val vertices = LinkedList<CompositeVertex>()
//            for (j in 1..(i*calibration)) {
//                vertices.add(manager.createCompositeVertex())
//            }
//            manager.saveChanges()
//            //--- preparation step end ----
//
//            val times = mutableListOf<Double>()
//            for (k in 1..calibration) {
//                val startTime = System.currentTimeMillis()
//                for (j in 1..i) {
//                    manager.remove(vertices.pop())
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
//    @Test fun removeContainmentsWidthTest() {
//        val resWriter = File(resDirectory,"RemoveContainmentsWidth.csv").bufferedWriter()
//        for (i in sizes) {
//            //----- preparation step -----
//            val vertices = LinkedList<Vertex>()
//            val graph = manager.createGraph()
//            for (j in 1..(i*calibration)) {
//                vertices.add(graph.addVertices(VertexType.Vertex))
//            }
//            manager.saveChanges()
//            //--- preparation step end ----
//
//            val times = mutableListOf<Double>()
//            for (k in 1..calibration) {
//                val startTime = System.currentTimeMillis()
//                for (j in 1..i) {
//                    graph.unsetVertices(vertices.pop())
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
//    // ---------------------------- READ ---------------------------- //
//    @Test fun readContainmentsWidthTest() {
//        val resWriter = File(resDirectory,"ReadContainmentsWidth.csv").bufferedWriter()
//        //----- preparation step -----
//        val graph = manager.createGraph()
//        for (j in 1..maxSize) {
//            graph.addVertices(VertexType.Vertex)
//        }
//        manager.saveChanges()
//        manager.clearCache()
//        //--- preparation step end ----
//
//        for (i in sizes) {
//            val times = mutableListOf<Double>()
//            for (k in 1..calibration) {
//                val graphLoaded = manager.loadGraphById(graph._id)
//                val startTime = System.currentTimeMillis()
//                graphLoaded.getVertices(i)
//                val endTime = System.currentTimeMillis()
//                times.add((endTime - startTime).toDouble() / 1000)
//                manager.clearCache()
//            }
//            resWriter.write("$i;${times.average()}\n")
//            resWriter.flush()
//        }
//        resWriter.close()
//    }
//
//    @Test fun readByLabelTest() {
//        val resWriter = File(resDirectory,"ReadByLabel.csv").bufferedWriter()
//        //----- preparation step -----
//        for (j in 1..maxSize) {
//            manager.createCompositeVertex()
//        }
//        manager.saveChanges()
//        manager.clearCache()
//        //--- preparation step end ----
//
//        for (i in sizes) {
//            val times = mutableListOf<Double>()
//            for (k in 1..calibration) {
//                val startTime = System.currentTimeMillis()
//                manager.loadCompositeVertexList(i)
//                val endTime = System.currentTimeMillis()
//                times.add((endTime - startTime).toDouble() / 1000)
//                manager.clearCache()
//            }
//            resWriter.write("$i;${times.average()}\n")
//            resWriter.flush()
//        }
//        resWriter.close()
//    }

    @AfterAll fun close() {
        manager.clearDB()
        manager.clearCache()
        manager.close()
    }

    fun getFile(fileName: String): File {
        // create csv file to store the results
        val resFile = File("../ECMFA-2026-Evaluation/results/RQ2/${fileName}_run_$evalCount.csv")
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