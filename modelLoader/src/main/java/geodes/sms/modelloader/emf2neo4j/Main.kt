package geodes.sms.modelloader.emf2neo4j

import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase
import java.io.File


object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        //val basePath = "./EmfModel/metamodel_test"
        //val modelPath = "$basePath/Containment.xmi"

        val f = File(args[0])
        val dbUri = "bolt://localhost:7687"
        val username = "neo4j"
        val password = "admin"

        GraphDatabase.driver(dbUri, AuthTokens.basic(username, password)).use { driver ->
            if (f.isDirectory) {
                f.walk()
                    .filter { file -> file.extension in listOf("ecore", "xmi") }
                    .forEach { file ->
                        val dbWriter = Neo4jBufferedWriter(driver)
                        EmfModelLoader.createFromContent(file.path).load(dbWriter)
                        dbWriter.close()

                    }
            } else {
                val dbWriter = Neo4jBufferedWriter(driver)
                EmfModelLoader.createFromContent(args[0]).load(dbWriter)
                dbWriter.close()
            }
        }

        println("Loading finished")
    }
}



/*
    @JvmStatic
    fun getModel(modelPath: String) : Resource {
        val extensionMap = Resource.Factory.Registry.INSTANCE.extensionToFactoryMap
        extensionMap["ecore"] = XMIResourceFactoryImpl()
        extensionMap["xmi"] = XMIResourceFactoryImpl()
        val resourceSet = ResourceSetImpl()
        return resourceSet.getResource(URI.createFileURI(File(modelPath).path), true)

        /*
        val encoded = Files.readAllBytes(Paths.get(modelPath))
        val resource = resourceSet.createResource(URI.createURI("*.ecore"))
        resource.load(new URIConverter.ReadableInputStream(new String(encoded, StandardCharsets.UTF_8)), null);
        return resource.contents[0]
        return resource//.allContents//.iterator()
        */
    }
 */
//val parentUri = eObject.eContainer()?.let { resource.getURIFragment(it) }
//val refName = eObject.eContainmentFeature()?.name

//resource.save(System.out, null)