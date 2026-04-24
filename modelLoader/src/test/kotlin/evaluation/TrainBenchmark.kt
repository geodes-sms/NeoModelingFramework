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

        // Collect files
        val allFiles = rootDir.walkTopDown()
            .filter { it.isFile }
            .toList()

        val ecoreFiles = allFiles.filter { it.extension.equals("ecore", ignoreCase = true) }
        val xmiFiles = allFiles.filter { it.extension.equals("xmi", ignoreCase = true) }

        require(ecoreFiles.isNotEmpty()) {
            "No Ecore file found in ${rootDir.absolutePath}"
        }

        println("Found ${ecoreFiles.size} Ecore file(s) and ${xmiFiles.size} XMI files")

        val graphWriter = GraphBatchWriter(dbUri, username, password)

        try {
            graphWriter.clearDB()
            // Load Ecore first
            loadEcore(ecoreFiles.map { it.absolutePath },graphWriter)

            // Then run evaluation on XMI models
            runEval(xmiFiles.map { it.absolutePath }, graphWriter)

        } finally {
            graphWriter.close()
        }
    }

    /**
     * Loads and registers the Ecore metamodel(s) before any XMI loading.
     */
    private fun loadEcore(ecoreFiles: List<String>, graphWriter: GraphBatchWriter) {
        for (ecore in ecoreFiles) {
            try {
                println("Loading file ${getModelName(ecore)}")
                EmfModelLoader.load(ecore, graphWriter)

            } catch (e: Exception) {
                println("Error loading Ecore ${ecore}: ${e.message}")
            }
        }
    }

    fun runEval(files: List<String>, graphWriter: GraphBatchWriter) {

        for (model in files) {
            try {
                graphWriter.clearDB()
                println("Loading file ${getModelName(model)}")

                val (nodeCount, edgeCount) = EmfModelLoader.load(model, graphWriter)

                println("Size for file: nodes: $nodeCount, edges: $edgeCount")

            } catch (e: Exception) {
                println("Error loading model ${getModelName(model)}: ${e.message}")
            }
        }
    }

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