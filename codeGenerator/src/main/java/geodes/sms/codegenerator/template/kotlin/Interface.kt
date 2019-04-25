package geodes.sms.codegenerator.template.kotlin


object Interface {

    fun genHeader(packageName: String, className: String, superTypes: List<String>) = """
        package geodes.sms.modeleditor.$packageName
        import geodes.sms.neo4jecore.Neo4jEObject

        interface $className : ${
        if(superTypes.isEmpty()) "Neo4jEObject" else superTypes.joinToString(", ") { it }} {

        """.trimIndent()

    fun genAttributeGetterAndSetter(attrName: String, eType: String, upperBound: Int): String {
        val type = Util.type.getOrDefault(eType, eType)
        return if (upperBound == 1) """
            fun set${attrName.capitalize()} (attrValue: $type) : Boolean
            fun get${attrName.capitalize()} () : $type?
            """
        else """
            fun set${attrName.capitalize()} (attrValue: List<$type>) : Boolean
            fun get${attrName.capitalize()} () : List<$type>?
            """
    }

    fun genRefSetter(refName: String, endClass: String, upperBound: Int): String {
        return if (upperBound == 1) """
            fun set${refName.capitalize()}(endNode: ${endClass.capitalize()}) : Boolean
            """
        else """
            fun add${refName.capitalize()}(endNode: ${endClass.capitalize()}) : Boolean
            """
    }

    object ManagerClass {

        fun genHeader(packageName: String) = """
            package  geodes.sms.modeleditor.$packageName

            interface ModelManager {
        """.trimIndent()

        fun addClass(className: String) = """
            fun create$className() : $className
            fun get${className}ByID(id: Int) : $className?
        """
    }
}