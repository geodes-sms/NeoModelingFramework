package evaluation

import geodes.sms.nmf.loader.emf2neo4j.EmfModelLoader
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import org.junit.jupiter.api.Test
import java.io.File

class TrainBenchmark {

    private val dbUri = DBCredentials.dbUri
    private val username = DBCredentials.username
    private val password = DBCredentials.password

    @Test
    fun loadEvalData() {
        val rootDir = File("../Evaluation/dataset/railway")

        require(rootDir.exists() && rootDir.isDirectory) {
            "Directory not found: ${rootDir.absolutePath}"
        }

        // Collect all XMI files directly (no subfolders)
        val xmiFiles = rootDir.walkTopDown()
            .filter { it.isFile && it.extension.equals("xmi", ignoreCase = true) }
            .toList()

        println("Found ${xmiFiles.size} XMI files to process")

        val graphWriter = GraphBatchWriter(dbUri, username, password)

        try {
            graphWriter.clearDB()
            runEval(xmiFiles.map { it.absolutePath }, graphWriter)
        } finally {
            graphWriter.close()
        }
    }

    fun runEval(files: List<String>, graphWriter: GraphBatchWriter) {

        for (model in files) {
            try {
                println("Loading file ${getModelName(model)}")

                val (nodeCount, edgeCount) = EmfModelLoader.load(model, graphWriter)

                println("Size for file: nodes: $nodeCount, edges: $edgeCount")

            } catch (e: Exception) {
                println("Error loading model ${getModelName(model)}: ${e.message}")
            }
        }
    }

    // Helper function
    fun getModelName(model: String): String {
        val file = File(model)
        val fileName = file.name

        return when {
            fileName.endsWith(".xmi", ignoreCase = true) ->
                fileName.removeSuffix(".xmi")
            fileName.endsWith(".ecore", ignoreCase = true) ->
                fileName.removeSuffix(".ecore")
            else -> fileName
        }
    }
}