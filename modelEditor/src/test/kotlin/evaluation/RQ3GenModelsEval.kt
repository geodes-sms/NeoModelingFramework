package evaluation

import DBCredentials
import geodes.sms.neo4j.io.controllers.IGraphManager
import geodes.sms.neo4j.io.type.AsString
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
class RQ3GenModelsEval {
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
        val subfolders = rootDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
        println("Found ${subfolders.size} subfolders to process")

        val graphWriter = GraphBatchWriter(dbUri, username, password)
        for ((subIndex, subfolder) in subfolders.withIndex()) {
            println("Processing subfolder: ${subfolder.name}")


            // Load all .xmi files in the same subfolder and othe largest only
            val (filteredXmiFiles, otherXmiFiles) =
                subfolder.listFiles { file ->
                    file.isFile && file.extension.equals("xmi", ignoreCase = true)
                }?.partition { file ->
                    file.name.contains("1000000")  || file.name.contains("eclipseModel-all")// only the largest file is used for create, update, and read
                } ?: (emptyList<File>() to emptyList())

            val allXmiFiles = filteredXmiFiles + otherXmiFiles


            val largeFilesToLoad = mutableListOf<String>()
            filteredXmiFiles.forEach { largeFilesToLoad.add(it.absolutePath) }

            println("Files to load for create, update, read in ${subfolder.name}: ${largeFilesToLoad.size}")
            reset(null, null) // to clear db in case last run threw an exception

            val filesToLoad = mutableListOf<String>()
            allXmiFiles.forEach { filesToLoad.add(it.absolutePath) }
            println("Files to load for delete in ${subfolder.name}: ${filesToLoad.size}")
            reset(null, null) // to clear db in case last run threw an exception
            // Run evaluation multiple times if needed
            for (i in 1..30) {
                //runEval(largeFilesToLoad, graphWriter, i, subfolder.name)
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
//
//
//            // create
            create(metamodelName)


            // update
            update(metamodelName)
        }
    }

    fun runEvalDel(files: List<String>, graphWriter: GraphBatchWriter, i: Int, metamodelName: String) {
        if (isEval)
            sizes = sizesEval
        maxSize = sizes[sizes.size - 1]
        println("Running delete evaluation number: $i")
        val filteredFiles = files.mapNotNull { file ->
            val name = getModelName(file)
            val sizeInName = name.substringAfterLast("_").substringBefore(".").toIntOrNull()
            if (sizeInName != null && sizeInName in sizes) {
                file to sizeInName   // keep both
            } else {
                null
            }
        }
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
        //----- preparation step -----
        val nodes = ArrayList(manager.loadNodes(maxSize) { it })
        //--- preparation step end ----
        println("Running delete evaluation for size $currentSize")
        var mem: Long
        garbageCollector()
        val size = nodes.size
        val beforeMemory = getUsedMemoryKB()
        val startTime = System.currentTimeMillis()
        for (i in 0 until size) {
            manager.remove(nodes[i])
        }
        manager.saveChanges()
        val endTime = System.currentTimeMillis()
        mem = getUsedMemoryKB() - beforeMemory
        val time = endTime - startTime
        resWriter.appendText("$currentSize,$time,$mem\n")
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