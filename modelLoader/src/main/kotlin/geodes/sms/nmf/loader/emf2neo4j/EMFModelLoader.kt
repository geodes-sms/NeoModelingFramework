package geodes.sms.nmf.loader.emf2neo4j

import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import java.io.File


interface EmfModelLoader {
    companion object {
        fun load(modelPath: String, writer: GraphBatchWriter): Pair<Int, Int> {
            Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.apply {
                put("ecore", EcoreResourceFactoryImpl())
                put("*", XMIResourceFactoryImpl())
            }
            // createFileURI method is able to locate metamodel by xsi:schemaLocation
            // absolute path is important here !!
            val resource = ResourceSetImpl().getResource(URI.createFileURI(File(modelPath).absolutePath), true)
            return when (val root = resource.contents[0]) {
                is EPackage -> EcoreLoader(writer).load(root)
                is EObject -> ReflectiveBatchLoader(writer).load(root)
                else -> throw Exception("Unknown element type $root in the model $modelPath. SKIPPING the model")
            }
        }
    }
}