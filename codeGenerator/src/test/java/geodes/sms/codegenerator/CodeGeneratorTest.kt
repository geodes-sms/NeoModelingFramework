package geodes.sms.codegenerator

import org.junit.Test
import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase
import java.io.File

class CodeGeneratorTest {

    @Test
    fun generatorTest() {


        val dbUri = "bolt://localhost:7687"
        val username = "neo4j"
        val password = "admin"
        val driver = GraphDatabase.driver(dbUri, AuthTokens.basic(username, password))

        val outputDir = createTempDir(directory = File("./"))
        val resWriter = File("../TestResults/CodeGenerator.csv").bufferedWriter()

        val models = listOf<Long>(1019, 1467)

        driver.use {
            models.forEach { metamodelID ->

                println("metamodelID: $metamodelID")
                val times = mutableListOf<Long>()

                for (i in 1..20) {
                    print("$i  ")

                    val startTime = System.currentTimeMillis()
                    val packageName = CodeGenerator.generateFromNeo4jMetamodel(it, metamodelID, outputDir.path)
                    val endTime = System.currentTimeMillis()

                    times.add(endTime - startTime)
                    File(outputDir, packageName).deleteRecursively()
                }
                val total = times.reduce { acc, time -> acc + time } / times.size
                val seconds = total.toDouble() / 1000
                resWriter.write("ID: $metamodelID; $total; $seconds\n")

                println("\ntotal: $total $seconds\n")
            }
            resWriter.close()
        }
    }
}