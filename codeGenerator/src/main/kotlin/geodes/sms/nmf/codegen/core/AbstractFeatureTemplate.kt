package geodes.sms.nmf.codegen.core

import org.eclipse.emf.ecore.EStructuralFeature

abstract class AbstractFeatureTemplate(feature: EStructuralFeature) {
    val featureNameCapitalized = feature.name.capitalize()
    protected abstract val template: IFeatureTemplate
    fun genInterface(): String = template.genInterface()
    fun genImplementation(): String = template.genImplementation()

    protected interface IFeatureTemplate {
        fun genInterface(): String
        fun genImplementation(): String
    }
}