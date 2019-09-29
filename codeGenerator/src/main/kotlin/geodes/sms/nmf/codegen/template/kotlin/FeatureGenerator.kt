package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EStructuralFeature


interface FeatureGenerator {
    fun genInterface(f: EStructuralFeature, type: String) : String
    fun genImpl(f: EStructuralFeature, type: String) : String

    companion object {
        fun getAttrInstance(upb: Int) = when {
            upb == 1 -> SingleAttributeGenerator
            upb  > 1 -> CollectionAttributeGenerator
            else -> EmptyFeatureGenerator
        }

        fun getRefInstance(upb: Int) = when {
            upb == 1 -> SingleReferenceGenerator
            upb  > 1 -> CollectionReferenceFenerator
            else -> EmptyFeatureGenerator
        }
    }
}