package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EReference


interface IReferenceGenerator {
    fun genInterface(eRef: EReference) : String
    fun genImpl(eRef: EReference) : String

    companion object {
        fun getInstance(ctm: Boolean, upb: Int): IReferenceGenerator = when {
            upb == 1 -> if(ctm) ContainmentRefGenerator.SingleReferenceGenerator
                        else CrossRefGenerator.SingleReferenceGenerator

            upb > 1 -> if(ctm) ContainmentRefGenerator.CollectionBoundedReferenceGenerator
                        else CrossRefGenerator.CollectionBoundedReferenceGenerator

            upb == -1 -> if(ctm) ContainmentRefGenerator.CollectionUnboundedReferenceGenerator
                        else CrossRefGenerator.CollectionUnboundedReferenceGenerator

            else -> object : IReferenceGenerator {
                override fun genInterface(eRef: EReference) = ""
                override fun genImpl(eRef: EReference): String = ""
            }
        }
    }
}