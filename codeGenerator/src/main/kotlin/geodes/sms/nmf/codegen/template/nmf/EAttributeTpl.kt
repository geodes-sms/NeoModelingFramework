package geodes.sms.nmf.codegen.template.nmf

import geodes.sms.nmf.codegen.core.AbstractFeatureTemplate
import geodes.sms.nmf.codegen.core.Util
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EEnum

class EAttributeTpl(val eAttr: EAttribute) : AbstractFeatureTemplate(eAttr) {
    private val type = Util.getAttrType(eAttr)
    private val mapFunc = Util.mapFunc.getOrDefault(eAttr.eAttributeType.name, "AsObject")

    override val template: IFeatureTemplate = when {
        eAttr.upperBound == 1 -> SingleAttributeTemplate()
        eAttr.upperBound > 1 -> CollectionBoundedAttributeTemplate()
        eAttr.upperBound == -1 -> CollectionUnboundedAttributeTemplate()
        else -> object : IFeatureTemplate {
            override fun genInterface() = ""
            override fun genImplementation() = ""
        }
    }

    private inner class SingleAttributeTemplate : IFeatureTemplate {
        override fun genInterface() = StringBuilder()
            .append("\tfun set$featureNameCapitalized(v: $type?)\n")
            .append("\tfun get$featureNameCapitalized(): $type?\n")
            .toString()

        override fun genImplementation(): String {
            val unique = if (eAttr.isID) "Unique" else ""
            val str = StringBuilder().appendLine()
                .append("\toverride fun set$featureNameCapitalized(v: $type?) {\n")
                .append("\t\tif (v == null) removeProperty(\"${eAttr.name}\")\n")
                .append("\t\telse put${unique}Property(\"${eAttr.name}\", v)\n")
                .append("\t}\n")

                .appendLine()
                .append("\toverride fun get$featureNameCapitalized(): $type? {\n")
            if (eAttr.eAttributeType is EEnum) {
                str.append("\t\tval res = getProperty(\"${eAttr.name}\", AsString)\n")
                str.append("\t\treturn if (res != null) enumValueOf<$type>(res) else null\n")
            } else str.append("\t\treturn getProperty(\"${eAttr.name}\", $mapFunc)\n")

            return str.append("\t}\n").toString()
        }
    }

    private inner class CollectionBoundedAttributeTemplate : IFeatureTemplate {
        override fun genInterface() = StringBuilder()
            .append("\tfun set$featureNameCapitalized(v: List<$type>?)\n")
            .append("\tfun get$featureNameCapitalized(): List<$type>?\n")
            .toString()

        override fun genImplementation(): String {
            val tpl = if (eAttr.eAttributeType is EEnum) {
                "getProperty(\"${eAttr.name}\", AsString).map { enumValueOf<$type>(it) }"
            } else "getProperty(\"${eAttr.name}\", AsList($mapFunc))"

            return """
                
                override fun set$featureNameCapitalized(v: List<$type>?) {
                    when {
                        v == null || v.isEmpty() -> removeProperty("${eAttr.name}")
                        v.size in ${eAttr.lowerBound}..${eAttr.upperBound} -> putProperty("${eAttr.name}", v)
                        else -> throw Exception("bound limits: list size must be in ${eAttr.lowerBound}..${eAttr.upperBound}")
                    }
                }
                
                override fun get$featureNameCapitalized(): List<$type>? {
                    return $tpl
                }
            """.replaceIndent("\t").plus("\n")
        }
    }

    private inner class CollectionUnboundedAttributeTemplate : IFeatureTemplate {
        override fun genInterface() = StringBuilder()
            .append("\tfun set$featureNameCapitalized(v: List<$type>?)\n")
            .append("\tfun get$featureNameCapitalized(): List<$type>?\n")
            .toString()

        override fun genImplementation(): String {
            val tpl = if (eAttr.eAttributeType is EEnum) {
                "getProperty(\"${eAttr.name}\", AsString).map { enumValueOf<$type>(it) }"
            } else "getProperty(\"${eAttr.name}\", AsList($mapFunc))"

            return """
                
                override fun set$featureNameCapitalized(v: List<$type>?) {
                    if (v == null || v.isEmpty()) removeProperty("${eAttr.name}")
                    else putProperty("${eAttr.name}", v)
                }
                
                override fun get$featureNameCapitalized(): List<$type>? {
                    return $tpl
                }
            """.replaceIndent("\t").plus("\n")
        }
    }
}