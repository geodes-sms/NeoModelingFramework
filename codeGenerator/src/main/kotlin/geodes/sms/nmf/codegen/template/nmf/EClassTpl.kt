package geodes.sms.nmf.codegen.template.nmf

import org.eclipse.emf.ecore.EClass
import java.lang.StringBuilder

class EClassTpl(val eClass: EClass, val basePackagePath: String) {
    val className = eClass.name.capitalize()

    fun genInterfaceHeader(): String {
        val superTypes = if (eClass.eSuperTypes.size > 0)
            eClass.eSuperTypes.joinToString { it.name.capitalize() }
        else "INodeEntity"

        return """
            package $basePackagePath

            import geodes.sms.neo4j.io.entity.INodeEntity
        
            interface $className : $superTypes {

        """.trimIndent()
    }

    fun genImplHeader(): String {
        val abstract = if (eClass.isAbstract) "abstract " else "" //if (subClassed) "open " else ""
        var ncImplementor = ", INodeController by nc"
        val superTypes = StringBuilder()
        for (s in eClass.eSuperTypes) {
            val name = s.name.capitalize()
            if (s.isAbstract) {
                ncImplementor = ""
                superTypes.append(", ${name}Neo4jImpl(nc)")
            }
            else superTypes.append(", $name by ${name}Neo4jImpl(nc)")
        }
        superTypes.append(ncImplementor)
        val header =  """
            package $basePackagePath.neo4jImpl
            
            import geodes.sms.neo4j.io.controllers.INodeController
            import geodes.sms.neo4j.io.type.*
            import $basePackagePath.*
            
            ${abstract}class ${className}Neo4jImpl(nc: INodeController) : $className$superTypes {
        """.trimIndent()

        val str = StringBuilder().append(header)
        if (eClass.eSuperTypes.size > 1 || (eClass.eSuperTypes.size == 1 && !eClass.eSuperTypes[0].isAbstract)) {
            str.appendLine()
            str.appendLine("\toverride val _id by nc::_id")
            str.appendLine("\toverride val label by nc::label")
        }
        return str.toString()
    }
}