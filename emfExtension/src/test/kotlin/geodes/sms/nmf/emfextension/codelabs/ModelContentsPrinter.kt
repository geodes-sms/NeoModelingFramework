package geodes.sms.nmf.emfextension.codelabs

import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EEnumLiteral
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import java.io.File


fun main(args: Array<String>) {
    //val model = "./EmfModel/instance/Document.xmi"
    val model = "./EmfModel/instance/Attributes.xmi"
    //val model = args[0]

    Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.apply {
        put("ecore", EcoreResourceFactoryImpl())
        put("xmi", XMIResourceFactoryImpl())
    }

    val resource = ResourceSetImpl().getResource(URI.createFileURI(File(model).absolutePath), true)
    for(eObject in resource.allContents) {
        val eClass = eObject.eClass()
        println(eClass.name)
        //println("container: ${eObject.eContainer()}")

        eClass.eAttributes.forEach { eAttr ->
            val value: Any? = eObject.eGet(eAttr, true)
            println("  attrName: ${eAttr.name}  value: $value  type: ${getType(
                value
            )}  isMany: ${eAttr.isMany}")
        }

        /*
        val crossIterator = eObject.eCrossReferences().iterator() as EContentsEList.FeatureIterator
        while (crossIterator.hasNext()) {
            val endEObj = crossIterator.next()
            val eRef = crossIterator.feature() as EReference
            println("  crossRef: " + endEObj.eClass().name + "  refName: " + eRef.name)
        }

        val contentIterator = eObject.eContents().iterator() as EContentsEList.FeatureIterator
        while (contentIterator.hasNext()) {
            val endEObj = contentIterator.next()
            val eRef = contentIterator.feature() as EReference
            println("  contents: " + endEObj.eClass().name + "  refName: " + eRef.name)
        }
         */

        eClass.eReferences.forEach { eRef ->
            val value: Any? = eObject.eGet(eRef, true)
            println("  refName:  ${eRef.name}  value: $value  type: ${getType(
                value
            )}")
        }
    }
}

fun getType(value: Any?): String {
    return when (value) {
        null -> "null"
        is String -> "String"
        is Int -> "Int"
        is Boolean -> "Boolean"
        is Byte -> "Byte"
        is ByteArray -> "ByteArray"
        is Char -> "Char"
        is Double -> "Double"
        is Float -> "Float"
        is Long -> "Long"
        is Short -> "Short"
        is java.math.BigDecimal -> "BigDecimal"
        is java.math.BigInteger -> "BigInteger"
        //is EEnum -> "Enum"    = EClass = EDataType  //not in model instance; is not a direct attr type
        is EEnumLiteral -> "EnumLiteral"
        is java.util.Date -> "Date"
        is Array<*> -> "Array"  //newer used. Collections are List in EMF
        //is EMap<*, *> -> "EMap"
        is EList<*> -> value.joinToString(",") {
            getType(
                it
            )
        }
        else -> "Any" //not persistable value     //EJavaObject - Any
    }
}
