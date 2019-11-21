package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EReference
import java.io.BufferedWriter


abstract class EClassWriter(protected val eClass: EClass) : AutoCloseable {

    protected val className = eClass.name.capitalize()
    protected val packageName = eClass.ePackage.name.decapitalize()

    abstract val writer: BufferedWriter
    abstract val eReferences: List<EReference>
    abstract val eAttributes: List<EAttribute>

    fun doGenerate() {
        writeHeader()
        writeAttributes(eAttributes)
        writeReferences(eReferences)
        close()
    }

    abstract fun writeHeader()
    abstract fun writeAttributes(eAttrs: List<EAttribute>)
    abstract fun writeReferences(eRefs: List<EReference>)

    override fun close() {
        writer.write("}")
        writer.close()
    }
}