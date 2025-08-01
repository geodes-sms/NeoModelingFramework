package evaluation

import DBCredentials
import geodes.sms.nmf.editor.graph.CompositeVertex
import geodes.sms.nmf.editor.graph.Graph
import geodes.sms.nmf.editor.graph.Vertex
import geodes.sms.nmf.editor.graph.VertexType
import geodes.sms.nmf.editor.graph.neo4jImpl.ModelManagerImpl
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.*

// To run this file, follow the steps on the Readme.md
// test file to evaluate RQ2
//  To what extent can our approach perform CRUD operations on metamodels and models of different domains, complexity and sizes?
// metrics (time and memory consumed when performing each operation for different configurations as defined in `sizesEval`)
@Suppress("UNCHECKED_CAST")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RQ2Eval {
    private val dbUri = DBCredentials.dbUri
    private val username = DBCredentials.username
    private val password = DBCredentials.password
    private val manager = ModelManagerImpl(dbUri, username, password)
    private val sizesDebug = listOf( // only for debugging
        10, 50, 100, 500, 1000, 5000, 10000
    )
    private val sizesEval = listOf( // for the evaluation
        10, 50, 100, 500, 1000, 5000, 10000, 50000, 100000, 500000, 1000000
    )
    var sizes = sizesDebug
    val isEval = true // set to true to use the evaluation data
    var maxSize = 0

    private var evalCount = 0

    @Test fun loadEvalData() {
        if (isEval)
            sizes = sizesEval
        maxSize = sizes[sizes.size-1]
        reset() // to clear db in case last run threw an exception
        for (i in 8 .. 30) { // we run the evaluation multiple times to mitigate threats
            evalCount = i
            // For each run, execute all tests
            //deletes
            deleteCrossRef()
            reset()
            deleteContainmentDepth()
            reset()
            deleteSingle()
            reset()
            deleteContainmentWidth()
            reset()
            // creates
            createSingle()
            reset()
            createContainmentWidth()
            reset()
            createContainmentDepth()
            reset()
            createCrossRef()
            reset()

            // reads
            readSingle()
            reset()
            readContainmentWidth()
            reset()
            readContainmentDepth()
            reset()
            readCrossRef()
            reset()

            // update
            update()
            reset()
        }
        manager.close() // close db connection
    }

    // ---------------------------- CREATE ---------------------------- //
    fun createSingle() {
        val resWriter = getFile("CreateSingle")
        for (i in sizes) {
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            generateSingleGraph(i)
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory
            val time = endTime - startTime
            reset() // clear db and cache
            resWriter.appendText("$i,$time,$mem\n")
        }
    }



    fun createContainmentWidth() {
        val resWriter = getFile("CreateContainmentWidth")
        for (i in sizes) {
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            generateContainmentWidthGraph(i)
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory
            val time = endTime - startTime
            reset() // clear db and cache
            resWriter.appendText("$i,$time,$mem\n")
        }
    }
    //
    fun createContainmentDepth() {
        val resWriter = getFile("CreateContainmentDepth")
        for (i in sizes) {
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            generateContainmentDepthGraph(i)
            manager.saveChanges()
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory
            val time = endTime - startTime
            reset() // clear db and cache
            resWriter.appendText("$i,$time,$mem\n")
        }

    }

    fun createCrossRef() {
        val resWriter = getFile("CreateCrossRef")
        for (i in sizes) {
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            generateCrossRefGraph(i)
            manager.saveChanges()
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory
            val time = endTime - startTime
            reset() // clear db and cache
            resWriter.appendText("$i,$time,$mem\n")
        }
    }

    // ---------------------------- READ ---------------------------- //
    fun readSingle() {
        val resWriter = getFile("ReadSingle")
        //----- preparation step -----
        generateSingleGraph(maxSize)
        manager.clearCache() // to make sure we are not reading from memory
        //--- preparation step end ----
        for (i in sizes) {
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            manager.loadCompositeVertexList(i)
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory

            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }

    }

    fun readContainmentWidth() {
        val resWriter = getFile("ReadContainmentWidth")
        //----- preparation step -----
        generateContainmentWidthGraph(maxSize)
        manager.clearCache() // to make sure we are not reading from memory
        //--- preparation step end ----
        for (i in sizes) {
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            manager.loadCompositeVertexList(i)
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory

            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
    }

    fun readContainmentDepth() {
        val resWriter = getFile("ReadContainmentDepth")
        //----- preparation step -----
        generateContainmentDepthGraph(maxSize)
        manager.clearCache() // to make sure we are not reading from memory
        //--- preparation step end ----
        for (i in sizes) {
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            manager.loadCompositeVertexList(i)
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory

            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
    }

    fun readCrossRef() {
        val resWriter = getFile("ReadCrossRef")
        //----- preparation step -----
        generateCrossRefGraph(maxSize)
        manager.clearCache() // to make sure we are not reading from memory
        //--- preparation step end ----
        for (i in sizes) {
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            manager.loadCompositeVertexList(i)
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory

            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
    }

    // ---------------------------- UPDATE ---------------------------- //
    fun update() {
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
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            for (j in 0 until i) {
                val vertex = vertices[j]
                vertex.setCapacity(-1 * vertex.getCapacity()!!)
                vertex.setIs_initial(!vertex.getIs_initial()!!) //change boolean to opposite value
                vertex.setName("y$j")
            }
            manager.saveChanges()
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory

            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
    }

    // ---------------------------- DELETE ---------------------------- //
    fun deleteSingle() {
        val resWriter = getFile("DeleteSingle")
        //----- preparation step -----
        generateSingleGraph(getMaxSizeForDelete())
        val vertices:LinkedList<Vertex> = manager.loadVertexList(getMaxSizeForDelete()) as LinkedList<Vertex>
        //--- preparation step end ----
        for (i in sizes) {
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            (1..i).forEach { j ->
                manager.remove(vertices.pop())
            }
            manager.saveChanges()
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory

            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
    }

    fun deleteContainmentWidth() {
        val resWriter = getFile("DeleteContainmentWidth")
        //----- preparation step -----
        generateContainmentWidthGraph(getMaxSizeForDelete())
        val vertices:LinkedList<Vertex> = manager.loadCompositeVertexList(getMaxSizeForDelete()) as LinkedList<Vertex>
        //--- preparation step end ----
        for (i in sizes) {
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            (1..i).forEach { j ->
                manager.remove(vertices.pop())
            }
            manager.saveChanges()
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory

            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
    }

    fun deleteContainmentDepth() {
        val resWriter = getFile("DeleteContainmentDepth")
        for (i in sizes) {  // due to the remove recursive call, we have to create the graphs for each run
            //----- preparation step -----
            generateContainmentDepthGraph(i)
            val vertices:LinkedList<Vertex> = manager.loadCompositeVertexList(i) as LinkedList<Vertex>
            //--- preparation step end ----
            // garbageCollector()
            var mem: Long
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            manager.remove(vertices.pop()) // since it's a recursive remove, calling only one is enough
            manager.saveChanges()
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory
            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
    }

    fun deleteCrossRef() {
        val resWriter = getFile("DeleteCrossRef")
        //----- preparation step -----
        generateCrossRefGraph(getMaxSizeForDelete())
        val vertices:LinkedList<Vertex> = manager.loadCompositeVertexList(getMaxSizeForDelete()) as LinkedList<Vertex>
        //--- preparation step end ----
        for (i in sizes) {
            garbageCollector()
            var mem: Long
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            for (j in 1..i) {
                if(j!=maxSize) // to prevent trying to remove the last edge (it does not exist as it is removed when the previous one is)
                    vertices[0].unsetEdge(vertices[1])
            }
            manager.saveChanges()
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory

            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
        vertices.clear()
    }

    // ---------------------------- GRAPH GENERATORS ---------------------------- //

    fun generateSingleGraph(maxSize: Int) {
        (1..maxSize).forEach { j ->
            manager.createVertex()
        }
        manager.saveChanges()
    }
    private fun generateContainmentWidthGraph(maxSize: Int): Graph{
        val graph = manager.createGraph()
        (1..maxSize).forEach { j ->
            graph.addVertices(VertexType.CompositeVertex)
        }
        manager.saveChanges()
        return graph
    }

    private fun generateContainmentDepthGraph(maxSize: Int) : Graph {
        val graph = manager.createGraph()
        var lastVertex = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex
        (2..maxSize).forEach { j ->
            // since the first vertex is created before the loop, we start at 2
            lastVertex = lastVertex.addSub_vertices(VertexType.CompositeVertex) as CompositeVertex
        }
        manager.saveChanges()
        return graph
    }

    private fun generateCrossRefGraph(maxSize: Int) {
        val cv1 = manager.createCompositeVertex()
        val cv2 = manager.createCompositeVertex()
        (1..maxSize).forEach { j ->
            cv1.setEdge(cv2)
        }
        manager.saveChanges()
    }

    // ---------------------------- UTILS ---------------------------- //
    /**
     * For deletes, we create as many elements as we will delete in total (the combination of all sizes)
     */
    private fun getMaxSizeForDelete() : Int {
        var deleteMaxSize = 0
        for (i in sizes) {
            deleteMaxSize += i
        }
        return deleteMaxSize
    }

    /**
     * create csv file to store the results
     */
    fun getFile(operationName: String): File {
        val resFile = File("../ECMFA-2026-Evaluation/results/RQ2/${operationName}/${operationName}_run_$evalCount.csv")
        resFile.writeText("") // clear file in case it existed before
        resFile.appendText("element_count,time,mem\n")
        println("file created: ${resFile.name}")
        return resFile
    }

    /**
     * return memory used in KB
     */
    fun getUsedMemoryKB(): Long {
        val runtime = Runtime.getRuntime()
        val usedBytes = runtime.totalMemory() - runtime.freeMemory()
        return usedBytes / 1024 // memory is in KB
    }

    /**
     * Makes sure that memory is cleaned before collecting results
     */
    fun garbageCollector() {

        System.gc()
        Thread.sleep(100)
    }

    /**
     * Clear db data and cache to go back to original state
     */
    fun reset(){
        manager.clearDB()
        manager.clearCache()
    }
}