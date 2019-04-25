package geodes.sms.modelloader.emf2neo4j

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl


interface EmfModelLoader  {
    fun load(dbWriter : Neo4jBufferedWriter)

    companion object {
        fun createFromContent(modelPath: String) : EmfModelLoader {
            val extensionMap = Resource.Factory.Registry.INSTANCE.extensionToFactoryMap
            extensionMap["ecore"] = XMIResourceFactoryImpl()
            extensionMap["xmi"] = XMIResourceFactoryImpl()
            val resourceSet = ResourceSetImpl()
            val resource = resourceSet.getResource(URI.createFileURI(modelPath), true)

            val adapter = ECrossReferenceAdapter()
            resourceSet.eAdapters().add(0, adapter)

            return when (resource.contents[0]) {
                is EPackage -> MetaModelLoader(resource, adapter)
                is EObject -> ModelInstanceLoader(resource, adapter)
                else -> throw Exception("Unknown model format $modelPath")
            }
        }
    }
}
