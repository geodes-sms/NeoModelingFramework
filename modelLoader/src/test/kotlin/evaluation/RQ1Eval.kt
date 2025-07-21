package evaluation

import geodes.sms.nmf.loader.emf2neo4j.EmfModelLoader
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import org.junit.jupiter.api.Test
import java.io.File
/*/
To run this file, follow the steps on the Readme.md
 */
// test file to evaluate RQ1
// To what extent can our approach represent metamodels and models of different domains, complexity and sizes?
// Metrics: model size Number of classes, Number of associations/containments, Number of attributes, Types of attributes
// For us, only: int, Integer, string, list, map,
// number of nodes, number of edges, time (ms) and memory (KB) consumed to load

class RQ1Eval {
    private val dbUri = DBCredentials.dbUri
    private val username = DBCredentials.username
    private val password =  DBCredentials.password

    @Test fun loadEvalData() {
        val directory = File("../ECMFA-2026-Evaluation/models") // loading models
        val ecoreFiles = directory
            .walk()
            .filter { it.isFile && it.extension == "ecore" }
            .map { it.path }
            .toList()

        val graphWriter = GraphBatchWriter(dbUri, username, password)
        runEval(ecoreFiles,graphWriter,0) // we run the evaluation multiple times and use the worst values to mitigate threats
        graphWriter.close()
    }

    fun runEval(ecoreFiles: List<String>, graphWriter:GraphBatchWriter, i: Int) {
        println("Running evaluation number: $i")
        val resFile = getFile(i) // creating csv file for each eval
        for (model in ecoreFiles) {
            try {
                garbageCollector() // to guarantee that the garbage colletor is run before the memory
                val beforeMemory = getUsedMemoryKB()
                val writeStartTime = System.currentTimeMillis()
                val (nodeCount, edgeCount) = EmfModelLoader.Companion.load(model, graphWriter)
                val mem = getUsedMemoryKB() - beforeMemory
                val writeTime = System.currentTimeMillis() - writeStartTime
                resFile.appendText("${getModelName(model)},$nodeCount,$edgeCount,$writeTime,$mem\n")
            }catch (e: Exception) {  // to avoid invalid models (models with null values)
               // println("error loading model: ${getModelName(model)} with message: ${e.message}")
            }
        }
        graphWriter.clearDB() // clearing db after eval is finished
    }

    fun getModelName(model: String): String {
        // get only the model name
        var end = ".ecore";
        if(model.contains("xmi"))
            end = ".xmi"
        return model.substring(model.lastIndexOf('\\') + 1,model.lastIndexOf(end));

    }

    fun getFile(i: Int): File {
        // create csv file to store the results
        val resFile = File("../ECMFA-2026-Evaluation/results/RQ1/models_run_$i.csv")
        resFile.writeText("") // clear file in case it existed before
        resFile.appendText("model,nodes,edges,time,mem\n")
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