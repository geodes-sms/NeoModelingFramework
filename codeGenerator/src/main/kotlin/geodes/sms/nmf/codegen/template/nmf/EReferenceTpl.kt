package geodes.sms.nmf.codegen.template.nmf

import geodes.sms.nmf.codegen.core.AbstractFeatureTemplate
import geodes.sms.nmf.codegen.core.Context
import org.eclipse.emf.ecore.EReference
import java.lang.StringBuilder

class EReferenceTpl(val eRef: EReference, context: Context) : AbstractFeatureTemplate(eRef) {
    private val subClasses: List<String> = context.getSubClasses(eRef.eReferenceType)
    private val type = eRef.eReferenceType.name.capitalize()
    private val allTypes = if (eRef.eReferenceType.isAbstract || eRef.eReferenceType.isInterface)
        subClasses else subClasses.plus(type)

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

    private val readMapFunction = when {
        allTypes.size > 1 -> {
            val str = StringBuilder("\t\t\twhen (it.label) {\n")
            for (t in allTypes) {
                str.appendLine("\t\t\t\t\"$t\" -> ${t}Neo4jImpl(it)")
            }
            str.appendLine("\t\t\t\telse -> throw Exception(\"Cannot cast to INodeController\")")
            str.appendLine("\t\t\t}")
            str.toString()
        }
        allTypes.size == 1 -> "\t\t\t${allTypes[0]}Neo4jImpl(it)\n"
        else -> ""
    }

    private interface LoadTemplate {
        fun genInterface(): String
        fun genImpl(): String
    }

    private inner class SingleRefLoadTemplate : LoadTemplate {
        override fun genInterface(): String {
            return if (allTypes.isNotEmpty())
                "\tfun get$featureNameCapitalized(): $type?\n"
            else ""
        }

        override fun genImpl(): String {
            return if (allTypes.isNotEmpty())
                StringBuilder()
                    .appendLine("\toverride fun get$featureNameCapitalized(): $type? {")
                    .appendLine("\t\tval data = loadOutConnectedNodes(\"${eRef.name}\", null, 1) {")
                    .append(readMapFunction)
                    .appendLine("\t\t}")
                    .appendLine("\t\treturn if (data.isEmpty()) null else data[0]")
                    .appendLine("\t}")
                    .toString()
            else ""
        }
    }

    private inner class MultipleRefLoadTemplate : LoadTemplate {
        override fun genInterface(): String {
            return if (allTypes.isNotEmpty())
                "\tfun get$featureNameCapitalized(limit: Int = 100): List<$type>\n"
            else ""
        }

        override fun genImpl(): String {
            return if (allTypes.isNotEmpty())
                StringBuilder()
                    .appendLine("\toverride fun get$featureNameCapitalized(limit: Int): List<$type> {")
                    .appendLine("\t\treturn loadOutConnectedNodes(\"${eRef.name}\", null, limit) {")
                    .append(readMapFunction)
                    .appendLine("\t\t}\n\t}")
                    .toString()
            else ""
        }
    }

    private inner class CrossRefTemplate(private val loadTpl: LoadTemplate) : IFeatureTemplate {
        override fun genInterface() = StringBuilder()
            .appendLine("\tfun set$featureNameCapitalized(v: $type)")
            .appendLine("\tfun unset$featureNameCapitalized(v: $type)")
            .append(loadTpl.genInterface())
            .toString()

        override fun genImplementation() = StringBuilder()
            .appendLine()
            .appendLine("\toverride fun set$featureNameCapitalized(v: $type) {")
            .appendLine("\t\tcreateOutRef(\"${eRef.name}\", v$upperBound)")
            .appendLine("\t}")
            .appendLine()
            .appendLine("\toverride fun unset$featureNameCapitalized(v: $type) {")
            .appendLine("\t\tremoveOutRef(\"${eRef.name}\", v$lowerBound)")
            .appendLine("\t}")
            .appendLine()
            .append(loadTpl.genImpl())
            .toString()
    }

    private inner class ContainmentRefTemplate(private val loadTpl: LoadTemplate) : IFeatureTemplate {
        override fun genInterface() : String {
            val str = StringBuilder()
                .appendLine("\tfun unset$featureNameCapitalized(v: $type)")
                .append(loadTpl.genInterface())
            if (allTypes.size > 1) {
                str.appendLine("\tfun add$featureNameCapitalized(type: ${type}Type): $type")
            } else if (allTypes.size == 1) {
                str.appendLine("\tfun add$featureNameCapitalized(): ${allTypes[0]}")
            }
            return str.toString()
        }

        override fun genImplementation() : String {
            val str = StringBuilder().appendLine()
            // add template
            if (allTypes.size > 1) {
                val enum = "${type}Type"
                str.appendLine("\toverride fun add$featureNameCapitalized(type: $enum): $type {")
                str.appendLine("\t\treturn when(type) {")
                for (it in allTypes) {
                    str.appendLine("\t\t\t${enum}.$it -> ${it}Neo4jImpl(createChild(\"${eRef.name}\", \"$it\"$upperBound))")
                }
                str.appendLine("\t\t}\n\t}")
            } else if (allTypes.size == 1) {
                str.appendLine("\toverride fun add$featureNameCapitalized(): ${allTypes[0]} {")
                    .appendLine("\t\treturn ${allTypes[0]}Neo4jImpl(createChild(\"${eRef.name}\", \"$type\"$upperBound))")
                    .appendLine("\t}")
            }
            str.appendLine()
            // unset template
            str.appendLine("\toverride fun unset$featureNameCapitalized(v: $type) {")
                .appendLine("\t\tremoveChild(\"${eRef.name}\", v$lowerBound)")
                .appendLine("\t}")

            return str.toString() + "\n" + loadTpl.genImpl()
        }
    }
}