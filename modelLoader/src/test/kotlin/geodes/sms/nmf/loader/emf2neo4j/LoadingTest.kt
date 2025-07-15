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
            "../EmfModel/instance/railway/railway-batch-32.xmi",        //[0]
            "../EmfModel/instance/railway/railway-batch-64.xmi",        //[1]
            "../EmfModel/instance/railway/railway-batch-128.xmi",       //[2]
            "../EmfModel/instance/railway/railway-batch-256.xmi",       //[3]
            "../EmfModel/instance/railway/railway-batch-512.xmi",       //[4]
            "../EmfModel/instance/railway/railway-batch-1024.xmi"       //[5]
        )
        val model = models[2]

        val resFile = File("../TestResults/LoaderRailway.csv")
        //resWriter.write("model; loadResource avg (sec); loadResource min (sec); loadResource max (sec); " +
        //        "dbWrite avg (sec); dbWrite min (sec); dbWrite max (sec);\n")

        val resourceLoadTimes = mutableListOf<Double>() //seconds
        val dbWriteTimes = mutableListOf<Double>()  //seconds

        val graphWriter = GraphBatchWriter(dbUri, username, password)
        //graphWriter.clearDB()

        for (i in 1..5) {
            println("Processing model:  $model")

//            val resourceLoadStartTime = System.currentTimeMillis()
//                val resource = getResource(model)
//            val resourceLoadTime = (System.currentTimeMillis() - resourceLoadStartTime).toDouble() / 1000
//            resourceLoadTimes.add(resourceLoadTime) //millis to seconds
//            println(" resource load time: $resourceLoadTime")
//            val loader = ReflectiveBatchLoader(graphWriter)

            val writeStartTime = System.currentTimeMillis()
                val (nodeCount, refCount) = EmfModelLoader.load(model, graphWriter)
            val writeTime = (System.currentTimeMillis() - writeStartTime).toDouble() / 1000
            dbWriteTimes.add(writeTime) //millis to seconds
        }

        val s1 = "${resourceLoadTimes.average()}; ${resourceLoadTimes.minOrNull()}; ${resourceLoadTimes.maxOrNull()}"
        val s2 = " ${dbWriteTimes.average()}; ${dbWriteTimes.minOrNull()}; ${dbWriteTimes.maxOrNull()}"
        resFile.appendText("$model; $s1;   $s2;\n")

        //resFile.close()
        graphWriter.close()
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