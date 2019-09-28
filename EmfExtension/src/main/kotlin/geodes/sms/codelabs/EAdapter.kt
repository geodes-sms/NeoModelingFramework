package geodes.sms.codelabs

import org.eclipse.emf.common.notify.Notification
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EcoreFactory
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EContentAdapter
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import java.io.File

fun main() {
    //val model = "./EmfModel/instance/Document.xmi"
    val model = "./EmfModel/metamodel/Library.ecore"

    Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.apply {
        put("ecore", EcoreResourceFactoryImpl())
        put("xmi", XMIResourceFactoryImpl())
    }

    val factory = EcoreFactory.eINSTANCE
    val resourceSet = ResourceSetImpl()
    val resource = resourceSet.getResource(URI.createFileURI(File(model).absolutePath), true)

    resource.eAdapters().add(object : EContentAdapter() {
        override fun notifyChanged(notification: Notification) {
            println("change ${notification.eventType}")
        }
    })

    when (val ePackage = resource.contents[0]) {
        is EPackage -> {
            ePackage.eClassifiers.add(factory.createEClass())
        }
        else -> { println("The resource is not an ECore model") }
    }

    resource.allContents.forEach { eObj ->
        println(eObj.eClass().name)
    }
}