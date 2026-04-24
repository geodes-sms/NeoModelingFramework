package evaluation

import DBCredentials
import geodes.sms.neo4j.io.controllers.IGraphManager
import geodes.sms.nmf.loader.emf2neo4j.EmfModelLoader
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

// To run this file, follow the steps on the Readme.md
// test file to evaluate RQ3 with generated models
//  To what extent can our framework perform CRUD operations on models of different complexities and sizes?
// metrics (time and memory consumed when performing each operation for different
@Suppress("UNCHECKED_CAST")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RQ3EvalModels {
    private val dbUri = DBCredentials.dbUri
    private val username = DBCredentials.username
    private val password = DBCredentials.password
    private val manager = IGraphManager.getDefaultManager(dbUri, username, password)

    private var evalCount = 0
    private val sizesDebug = listOf( // only for debugging
        10, 50, 100, 500, 1000, 5000, 10000, 50000, 100000
    )
    private val sizesEval = listOf( // for the evaluation
        10000, 50000, 100000, 500000, 1000000
    )
    var sizes = sizesDebug
    val isEval = true // set to true to use the evaluation data
    var maxSize = 0

    @Test
    fun loadEvalData() {
        val rootDir = File("../Evaluation/dataset/models")
        require(rootDir.exists() && rootDir.isDirectory) {
            "Directory not found: ${rootDir.absolutePath}"
        }

        // Iterate over each subfolder
        val subfolders = rootDir.listFiles()?.asSequence()
            ?.filter { it.isDirectory }
            ?.toList() ?: emptyList()

        println("Found ${subfolders.size} subfolders to process")

        val graphWriter = GraphBatchWriter(dbUri, username, password)

        for (subfolder in subfolders) {
            println("Processing subfolder: ${subfolder.name}")

            // Get .xmi files
            val xmiFiles = subfolder.listFiles { file ->
                file.isFile && file.extension.equals("xmi", ignoreCase = true)
            } ?: emptyArray()

            val (filteredXmiFiles, otherXmiFiles) = xmiFiles.partition { file ->
                val name = file.name
                name.contains("1000000")}

            val allXmiFiles = ArrayList<File>(xmiFiles.size).apply {
                addAll(filteredXmiFiles)
                addAll(otherXmiFiles)
            }

            val largeFilesToLoad = mutableListOf<String>()
            filteredXmiFiles.forEach { largeFilesToLoad.add(it.absolutePath) }
            println("Files to load for create, update, read in ${subfolder.name}: ${largeFilesToLoad.size}")


            val filesToLoad = ArrayList<String>(allXmiFiles.size)
            for (file in allXmiFiles) {
                filesToLoad.add(file.absolutePath)
            }

            println("Files to load for delete in ${subfolder.name}: ${filesToLoad.size}")

            reset(null, null) // to clear db in case last run threw an exception

            // Run evaluation multiple times if needed
            for (i in 1..3) {
                runEval(largeFilesToLoad, graphWriter, i, subfolder.name)
            }

            for (j in 1..3) { // delete is run separately because it uses multiple files
                runEvalDel(filesToLoad, graphWriter, j, subfolder.name)
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
            // reads
            read(metamodelName)
            // update
            update(metamodelName)
            // create
            create(metamodelName)
        }
    }

    fun runEvalDel(files: List<String>, graphWriter: GraphBatchWriter, i: Int, metamodelName: String) {
        if (isEval) sizes = sizesEval

        maxSize = sizes.last()
        println("Running delete evaluation number: $i")

        val validSizes = sizes.toSet()

        val filteredFiles = files.mapNotNull { file ->
            val name = getModelName(file)
            val size = name.substringAfterLast("_")
                .substringBefore(".")
                .toIntOrNull()

            if (size != null && size in validSizes) {
                file to size
            } else null
        }.sortedBy { it.second }

        evalCount = i
        val resWriter = getFile("Delete", metamodelName)

        for ((model, size) in filteredFiles) {
            reset(model, graphWriter)
            delete(resWriter, size)
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
    fun update(metamodel: String) {
        val resWriter = getFile("Update", metamodel)
        //----- preparation step -----
        val nodes = ArrayList(manager.loadNodes(maxSize) { it })
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
    fun delete(resWriter: File, currentSize: Int) {
        println("Running delete evaluation for size $currentSize")
        //----- preparation step -----
        val nodes = ArrayList(manager.loadNodes(currentSize) { it })
        //--- preparation step end ----
        val limit = minOf(currentSize, nodes.size)
        garbageCollector()
        val beforeMemory = getUsedMemoryKB()
        val startTime = System.currentTimeMillis()

        val mgr = manager
        for (i in 0 until limit) {
            val node = nodes[i]
            mgr.remove(node)
        }
        mgr.saveChanges()

        val time = System.currentTimeMillis() - startTime
        val mem = getUsedMemoryKB() - beforeMemory
        resWriter.appendText("$currentSize,$time,$mem\n")
    }


    // ---------------------------- UTILS ---------------------------- //

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
        if (model == null || model.contains("1000000")) {
            manager.clearDB()
            manager.clearCache()
        }
        if (model != null && graphWriter != null) {
            EmfModelLoader.load(model, graphWriter)
            println("Loading file ${getModelName(model)}")
        }
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