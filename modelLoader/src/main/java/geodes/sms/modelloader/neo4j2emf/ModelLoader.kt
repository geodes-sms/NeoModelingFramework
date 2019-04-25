package geodes.sms.modelloader.neo4j2emf

import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.*
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import kotlin.Exception


class ModelLoader (private val metaEPackage : EPackage, private val modelReader : Neo4jModelReader) {

    /** neo4jID -> EObject */
    private val eObjectMap = hashMapOf<Long, EObject>()

    /** root element of model */
    private val eFactory = metaEPackage.eFactoryInstance


    fun load(fileName : String) {
        while (modelReader.hasData()) {
            val neo4jObjects = modelReader.readSubGraph()

            //neo4jObjects.forEach { neo4jObject ->
            for (neo4jObject in neo4jObjects) {
                val props = neo4jObject.node.asMap()
                val eClass = metaEPackage.getEClassifier(neo4jObject.node.labels().first())
                if (eClass is EClass) {
                    val eObject = eFactory.create(eClass)
                    eObjectMap[neo4jObject.node.id()] = eObject

                    println(neo4jObject.node.labels().first())

                    props.forEach { (key, value) ->
                        val feature = eClass.getEStructuralFeature(key)
                        feature?.let { eObject.eSet(it, value) }
                    }

                    neo4jObject.outputRefs.forEach { rel ->
                        val endNode = eObjectMap[rel.endNodeId()]
                        endNode?.let {
                            val feature = eClass.getEStructuralFeature(rel.type())
                            connectEObjects(eObject, feature, it)
                            println(" out --> ${feature.name} from: ${eClass.name}  to ${endNode.eClass().name}")
                        }
                    }

                    neo4jObject.inputRefs.forEach { rel ->
                        val startNode = eObjectMap[rel.startNodeId()]
                        startNode?.let {
                            val feature = it.eClass().getEStructuralFeature(rel.type())
                            connectEObjects(it, feature, eObject)
                            println(" in  <-- ${feature.name} from: ${startNode.eClass().name}  to ${eClass.name}")
                        }
                    }
                } //else is DataType or if node is a Map
                //println(props)

            }
        }

        println()
        eObjectMap.forEach { (k, v) ->
            println("" + k + "  " + v.eClass().name + "  " + v.eContents().size)
        }

        println()
        val eObject = eObjectMap[modelReader.rootID]
        saveModelToFile(eObject, fileName)
    }

    fun connectEObjects(startNode: EObject, feature: EStructuralFeature, endNode: EObject) {
        val value = startNode.eGet(feature)
        if (feature.isMany) {
            (value as EList<EObject>).add(endNode)
        } else startNode.eSet(feature, endNode)
        /*
        when (value) {
            is EList<*> -> value.filterIsInstance<EObject>().toMutableList().add(endNode)
            is EObject, null -> startNode.eSet(feature, endNode)
            else -> throw Exception("Some exception; value: $value \n feature: $feature \n startNode: $startNode")
        }*/
    }

    companion object {
        fun loadMetamodelFromFile(filePath : String) : EPackage {
            val extensionMap = Resource.Factory.Registry.INSTANCE.extensionToFactoryMap
            extensionMap["ecore"] = XMIResourceFactoryImpl()
            val resourceSet = ResourceSetImpl()
            val resource = resourceSet.getResource(URI.createFileURI(filePath), true)
            val rootElement = resource.contents[0]
            if (rootElement is EPackage) return rootElement else throw Exception("Wrong MetaModel")
        }

        fun saveModelToFile(eObject: EObject?, filePath : String) {
            val extensionMap = Resource.Factory.Registry.INSTANCE.extensionToFactoryMap
            extensionMap["ecore"] = XMIResourceFactoryImpl()
            extensionMap["xmi"] = XMIResourceFactoryImpl()
            val resourceSet = ResourceSetImpl()
            val resource = resourceSet.createResource(URI.createURI(filePath))
            resource.contents.add(eObject)
            resource.save(System.out, null)

            val size = resource.allContents.asSequence().toList().size
            println("\n size: $size")
        }
    }

}