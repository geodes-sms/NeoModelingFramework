package geodes.sms.nmf.codegen

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import java.io.File


fun main(args: Array<String>) {

//    './' is a project level directory
//    val outputPath = "./modelEditor/src/main/kotlin"
//    val metamodel = "./EmfModel/metamodel/AttributesTest.ecore"
//    val metamodel = "./EmfModel/metamodel/MindMaps.ecore"

    val metamodel = args[0]
    val outputPath = args[1]

    Resource.Factory.Registry.INSTANCE.extensionToFactoryMap["ecore"] = XMIResourceFactoryImpl()
    val resourceSet = ResourceSetImpl()
    val resource = resourceSet.getResource(URI.createURI(File(metamodel).absolutePath), true)

    resource.contents.filterIsInstance<EPackage>().forEach { ePackage ->
        val generator = KotlinCodeGenerator(ePackage, outputPath)
        generator.generate()
        println("Code generation for ${ePackage.name} finished")
    }
}