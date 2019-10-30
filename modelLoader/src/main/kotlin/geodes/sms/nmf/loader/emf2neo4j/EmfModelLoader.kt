package geodes.sms.nmf.loader.emf2neo4j

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import java.io.File


interface EmfModelLoader  {

    //fun load()

    companion object {
        fun createFromContent(modelPath: String) : EmfModelLoader {

            //all extensions [xmi; ecore] are mandatory
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
            resource.contents.forEach { root ->

            }


            return when (resource.contents[0]) {
                //is EPackage -> EcoreLoader(resource, adapter)
                //is EObject -> ReflectiveLoader(resource, adapter)
                else -> throw Exception("Unknown model format $modelPath")
            }
        }
    }
}
