package geodes.sms.nmf.codegen

import geodes.sms.nmf.codegen.core.Context
import geodes.sms.nmf.codegen.template.nmf.NMFGenerator
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import java.io.File

fun main(args: Array<String>) {
    //  './' is a project level directory
    val outputPath = "./modelEditor/src/main/kotlin/geodes/sms/nmf/editor"
    val packagePath = "geodes.sms.nmf.editor"
    val metamodel = "./EmfModel/metamodel/Graph.ecore"
    //val metamodel = "./EmfModel/metamodel/Attributes.ecore"
    //val metamodel = "./EmfModel/metamodel/Railway.ecore"
    //val metamodel = "./EmfModel/metamodel/SuperTypes.ecore"
    //val metamodel = "./EmfModel/metamodel/Latex.ecore"
    //val metamodel = "./EmfModel/metamodel/Ecore.ecore"

    //val metamodel = args[0]
    //val outputPath = args[1]

    Resource.Factory.Registry.INSTANCE.extensionToFactoryMap["ecore"] = XMIResourceFactoryImpl()
    val resource = ResourceSetImpl().getResource(URI.createURI(File(metamodel).absolutePath), true)

    val root = resource.contents[0]
    if (root is EPackage) {
        NMFGenerator(Context(root, packagePath, outputPath)).generate()
        println("Code generation for package '${root.name}' is finished")
    } else {
        println("Model must contain EPackage as root element")
    }
}