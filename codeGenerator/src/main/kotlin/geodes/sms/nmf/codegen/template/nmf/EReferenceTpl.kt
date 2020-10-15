package geodes.sms.nmf.codegen.template.nmf

import geodes.sms.nmf.codegen.core.AbstractFeatureTemplate
import org.eclipse.emf.ecore.EReference
import java.lang.StringBuilder

class EReferenceTpl(val eRef: EReference, val subClasses: List<String>) : AbstractFeatureTemplate(eRef) {
    private val type = eRef.eReferenceType.name.capitalize()
    private val isETypeAbstract = eRef.eReferenceType.isAbstract || eRef.eReferenceType.isInterface
    private val allTypes = if (isETypeAbstract) subClasses else subClasses.plus(type)

    private val readMapFunction = if (subClasses.isNotEmpty()) {
        val str = StringBuilder("when (it.label) {\n")
        for (t in allTypes) {
            str.append("\t\"$t\" -> ${t}Neo4jImpl(it)\n")
        }
        str.append("\telse -> throw Exception(\"Cannot cast InodeController\")\n")
        str.append("}")
        str.toString()
    } else if (!isETypeAbstract) "${type}Neo4jImpl(it)" else ""

    private val loadTpl = when (eRef.upperBound) {
        1 -> SingleRefLoadTemplate()
        -1, -2, in 2..Int.MAX_VALUE -> MultipleRefLoadTemplate()
        else -> object : LoadTemplate {
            override fun genInterface() = ""
            override fun genImpl() = ""
        }
    }
    override val template = if (eRef.isContainment) ContainmentRefTemplate(loadTpl) else CrossRefTemplate(loadTpl)

    private val upperBound = when (val v = eRef.upperBound) {
        -1, -2 -> ""
        else -> ", $v"
    }

    private val lowerBound = when (val v = eRef.lowerBound) {
        0, -1, -2 -> ""
        else -> ", $v"
    }

    private interface LoadTemplate {
        fun genInterface(): String
        fun genImpl(): String
    }

    private inner class SingleRefLoadTemplate : LoadTemplate {
        override fun genInterface(): String {
            return if (allTypes.isNotEmpty())
                "\tfun load$featureNameCapitalized(): $type?\n"
            else ""
        }

        override fun genImpl(): String {
            return if (allTypes.isNotEmpty()) """
                override fun load$featureNameCapitalized(): $type? {
                    val data = loadOutConnectedNodes("${eRef.name}", null, 1, "") {
                        $readMapFunction
                    }
                    return if (data.isEmpty()) null else data[0]
                }
                """.replaceIndent("\t").plus("\n")
            else ""
        }
    }

    private inner class MultipleRefLoadTemplate : LoadTemplate {
        override fun genInterface(): String {
            return if (allTypes.isNotEmpty())
                "\tfun load$featureNameCapitalized(limit: Int = 100): List<$type>\n"
            else ""
        }

        override fun genImpl(): String {
            return if (allTypes.isNotEmpty()) """
                override fun load$featureNameCapitalized(limit: Int): List<$type> {
                    return loadOutConnectedNodes("${eRef.name}", null, limit, "") {
                        $readMapFunction
                    }
                }
                """.replaceIndent("\t").plus("\n")
            else ""
        }
    }

    private inner class CrossRefTemplate(private val loadTpl: LoadTemplate) : IFeatureTemplate {
        override fun genInterface() = """
            fun set$featureNameCapitalized(v: $type)
            fun unset$featureNameCapitalized(v: $type)
            ${loadTpl.genInterface()}
        """.replaceIndent("\t").plus("\n")

        override fun genImplementation(): String {
            return loadTpl.genImpl() + """
                override fun set$featureNameCapitalized(v: $type) {
                    createOutRef("${eRef.name}", v$upperBound)
                }
                override fun unset$featureNameCapitalized(v: $type) {
                    removeOutRef("${eRef.name}", v$lowerBound)
                }
            """.replaceIndent("\t").plus("\n")
        }
    }

    private inner class ContainmentRefTemplate(private val loadTpl: LoadTemplate) : IFeatureTemplate {
        override fun genInterface() : String {
            val addTpl = subClasses.joinToString("\n") { subType ->
                "\tfun add$subType$featureNameCapitalized(): $subType"
            }

            return addTpl + "\n" + loadTpl.genInterface() + """
                ${if (!isETypeAbstract) "fun add$featureNameCapitalized(): $type" else ""}
                fun remove$featureNameCapitalized(v: $type)
            """.replaceIndent("\t").plus("\n")
        }

        override fun genImplementation() : String {
            val addTpl = subClasses.joinToString("") { subType ->
                genAddSubtypeImpl(subType)
            }.plus(if (!isETypeAbstract) genAddImpl() else "")

            return addTpl + "\n" + loadTpl.genImpl() + """
                override fun remove$featureNameCapitalized(v: $type) {
                    removeChild("${eRef.name}", v$lowerBound)
                }
            """.replaceIndent("\t").plus("\n")
        }

        private fun genAddSubtypeImpl(subType: String) = """
            override fun add$subType$featureNameCapitalized(): $subType {
                return ${subType}Neo4jImpl(createChild("${eRef.name}", "$subType"$upperBound))
            }
        """.replaceIndent("\t").plus("\n")

        private fun genAddImpl() = """
            override fun add$featureNameCapitalized(): $type {
                return ${type}Neo4jImpl(createChild("${eRef.name}", "$type"$upperBound))
            }
        """.replaceIndent("\t").plus("\n")
    }
}