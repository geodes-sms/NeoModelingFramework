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
        override fun genInterface(): String {
            return """
                fun set$featureNameCapitalized(v: $type?)
                fun get$featureNameCapitalized(): $type?
            """.replaceIndent("\t").plus("\n")
        }

        override fun genImplementation(): String {
            val unique = if (eAttr.isID) "Unique" else ""
            val tpl = if (eAttr.eAttributeType is EEnum)
                """val res = getProperty("${eAttr.name}", AsString)
                    return if (res != null) enumValueOf<$type>(res) else null"""
            else "return getProperty(\"${eAttr.name}\", $mapFunc)"

            return """
                override fun set$featureNameCapitalized(v: $type?) {
                    if (v == null) removeProperty("${eAttr.name}")
                    else put${unique}Property("${eAttr.name}", v)
                }
                override fun get$featureNameCapitalized(): $type? {
                    $tpl
                }
            """.replaceIndent("\t").plus("\n")
        }
    }

    private inner class CollectionBoundedAttributeTemplate : IFeatureTemplate {
        override fun genInterface(): String {
            return """
                fun set$featureNameCapitalized(v: List<$type>?)
                fun get$featureNameCapitalized(): List<$type>?
            """.replaceIndent("\t").plus("\n")
        }

        override fun genImplementation(): String {
            val tpl = if (eAttr.eAttributeType is EEnum) {
                "getProperty(\"${eAttr.name}\", AsString).map { enumValueOf<$type>(it) }"
            } else "getProperty(\"${eAttr.name}\", AsList($mapFunc))"

            return """
                override fun set$featureNameCapitalized(v: List<$type>?) {
                    when {
                        v == null || v.isEmpty() -> removeProperty("${eAttr.name}")
                        v.size in ${eAttr.lowerBound}..${eAttr.upperBound} -> putProperty("${eAttr.name}", v)
                        throw Exception("bound limits: list size must be in ${eAttr.lowerBound}..${eAttr.upperBound}")
                    }
                }
                override fun get$featureNameCapitalized(): $type? {
                    return $tpl
                }
            """.replaceIndent("\t").plus("\n")
        }
    }

    private inner class CollectionUnboundedAttributeTemplate : IFeatureTemplate {
        override fun genInterface(): String {
            return """
                fun set$featureNameCapitalized(v: List<$type>?)
                fun get$featureNameCapitalized(): List<$type>?
            """.replaceIndent("\t").plus("\n")
        }

        override fun genImplementation(): String {
            val tpl = if (eAttr.eAttributeType is EEnum) {
                "getProperty(\"${eAttr.name}\", AsString).map { enumValueOf<$type>(it) }"
            } else "getProperty(\"${eAttr.name}\", AsList($mapFunc))"

            return """
                override fun set$featureNameCapitalized(v: List<$type>?) {
                    if (v == null || v.isEmpty()) removeProperty("${eAttr.name}")
                    else putProperty("${eAttr.name}", v)
                }
                override fun get$featureNameCapitalized(): $type? {
                    return $tpl
                }
            """.replaceIndent("\t").plus("\n")
        }
    }
}