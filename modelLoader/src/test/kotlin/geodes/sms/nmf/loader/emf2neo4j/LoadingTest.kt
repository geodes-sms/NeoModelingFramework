package geodes.sms.nmf.loader.emf2neo4j



import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import org.junit.jupiter.api.Test
import java.io.File

class LoadingTest {

    @Test
    fun loadRailwayModel() {
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

        val graphWriter = GraphBatchWriter(jacksonObjectMapper().readValue(File("../Neo4jDBCredentials.json")))
        graphWriter.clearDB()


        for (i in 1..5) {
            println("Processing model:  $model")

            val resourceLoadStartTime = System.currentTimeMillis()
                val resource = getResource(model)
            val resourceLoadTime = (System.currentTimeMillis() - resourceLoadStartTime).toDouble() / 1000
            resourceLoadTimes.add(resourceLoadTime) //millis to seconds
            println(" resource load time: $resourceLoadTime")

            val loader = ReflectiveBatchLoader(resource, graphWriter)

            val writeStartTime = System.currentTimeMillis()
                val (nodeCount, refCount) = loader.load()
            val writeTime = (System.currentTimeMillis() - writeStartTime).toDouble() / 1000
            dbWriteTimes.add(writeTime) //millis to seconds

            println(" db write time: $writeTime")
            println(" nodes loaded: $nodeCount;  ref loaded: $refCount")
            println()

            resource.unload()
        }

        val s1 = "${resourceLoadTimes.average()}; ${resourceLoadTimes.min()}; ${resourceLoadTimes.max()}"
        val s2 = " ${dbWriteTimes.average()}; ${dbWriteTimes.min()}; ${dbWriteTimes.max()}"
        resFile.appendText("$model; $s1;   $s2;\n")

        //resFile.close()
        graphWriter.clearDB()
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