package evaluation

import geodes.sms.nmf.loader.emf2neo4j.EmfModelLoader
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import org.junit.jupiter.api.Test
import java.io.File

class RQ2EvalLinTra {
    private val dbUri = DBCredentials.dbUri
    private val username = DBCredentials.username
    private val password =  DBCredentials.password

    @Test
    fun loadEvalData() {
        val rootDir = File("../Evaluation/LinTra")
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
            val xmiFiles = subfolder.listFiles()?.filter {
                it.isFile && it.extension.equals("xmi", ignoreCase = true)
            } ?: emptyList()

            // Merge files: ecore first, then xmi
            val filesToLoad = mutableListOf<String>()
            xmiFiles.forEach { filesToLoad.add(it.absolutePath) }

            println("Files to load in ${subfolder.name}: ${filesToLoad.size}")

            // Run evaluation multiple times if needed
            for (i in 1..2) {
                graphWriter.clearDB()
                runEval(filesToLoad, graphWriter, i,"../Evaluation/results/RQ2LinTra/")
            }

            // Reset database before next subfolder
            println("Resetting database for next subfolder")
        }

        graphWriter.close()
    }

    fun runEval(files: List<String>, graphWriter: GraphBatchWriter, i: Int, baseEvalPath: String) {
        println("Running evaluation number: $i")

        // Group files by their parent folder
        val filesByFolder = files.groupBy { File(it).parentFile }

        for ((folder, folderFiles) in filesByFolder) {
            // Ensure folder for results exists
            val folderResultPath = File(baseEvalPath, folder.name)
            folderResultPath.mkdirs()

            // Create result file for this run inside this folder
            val resFile = File(folderResultPath, "run_$i.csv")
            resFile.writeText("") // clear if exists
            resFile.appendText("model,ext,nodes,edges,time,mem\n")

            // Process each model in this folder
            for (model in folderFiles) {
                try {
                    garbageCollector()
                    val beforeMemory = getUsedMemoryKB()
                    val writeStartTime = System.currentTimeMillis()
                    val (nodeCount, edgeCount) = EmfModelLoader.load(model, graphWriter)
                    val writeTime = System.currentTimeMillis() - writeStartTime
                    val mem = getUsedMemoryKB() - beforeMemory
                    resFile.appendText("${getModelName(model)},${File(model).extension},$nodeCount,$edgeCount,$writeTime,$mem\n")
                } catch (e: Exception) {
                    println("Error loading model: ${getModelName(model)} with message: ${e.message}")
                }
            }
        }
    }

    // Helper functions remain the same
    fun getModelName(model: String): String {
        val file = File(model)
        val fileName = file.name
        return when {
            model.contains("xmi", ignoreCase = true) -> fileName.removeSuffix(".xmi")
            fileName.endsWith(".ecore", ignoreCase = true) -> fileName.removeSuffix(".ecore")
            else -> fileName
        }
    }

    fun getUsedMemoryKB(): Long {
        val runtime = Runtime.getRuntime()
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024
    }

    fun garbageCollector() {
        System.gc()
        Thread.sleep(100)
    }
}