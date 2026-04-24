package geodes.sms.nmf.codegen.template.ogm

import org.eclipse.emf.ecore.EReference


interface IReferenceGenerator {
    fun genInterface(eRef: EReference) : String
    fun genImpl(eRef: EReference) : String

    companion object {
        fun getInstance(ctm: Boolean, upb: Int): IReferenceGenerator = when {
            upb == 1 -> if(ctm) ContainmentRefGenerator.SingleReferenceGenerator
                        else CrossRefTemplate.SingleReferenceTemplate

            upb > 1 -> if(ctm) ContainmentRefGenerator.CollectionBoundedReferenceGenerator
                        else CrossRefTemplate.CollectionBoundedReferenceTemplate

            upb == -1 -> if(ctm) ContainmentRefGenerator.CollectionUnboundedReferenceGenerator
                        else CrossRefTemplate.CollectionUnboundedReferenceTemplate

            else -> object : IReferenceGenerator {
                override fun genInterface(eRef: EReference) = ""
                override fun genImpl(eRef: EReference): String = ""
            }
        }
    }
}