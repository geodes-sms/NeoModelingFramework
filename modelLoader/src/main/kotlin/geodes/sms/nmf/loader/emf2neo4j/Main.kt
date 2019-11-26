package geodes.sms.nmf.loader.emf2neo4j

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import java.io.File
import kotlin.Exception


fun main(args: Array<String>) {
    try {
        val resource = getResource(args[0])
        println("Loading model: ${args[0]}")

        val graphWriter = GraphBatchWriter(jacksonObjectMapper().readValue(File("Neo4jDBCredentials.json")))
        val loader = ReflectiveBatchLoader(resource, graphWriter)
        val (nodeCount, refCount) = loader.load()

        println("Loaded successfully")
        println("Nodes loaded: $nodeCount")
        println("Refs loaded: $refCount")

        graphWriter.close()
    } catch (e: Exception) {
        println("Loading fail")
        e.printStackTrace()
    }
}

fun getResource(modelPath: String) : Resource {
    Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.apply {
        put("ecore", EcoreResourceFactoryImpl())
        put("xmi", XMIResourceFactoryImpl())
    }

    // createFileURI method is able to locate metamodel by xsi:schemaLocation
    // absolute path is important here !!
    return ResourceSetImpl().getResource(URI.createFileURI(File(modelPath).absolutePath), true)
}