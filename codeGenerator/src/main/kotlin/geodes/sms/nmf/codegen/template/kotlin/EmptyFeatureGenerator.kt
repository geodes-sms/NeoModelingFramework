package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EStructuralFeature


object EmptyFeatureGenerator : FeatureGenerator {
    override fun genImpl(f: EStructuralFeature, type: String) = ""
    override fun genInterface(f: EStructuralFeature, type: String) = ""
}