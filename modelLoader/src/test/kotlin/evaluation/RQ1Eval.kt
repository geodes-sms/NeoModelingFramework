package evaluation

import geodes.sms.nmf.loader.emf2neo4j.EmfModelLoader
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
//import org.eclipse.emf.ecore.EPackage
//import org.eclipse.gmt.modisco.java.cdo.meta.JavaPackage
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
        val directory = File("../Evaluation/models") // loading models
        val files = directory
            .walk()
            .filter { it.isFile && it.extension == "ecore" }
            .map { it.path }
            .toList()

        val graphWriter = GraphBatchWriter(dbUri, username, password)
        graphWriter.clearDB() // in case there is data in the db
        for (i in 1 .. 1) { // we run the evaluation multiple times to mitigate threats
            runEval("ecore",files, graphWriter, i)
        }
        graphWriter.close()
    }

//    @Test fun loadEvalDataXMI() {
//        val directory = File("../Evaluation/NeoEMF-benchmark-models") // loading models
//        val files = directory
//            .walk()
//            .filter { it.isFile && it.extension == "xmi" }
//            .map { it.path }
//            .toList()
//        EPackage.Registry.INSTANCE.put(JavaPackage.eNS_URI, JavaPackage.eINSTANCE);
//        val graphWriter = GraphBatchWriter(dbUri, username, password)
//        graphWriter.clearDB() // in case there is data in the db
//        for (i in 1 .. 1) { // we run the evaluation multiple times to mitigate threats
//            runEval("xmi",files, graphWriter, i)
//        }
//        graphWriter.close()
//    }

    fun runEval(type: String, files: List<String>, graphWriter:GraphBatchWriter, i: Int) {
        println("Running evaluation number: $i")
        val resFile = getFile(i,type) // creating csv file for each eval
        for (model in files) {
            try {
                garbageCollector() // to guarantee that the garbage colletor is run before the memory
                val beforeMemory = getUsedMemoryKB()
                val writeStartTime = System.currentTimeMillis()
                val (nodeCount, edgeCount) = EmfModelLoader.Companion.load(model, graphWriter)
                val writeTime = System.currentTimeMillis() - writeStartTime
                val mem = getUsedMemoryKB() - beforeMemory
                resFile.appendText("${getModelName(model)},$nodeCount,$edgeCount,$writeTime,$mem\n")
            }catch (e: Exception) {  // to avoid invalid models (models with null values)
               println("error loading model: ${getModelName(model)} with message: ${e.message}")
            }
        }
        graphWriter.clearDB() // clearing db after eval is finished
    }

    fun getModelName(model: String): String {
        // get only the model name
        val file = File(model)
        val fileName = file.name
        var end = ".ecore";
        if(model.contains("xmi"))
            end = ".xmi"
        return if (end.isNotEmpty()) fileName.removeSuffix(end) else fileName
    }

    fun getFile(i: Int,type:String): File {
        // create csv file to store the results
        val resFile = File("../Evaluation/results/RQ1/$type/models_run_$i.csv")
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
        // to make sure that memory is cleaned before collecting it
        System.gc()
        Thread.sleep(100)
    }



}