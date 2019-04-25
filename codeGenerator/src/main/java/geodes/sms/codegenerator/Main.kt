package geodes.sms.codegenerator

import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase


object Main {

    @JvmStatic
    fun main(args: Array<String>) {

        //val f = File("./tesssssttt.txt").writeText("ttteee  sss  ttt") --> create file in the project level near settings.gradle
        //val outputDir = "/home/vitali/Public/Projects/AndroidStudioProj/Neo4jEMF/modelEditor/src/main/java/geodes/sms/modeleditor"
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