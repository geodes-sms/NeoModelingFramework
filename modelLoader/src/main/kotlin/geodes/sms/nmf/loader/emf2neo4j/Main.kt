package geodes.sms.nmf.loader.emf2neo4j

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import geodes.sms.nmf.neo4j.io.IGraph
import geodes.sms.nmf.neo4j.io.Neo4jGraph
import org.eclipse.emf.common.util.AbstractTreeIterator
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException
import java.io.File
import kotlin.Exception


fun main(args: Array<String>) {

    try {
        val graph = Neo4jGraph.create(jacksonObjectMapper().readValue(File("Neo4jDBCredentials.json")))
        val (resource, adapter) = getResourceWithAdapter(args[0])
        resource.save(graph, adapter)

        graph.close()
        println("Loaded successfully")
    } catch (e: ServiceUnavailableException) {
        println("Cannot establish connection with Database")
        e.printStackTrace()
    } catch (e: Exception) {
        println("Model loading ERROR")
        e.printStackTrace()
    }

    /* Load multiple files
    GraphDatabase.driver(dbUri, AuthTokens.basic(username, password)).use { driver ->
        File(args[0]).walk()
            .filter { file -> file.extension in listOf("ecore", "xmi") }
            .forEach { file ->
                Neo4jBufferedWriter(driver).use { dbWriter ->
                    EmfModelLoader.createFromContent(file.path)
                        .load(dbWriter)
                    println("model loaded: ${file.name}")
                }
            }
    }
    println("Loading finished")*/
}

fun Resource.save(graph: IGraph, adapter: ECrossReferenceAdapter) {
    contents.forEach { root ->
        val loader = when (root) {
            is EPackage -> ReflectiveLoader(graph, adapter) //EcoreLoader(graph, adapter)
            is EObject -> ReflectiveLoader(graph, adapter)
            else -> throw Exception("Unknown model format in file input")
        }

        //provide an iterator that includes root
        loader.load(object : AbstractTreeIterator<EObject>(root, true) {
            public override fun getChildren(`object`: Any): Iterator<EObject> {
                return (`object` as EObject).eContents().iterator()
            }
        })
    }
}

fun getResourceWithAdapter(modelPath: String) : Pair<Resource, ECrossReferenceAdapter> {
    Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.apply {
        put("ecore", EcoreResourceFactoryImpl())
        put("xmi", XMIResourceFactoryImpl())
    }

    val adapter = ECrossReferenceAdapter()
    val resourceSet = ResourceSetImpl()
    resourceSet.eAdapters().add(adapter)

    // createFileURI method is able to locate metamodel by xsi:schemaLocation
    // absolute path is important here !!
    val resource = resourceSet.getResource(URI.createFileURI(File(modelPath).absolutePath), true)
    return resource to adapter
}