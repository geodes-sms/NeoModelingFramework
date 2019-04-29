package geodes.sms.codegenerator

import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase


object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        // currently in a project level directory
        val outputDir = "./modelEditor/src/main/java/geodes/sms/modeleditor"

        val dbUri = "bolt://localhost:7687"
        val username = "neo4j"
        val password = "admin"
        val driver = GraphDatabase.driver(dbUri, AuthTokens.basic(username, password))

        driver.use {
            CodeGenerator.generateFromNeo4jMetamodel(it, 743, outputDir)
        }

        println("code generation finished")
    }
}