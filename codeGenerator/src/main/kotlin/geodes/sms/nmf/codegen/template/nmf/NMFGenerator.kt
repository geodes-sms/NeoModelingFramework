package geodes.sms.nmf.codegen.template.nmf

import geodes.sms.nmf.codegen.core.AbstractFeatureTemplate
import geodes.sms.nmf.codegen.core.Context
import geodes.sms.nmf.codegen.core.AbstractGenerator
import org.eclipse.emf.ecore.EClass
import java.io.File

class NMFGenerator(context: Context) : AbstractGenerator(context) {
    private val managerWriter = ModelManagerWriter(context)
    private val subClassEnumWriter = SubclassEnumWriter(context)

    override fun postProcessing() {
        managerWriter.close()
        subClassEnumWriter.close()
    }

    override fun generate(eClass: EClass) {
        val eClassTpl = EClassTpl(eClass, context.basePackagePath)
        val writer = Writer(context, eClassTpl.className)
        writer.writeHeader(eClassTpl)
        for (eAttr in eClass.eAttributes) {
            writer.writeFeature(EAttributeTpl(eAttr))
        }
        for (eRef in eClass.eReferences) {
            writer.writeFeature(EReferenceTpl(eRef, context))
        }
        writer.close()

        if (!eClass.isInterface && !eClass.isAbstract)
            managerWriter.genClass(eClassTpl.className)
        subClassEnumWriter.writeEnumFor(eClass)
    }

    private class SubclassEnumWriter(val context: Context) {
        private val str = StringBuilder()

        fun writeEnumFor(eClass: EClass) {
            val subClasses = context.getSubClasses(eClass)
            if (subClasses.isNotEmpty()) {
                val className = eClass.name.capitalize()
                val list = if (eClass.isAbstract || eClass.isInterface) subClasses else subClasses.plus(className)
                str.append("\nenum class ${className}Type { " + list.joinToString { it.capitalize() } + " }")
            }
        }

        fun close() {
            if (str.isEmpty()) return
            val writer = File(context.interfaceDir, "SubClass.kt").bufferedWriter()
            writer.write("package ${context.basePackagePath}\n")
            writer.write(str.toString())
            writer.close()
        }
    }

    private class Writer(context: Context, className: String) {
        private val interfaceWriter = File(context.interfaceDir, "${className}.kt").bufferedWriter()
        private val implWriter = File(context.implDir, "${className}Neo4jImpl.kt").bufferedWriter()

        fun writeHeader(tpl: EClassTpl) {
            interfaceWriter.write(tpl.genInterfaceHeader())
            implWriter.write(tpl.genImplHeader())
        }

        fun writeFeature(tpl: AbstractFeatureTemplate) {
            interfaceWriter.write(tpl.genInterface())
            implWriter.write(tpl.genImplementation())
        }

        fun close() {
            interfaceWriter.write("}")
            implWriter.write("}")
            implWriter.close()
            interfaceWriter.close()
        }
    }
}