package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EReference
import java.io.File


class InterfaceWriter(eClass: EClass, dir: String) : EClassWriter(eClass) {

    override val writer = File(dir, "${className}.kt").bufferedWriter()
    override val eAttributes: List<EAttribute> = eClass.eAttributes
    override val eReferences: List<EReference> = eClass.eReferences

    override fun writeHeader() {
        val superTypes = if (eClass.eSuperTypes.size > 0)
            eClass.eSuperTypes.joinToString(prefix = ": ") { it.name.capitalize() }
        else ""

        val header = """
            package $packageName
        
            interface $className $superTypes {
        """.trimIndent().plus("\n")
        writer.write(header)
    }

    override fun writeAttributes(eAttrs: List<EAttribute>) = eAttrs.forEach { eAttr ->
        val attrGenerator = IAttributeGenerator.getInstance(eAttr.upperBound)
        writer.write(attrGenerator.genInterface(eAttr, Util.getAttrType(eAttr)))
    }

    override fun writeReferences(eRefs: List<EReference>) = eRefs.forEach { eRef ->
        val refGenerator = IReferenceGenerator.getInstance(eRef.isContainment, eRef.upperBound)
        writer.write(refGenerator.genInterface(eRef))
    }
}