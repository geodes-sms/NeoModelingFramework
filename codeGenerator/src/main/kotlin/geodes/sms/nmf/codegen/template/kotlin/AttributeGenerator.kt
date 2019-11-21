package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EAttribute


object SingleAttributeGenerator : IAttributeGenerator {

    override fun genInterface(eAttr: EAttribute, type: String): String {
        return "    var ${eAttr.name}: $type?\n"
    }

    override fun genImpl(eAttr: EAttribute, type: String): String {
        val default = eAttr.getDefaultValueOrElse { "null" }
        val annotations = (if (eAttr.isTransient) "@Transient " else "") + if (eAttr.isID) "@Required " else ""

        return """
            $annotations@Property(name = "${eAttr.name}")
            override var ${eAttr.name}: $type? = $default
        """.replaceIndent("\t").plus("\n\n")
    }
}

object CollectionUnboundedAttributeGenerator : IAttributeGenerator {

    override fun genInterface(eAttr: EAttribute, type: String): String {
        return "    val ${eAttr.name}: ArrayList<$type>\n"
    }

    override fun genImpl(eAttr: EAttribute, type: String): String {
        return "\n    override val ${eAttr.name} = mutableListOf<$type>()\n"
    }
}

object CollectionBoundedAttributeGenerator : IAttributeGenerator {

    override fun genInterface(eAttr: EAttribute, type: String): String {
        return "    val ${eAttr.name}: util.BoundedList<$type>\n"
    }

    override fun genImpl(eAttr: EAttribute, type: String): String {
        return "\n    override val ${eAttr.name} = util.BoundedList<$type>(${eAttr.upperBound})\n"
    }
}

/**
 * Returns the default value for the given StructuralFeature,
 * or the result of the defaultValue function if the defValue was not set.
 */
fun EAttribute.getDefaultValueOrElse(f: () -> String): String {
    val emfDefault = this.defaultValue      //heavy function inside; do not call it twice

    //Literal may be not null but defaultValue may be not convertible
    return if (defaultValueLiteral != null && emfDefault != null) {
        Util.defaultValue(emfDefault)   // 100% convertible here
    } else f()
}