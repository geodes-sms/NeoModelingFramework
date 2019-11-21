package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EAttribute

interface IAttributeGenerator {
    fun genInterface(eAttr: EAttribute, type: String) : String
    fun genImpl(eAttr: EAttribute, type: String) : String

    companion object {
        fun getInstance(upb: Int) = when {
            upb == 1 -> SingleAttributeGenerator
            upb  > 1 -> CollectionBoundedAttributeGenerator
            upb == -1 -> CollectionUnboundedAttributeGenerator
            else -> object : IAttributeGenerator {
                override fun genInterface(eAttr: EAttribute, type: String) = ""
                override fun genImpl(eAttr: EAttribute, type: String) = ""
            }
        }
    }
}