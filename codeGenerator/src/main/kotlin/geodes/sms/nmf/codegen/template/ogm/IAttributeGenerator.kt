package geodes.sms.nmf.codegen.template.ogm

import org.eclipse.emf.ecore.EAttribute

interface IAttributeGenerator {
    fun genInterface(eAttr: EAttribute, type: String) : String
    fun genImpl(eAttr: EAttribute, type: String) : String

    companion object {
        fun getInstance(upb: Int) = when {
            upb == 1 -> SingleAttributeTemplate
            upb  > 1 -> CollectionBoundedAttributeTemplate
            upb == -1 -> CollectionUnboundedAttributeTemplate
            else -> object : IAttributeGenerator {
                override fun genInterface(eAttr: EAttribute, type: String) = ""
                override fun genImpl(eAttr: EAttribute, type: String) = ""
            }
        }
    }
}