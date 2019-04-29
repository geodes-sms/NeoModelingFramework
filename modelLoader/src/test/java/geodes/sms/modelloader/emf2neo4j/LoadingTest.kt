package geodes.sms.modelloader.emf2neo4j

import org.junit.Test
import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase
import java.io.File

class LoadingTest {

    @Test
    fun modelLoaderPerformanceTest() {

        val dbUri = "bolt://localhost:7687"
        val username = "neo4j"
        val password = "admin"
        val driver = GraphDatabase.driver(dbUri, AuthTokens.basic(username, password))

        //inside test File("./").absolutePath() gives module level path. Use ../ to get project level
        val modelDir = File("../EmfModel/all")
        val resWriter = File("../TestResults/Loader.csv").bufferedWriter()

        driver.use {
            modelDir.walk().filter { it.extension == "ecore" }.forEach { file ->
                println(file.path)
                val times = mutableListOf<Long>()

                for (i in 1..20) {
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

    }
}