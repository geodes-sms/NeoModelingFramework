package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EStructuralFeature


object SingleAttributeGenerator : FeatureGenerator {

    override fun genInterface(f: EStructuralFeature, type: String): String {
        return "    var ${f.name}: $type?\n"
    }

    override fun genImpl(f: EStructuralFeature, type: String): String {
        val default = if(f.defaultValueLiteral == null) Util.defaultValue[type]
        else {
            println("${f.name} defaultValue: ${f.defaultValue}")
            f.defaultValue
        }
        return "    override var ${f.name}: $type? = $default\n"
    }
}

object CollectionAttributeGenerator : FeatureGenerator {

    override fun genInterface(f: EStructuralFeature, type: String): String {
        return "    val ${f.name}: Array<$type>\n"
    }

    override fun genImpl(f: EStructuralFeature, type: String) : String {
        val default = if(f.defaultValue == null) Util.defaultValue[type] else f.defaultValue
        return "    override val ${f.name} = Array<$type>(${f.upperBound}) { $default }\n"
    }
}