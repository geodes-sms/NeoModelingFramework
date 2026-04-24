package geodes.sms.nmf.emfextension.codelabs

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import java.io.File


fun main() {
    val model = "./EmfModel/metamodel/AttributesTest.ecore"

    Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.apply {
        put("ecore", EcoreResourceFactoryImpl())
        //put("xmi", XMIResourceFactoryImpl())
    }

    val resourceSet = ResourceSetImpl()
    val resource = resourceSet.getResource(URI.createFileURI(File(model).absolutePath), true)
    resource.allContents.asSequence().filterIsInstance<EStructuralFeature>().forEach {
        val def = it.defaultValue
        println("${it.name}   default: $def   literal: ${it.defaultValueLiteral}")

        if (def is ByteArray) {
            val str = String(def)
            val bytes = "hello emf".toByteArray()

            println("   $str")
            println("   $bytes")
            println("   equals: " + compareBytes(
                bytes,
                def
            )
            )
        }
    }
}

fun compareBytes(b1: ByteArray, b2: ByteArray): Boolean {
    var res = true
    if (b1.size == b2.size) {
        for (i in b1.indices) {
            println("${b1[i]}   ${b2[i]}")
            if (b1[i].compareTo(b2[i]) != 0) res = false

        }

    } else {
        println("size not equals")
        res = false
    }
    return res
}