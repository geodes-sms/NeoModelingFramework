package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EReference
import java.io.File


class ImplementationWriter(eClass: EClass, dir: String) : EClassWriter(eClass) {

    override val writer = File(dir, "${className}Neo4jImpl.kt").bufferedWriter()
    override val eAttributes: List<EAttribute> = eClass.eAllAttributes
    override val eReferences: List<EReference> = eClass.eAllReferences

    override fun writeHeader() {
        val header = """
            package ${packageName}.neo4jImpl
            
            import ${packageName}.*
            import org.neo4j.io2.annotation.*
    
            @NodeEntity(label = "$className")
            class ${className}Neo4jImpl : $className {
            
                @Id @GeneratedValue
                val id: Long? = null
        """.trimIndent().plus("\n\n")
        writer.write(header)
    }

    override fun writeAttributes(eAttrs: List<EAttribute>) = eAttrs.forEach { eAttr ->
        val attrGenerator = IAttributeGenerator.getInstance(eAttr.upperBound)
        writer.write(attrGenerator.genImpl(eAttr, Util.getAttrType(eAttr)))
        //genImpl !!!
    }

    override fun writeReferences(eRefs: List<EReference>) = eRefs.forEach { eRef ->
        val refGenerator = IReferenceGenerator.getInstance(eRef.isContainment, eRef.upperBound)
        writer.write(refGenerator.genImpl(eRef))
    }
}