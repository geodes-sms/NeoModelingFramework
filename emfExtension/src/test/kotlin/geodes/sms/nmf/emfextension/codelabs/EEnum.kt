package geodes.sms.nmf.emfextension.codelabs

import org.eclipse.emf.common.util.TreeIterator
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.*
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import java.io.File


fun main() {

    val factory = EcoreFactory.eINSTANCE
    val ePackage = EcorePackage.eINSTANCE

    val enum = factory.createEEnum()
    enum.name = "Color"
    val attr = factory.createEAttribute()
    attr.eType = enum

    val iterator = ePackage.eTreeIterator
    println(iterator is EDataType)

/*
    //val model = "./EmfModel/metamodel/AttributesTest.ecore"

    Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.apply {
        put("ecore", EcoreResourceFactoryImpl())
        //put("xmi", XMIResourceFactoryImpl())
    }

    val resourceSet = ResourceSetImpl()
    val resource = resourceSet.getResource(URI.createFileURI(File(model).absolutePath), true)
    resource.allContents.asSequence().filterIsInstance<EAttribute>().forEach {
        println(it.eAttributeType.toString() + "  " + it.eAttributeType.instanceClassName)
    }*/
}





