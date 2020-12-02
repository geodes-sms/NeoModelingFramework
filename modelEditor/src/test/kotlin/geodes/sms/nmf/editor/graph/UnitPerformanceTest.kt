package geodes.sms.nmf.editor.graph

import geodes.sms.nmf.editor.graph.neo4jImpl.ModelManagerImpl
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UnitPerformanceTest {
    private val dbUri = "bolt://localhost:7687"
    private val username = "neo4j"
    private val password = "admin"
    private val manager = ModelManagerImpl(dbUri, username, password)
    private val resDirectory = File("../TestResults/graph/")
//    private val sizes = listOf(5, 10, 30)
    private val sizes = listOf(
        10, 100, 1000, 5000, 10000, 15000, 20000, 30000, 40000, 60000, 80000, 100000,
        120000, 140000, 160000, 180000, 200000, 225000, 250000, 275000, 300000,
        325000, 350000, 375000, 400000
    )
    private val maxSize = sizes.maxOrNull()!!
    private val calibration = 30

    init {
        resDirectory.mkdirs()
    }

    @BeforeEach
    fun beforeEach() {
        manager.clearCache()
        manager.clearDB()
    }

    @Test fun createTest() {
        val resWriter = File(resDirectory,"CreateSingle.csv").bufferedWriter()
        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..calibration) {    //repeat n times to calibrate
                val startTime = System.currentTimeMillis()
                for (j in 1..i) {
                    manager.createVertex()
                }
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)

                manager.clearDB()
                manager.clearCache()
            }
            resWriter.write("$i;${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @Test fun createContainmentsWidthTest() {
        val resWriter = File(resDirectory,"CreateContainmentsWidth.csv").bufferedWriter()
        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..calibration) {
                val graph = manager.createGraph()
                val startTime = System.currentTimeMillis()
                for (j in 1..i) {
                    graph.addVertices(VertexType.CompositeVertex)
                }
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)

                //clear db
                manager.clearDB()
                manager.clearCache()
            }
            resWriter.write("$i;${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @Test fun createContainmentsDepthTest() {
        val resWriter = File(resDirectory,"CreateContainmentsDepth.csv").bufferedWriter()
        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..calibration) {
                val graph = manager.createGraph()
                var lastVertex = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex

                val startTime = System.currentTimeMillis()
                for (j in 2..i) {
                    lastVertex = lastVertex.addSub_vertices(VertexType.CompositeVertex) as CompositeVertex
                }
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)

                //clear db
                manager.clearDB()
                manager.clearCache()
            }
            resWriter.write("$i;${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @Test fun updateTest() {
        val resWriter = File(resDirectory,"Update.csv").bufferedWriter()
        //preparation step
        val vertices = ArrayList<CompositeVertex>(maxSize)
        for (i in 1..maxSize) {
            val vertex = manager.createCompositeVertex()
            vertex.setCapacity(7)
            vertex.setIs_initial(true)
            vertex.setName("some name")
            vertices.add(vertex)
        }
        manager.saveChanges()

        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..calibration) {
                val startTime = System.currentTimeMillis()
                for (j in 1..i) {
                    val vertex = vertices[j]
                    vertex.setCapacity(-1 * vertex.getCapacity()!!)
                    vertex.setIs_initial(!vertex.getIs_initial()!!) //change boolean to opposite value
                    vertex.setName("qwerty")
                }
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)
            }
            resWriter.write("$i;${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @Test fun updateUniqueTest() {
        val resWriter = File(resDirectory,"UpdateUnique.csv").bufferedWriter()
        //----- preparation step -----
        val vertices = ArrayList<CompositeVertex>(maxSize)
        for (i in 1..maxSize) {
            val compositeVertex = manager.createCompositeVertex()
            compositeVertex.setId(-i)
            vertices.add(compositeVertex)
        }
        manager.saveChanges()
        //--- preparation step end ----

        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..calibration) {
                val startTime = System.currentTimeMillis()
                for (j in 1..i) {
                    val v = vertices[j]
                    v.setId(v.getId()!! * -1)
                }
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)
            }
            resWriter.write("$i;${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @Test fun createCrossRefTest() {
        val resWriter = File(resDirectory,"CreateCrossRef.csv").bufferedWriter()
        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..calibration) {
                //----- preparation step -----
                val cv1 = manager.createCompositeVertex()
                val cv2 = manager.createCompositeVertex()
                manager.saveChanges()
                //--- preparation step end ----

                val startTime = System.currentTimeMillis()
                for (j in 1..i) {
                    cv1.setEdge(cv2)
                }
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)

                //clear db
                manager.clearDB()
                manager.clearCache()
            }
            resWriter.write("$i;${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @Test fun removeContainmentsWidthTest() {
        val resWriter = File(resDirectory,"RemoveContainmentsWidth.csv").bufferedWriter()
        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..calibration) {
                //----- preparation step -----
                val vertices = LinkedList<Vertex>()
                val graph = manager.createGraph()
                for (j in 1..i) {
                    vertices.add(graph.addVertices(VertexType.Vertex))
                }
                manager.saveChanges()
                //--- preparation step end ----

                val startTime = System.currentTimeMillis()
                for (j in 1..i) {
                    graph.removeVertices(vertices.pop())
                }
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)
            }
            resWriter.write("$i;${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @Test fun removeContainmentsDepthTest() {
        val resWriter = File(resDirectory,"RemoveContainmentsDepth.csv").bufferedWriter()
        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..calibration) {
                //----- preparation step -----
                val graph = manager.createGraph()
                var lastVertex = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex
                for (j in 2..i) {
                    val cv = lastVertex.addSub_vertices(VertexType.CompositeVertex) as CompositeVertex
                    manager.unload(lastVertex)
                    lastVertex = cv
                }
                manager.saveChanges()
                //--- preparation step end ----

                val cv = graph.loadVertices(1)[0]   //find first vertex
                val startTime = System.currentTimeMillis()
                graph.removeVertices(cv)
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)
            }
            resWriter.write("$i;${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @Test fun removeCrossReferenceTest() {
        val resWriter = File(resDirectory,"RemoveCrossRef.csv").bufferedWriter()
        //----- preparation step1 -----
        val cv1 = manager.createCompositeVertex()
        val cv2 = manager.createCompositeVertex()
        manager.saveChanges()
        //--- preparation step1 end ----

        for (i in sizes) {
            val times = mutableListOf<Double>()
            for (k in 1..calibration) {
                //--- preparation step2 ----
                for (j in 1..(i + 1)) {
                    cv1.setEdge(cv2)
                }
                manager.saveChanges()
                //--- preparation step2 end ----

                val startTime = System.currentTimeMillis()
                for (j in 1..i) {
                    cv1.unsetEdge(cv2)
                }
                manager.saveChanges()
                val endTime = System.currentTimeMillis()
                times.add((endTime - startTime).toDouble() / 1000)
            }
            resWriter.write("$i;${times.average()}\n")
            resWriter.flush()
        }
        resWriter.close()
    }

    @AfterAll fun close() {
        manager.clearDB()
        manager.clearCache()
        manager.close()
    }
}