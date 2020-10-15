package geodes.sms.nmf.codegen.template.nmf

import geodes.sms.nmf.codegen.core.AbstractFeatureTemplate
import geodes.sms.nmf.codegen.core.Context
import geodes.sms.nmf.codegen.core.AbstractGenerator
import org.eclipse.emf.ecore.EClass
import java.io.File

class NMFGenerator(context: Context) : AbstractGenerator(context) {
    private val managerWriter = ModelManagerWriter(context)

    override fun postProcessing() {
        managerWriter.close()
    }

    override fun generate(eClass: EClass) {
        if (!eClass.isInterface && !eClass.isAbstract)
            managerWriter.genClass(eClass.name)
        val subClasses = context.getSubClasses(eClass)
        val eClassTpl = EClassTpl(eClass, subClasses.isNotEmpty(), context.basePackagePath)

        val writer = if (eClass.isInterface)
            InterfaceWriter(context, eClassTpl.className)
        else ImplementationWriter(context, eClassTpl.className)

        writer.writeHeader(eClassTpl)
        for (eAttr in eClass.eAttributes) {
            writer.writeFeature(EAttributeTpl(eAttr))
        }
        for (eRef in eClass.eReferences) {
            writer.writeFeature(EReferenceTpl(eRef, context.getSubClasses(eRef.eReferenceType)))
        }
        writer.close()
    }

    private open class InterfaceWriter(context: Context, className: String) {
        private val writer = File(context.interfaceDir, "${className}.kt").bufferedWriter()

        open fun writeHeader(tpl: EClassTpl) {
            writer.write(tpl.genInterfaceHeader())
        }

        open fun writeFeature(tpl: AbstractFeatureTemplate) {
            writer.write(tpl.genInterface())
        }

        open fun close() {
            writer.write("}")
            writer.close()
        }
    }

    private class ImplementationWriter(context: Context, className: String): InterfaceWriter(context, className) {
        private val implWriter = File(context.implDir, "${className}Neo4jImpl.kt").bufferedWriter()

        override fun writeHeader(tpl: EClassTpl) {
            implWriter.write(tpl.genImplHeader())
            super.writeHeader(tpl)
        }

        override fun writeFeature(tpl: AbstractFeatureTemplate) {
            implWriter.write(tpl.genImplementation())
            super.writeFeature(tpl)
        }

        override fun close() {
            implWriter.write("}")
            implWriter.close()
            super.close()
        }
    }
}