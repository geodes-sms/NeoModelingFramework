package evaluation

import DBCredentials
import geodes.sms.neo4j.io.controllers.IGraphManager
import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.nmf.loader.emf2neo4j.EmfModelLoader
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.util.LinkedList

// To run this file, follow the steps on the Readme.md
// test file to evaluate RQ3
//  To what extent can our framework perform CRUD operations on models of different complexities and sizes?
// metrics (time and memory consumed when performing each operation for different
// configurations as defined in `sizesEval`) for different graph types
@Suppress("UNCHECKED_CAST")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RQ3GenModelsEval {
    private val dbUri = DBCredentials.dbUri
    private val username = DBCredentials.username
    private val password = DBCredentials.password
    private val manager = IGraphManager.getDefaultManager(dbUri, username, password)

    private var evalCount = 0
    private val sizesDebug = listOf( // only for debugging
        10, 50, 100, 500, 1000, 5000, 10000
    )
    private val sizesEval = listOf( // for the evaluation
        10, 50, 100, 500, 1000, 5000, 10000, 50000, 100000, 500000, 1000000
    )
    var sizes = sizesDebug
    val isEval = false // set to true to use the evaluation data
    var maxSize = 0

    @Test
    fun loadEvalData() {
        val rootDir = File("../Evaluation/dataset/modelsRQ3")
        require(rootDir.exists() && rootDir.isDirectory) {
            "Directory not found: ${rootDir.absolutePath}"
        }

        // Iterate over each subfolder
        val subfolders = rootDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
        println("Found ${subfolders.size} subfolders to process")

        val graphWriter = GraphBatchWriter(dbUri, username, password)
        for ((subIndex, subfolder) in subfolders.withIndex()) {
            println("Processing subfolder: ${subfolder.name}")


            // Load all .xmi files in the same subfolder
            val xmiFiles = subfolder.walkTopDown()
                .filter { it.isFile && it.extension.equals("xmi", ignoreCase = true) }
                .toList()

            // Merge files: ecore first, then xmi
            val filesToLoad = mutableListOf<String>()
            xmiFiles.forEach { filesToLoad.add(it.absolutePath) }

            println("Files to load in ${subfolder.name}: ${filesToLoad.size}")
            reset(null, null) // to clear db in case last run threw an exception
            // Run evaluation multiple times if needed
            for (i in 1..2) {
                runEval(filesToLoad, graphWriter, i, subfolder.name)
            }

        }
        manager.close() // close db connection
    }

    fun runEval(files: List<String>, graphWriter: GraphBatchWriter, i: Int, metamodelName: String) {
        if (isEval)
            sizes = sizesEval
        maxSize = sizes[sizes.size - 1]
        println("Running evaluation number: $i")

        for (model in files) {
            println("Loading model ${getModelName(model)}")
            reset(model, graphWriter)
            // we run the evaluation multiple times to mitigate threats
            evalCount = i
            // For each run, execute all tests
            //delete
            delete(metamodelName)
            reset(model,graphWriter)
            // create
//            create(metamodelName)
//            reset(model,graphWriter)

            // reads
//            read(metamodelName)
//            reset(model, graphWriter)

            // update
//            update(metamodelName)
//            reset(model,graphWriter)

        }
    }


    // ---------------------------- CREATE ---------------------------- //
    fun create(metamodel: String) {
        val resWriter = getFile("Create", metamodel)
        for (i in sizes) {
            println("Running create evaluation for size $i")
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            for (j in 1..i) {
                manager.createNode("NewNode")
            }
            manager.saveChanges()
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory
            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
    }


    // ---------------------------- READ ---------------------------- //
    fun read(metamodel: String) {
        val resWriter = getFile("Read", metamodel)
        //----- preparation step -----
        manager.clearCache() // to make sure we are not reading from memory
        //--- preparation step end ----
        for (i in sizes) {
            println("Running read evaluation for size $i")
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            manager.loadNodes(i, { it })
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory
            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
            manager.clearCache() // to make sure we are loading from the database
        }

    }


    // ---------------------------- UPDATE ---------------------------- //
    fun update(metamodel: String,) {
        val resWriter = getFile("Update", metamodel)
        //----- preparation step -----
        val nodes = manager.loadNodes(maxSize, { it })
        //---- preparation step end ----
        for (i in sizes) {
            println("Running update evaluation for size $i")
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            for (j in 0 until i) {
                val node = nodes[j]
                node.putProperty("name", "newValue")
            }
            manager.saveChanges()
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory
            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
    }

    // ---------------------------- DELETE ---------------------------- //
    fun delete(metamodel: String) {
        val resWriter = getFile("DeleteSingle", metamodel)
        //----- preparation step -----
        val nodes = LinkedList(manager.loadNodes(maxSize) { it })
        //--- preparation step end ----
        for (i in sizes) {
            println("Running delete evaluation for size $i")
            var mem: Long
            garbageCollector()
            val beforeMemory = getUsedMemoryKB()
            val startTime = System.currentTimeMillis()
            (1..i).forEach { j ->
                manager.remove(nodes.pop())
            }
            manager.saveChanges()
            val endTime = System.currentTimeMillis()
            mem = getUsedMemoryKB() - beforeMemory
            val time = endTime - startTime
            resWriter.appendText("$i,$time,$mem\n")
        }
    }


    // ---------------------------- UTILS ---------------------------- //
    /**
     * For deletes, we create as many elements as we will delete in total (the combination of all sizes)
     */
    private fun getMaxSizeForDelete(): Int {
        var deleteMaxSize = 0
        for (i in sizes) {
            deleteMaxSize += i
        }
        return deleteMaxSize
    }

    /**
     * create csv file to store the results
     */
    fun getFile(operationName: String, metamodel: String): File {
        val resFile = File(
            "../Evaluation/results/RQ3GenModels/$metamodel/${operationName}_run_$evalCount.csv"
        )

        // Ensure directory exists
        resFile.parentFile?.mkdirs()

        // Create/overwrite file and write header
        resFile.writeText("element_count,time,mem\n")

        println("file created: ${resFile.absolutePath}")
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
    fun reset(model: String?, graphWriter: GraphBatchWriter?) {
        manager.clearDB()
        manager.clearCache()
        if (model != null && graphWriter != null)
            EmfModelLoader.load(model, graphWriter)
    }

    fun getModelName(model: String): String {
        val file = File(model)
        val fileName = file.name
        return when {
            model.contains("xmi", ignoreCase = true) -> fileName.removeSuffix(".xmi")
            fileName.endsWith(".ecore", ignoreCase = true) -> fileName.removeSuffix(".ecore")
            else -> fileName
        }
    }

}