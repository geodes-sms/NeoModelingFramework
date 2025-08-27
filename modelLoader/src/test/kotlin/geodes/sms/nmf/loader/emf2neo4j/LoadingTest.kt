package geodes.sms.nmf.loader.emf2neo4j

import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import org.junit.jupiter.api.Test
import java.io.File

class LoadingTest {
    private val dbUri = DBCredentials.dbUri
    private val username = DBCredentials.username
    private val password =  DBCredentials.password

    @Test fun loadRailwayModel() {
        val models = listOf(
            "../EmfModel/instance/railway/railway-batch-1.xmi",
            "../EmfModel/instance/Attributes.xmi",
            "../EmfModel/instance/Document.xmi",
            "../EmfModel/instance/Document_large.xmi",
            "../EmfModel/instance/EnumTest.xmi",
            "../EmfModel/instance/Graph.xmi",
            "../EmfModel/metamodel/Attributes.ecore",
        )


        val resFile = File("../TestResults/LoaderRailway.csv")
        resFile.writeText("") // clear file
        val dbWriteTimes = mutableListOf<Double>()
        val graphWriter = GraphBatchWriter(dbUri, username, password)
        resFile.appendText("model,time_avg,time_min,time_max\n")
        for (model in models) {
            println("Processing model:  $model")
            val writeStartTime = System.currentTimeMillis()
            val (nodeCount, refCount) = EmfModelLoader.load(model, graphWriter)
            val writeTime = (System.currentTimeMillis() - writeStartTime).toDouble()
            dbWriteTimes.add(writeTime/ 1000) //millis to seconds
            val s2 = " ${dbWriteTimes.average()}, ${dbWriteTimes.minOrNull()}, ${dbWriteTimes.maxOrNull()}"
            val modelName = getModelName(model)
            resFile.appendText("$modelName,$s2\n")
        }

        graphWriter.close()
    }

    fun getModelName(model: String): String {
        // to get only the model name
        var end = ".ecore";
        if(model.contains("xmi"))
            end = ".xmi"
        return model.substring(model.lastIndexOf('/') + 1,model.lastIndexOf(end));

    }

    /*  OLD loader
    @Test
    fun modelLoaderPerformanceTest() {

        val dbUri = "bolt://localhost:7687"
        val username = "neo4j"
        val password = "admin"
        val driver = GraphDatabase.driver(dbUri, AuthTokens.basic(username, password))

        //inside test File("./").absolutePath() gives module level path. Use ../ to get project level
        val modelDir = File("../EmfModel/metamodel_test")
        val resWriter = File("../TestResults/Loader.csv").bufferedWriter()

        driver.use {
            modelDir.walk().filter { it.extension == "ecore" }.sortedBy {it.name }.forEach { file ->
                println(file.path)
                val times = mutableListOf<Long>()

                for (i in 1..15) {
                    print("$i  ")

                    val startTime = System.currentTimeMillis()
                    val dbWriter = Neo4jBufferedWriter(it)
                    dbWriter.use { writer ->
                        EmfModelLoader.createFromContent(file.path).load(writer)
                    }
                    val endTime = System.currentTimeMillis()
                    times.add(endTime - startTime)
                    driver.session().run("MATCH (n) DETACH DELETE n")
                }
                val totalMillis = times.reduce { acc, time -> acc + time } / times.size
                val seconds = totalMillis.toDouble() / 1000

                resWriter.write("${file.name}; $totalMillis; $seconds\n")
                println("\ntotal: $totalMillis $seconds\n")
            }
            resWriter.close()
        }
    }*/
}