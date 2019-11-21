package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EReference


object ContainmentRefGenerator {

    object SingleReferenceGenerator : IReferenceGenerator {

        override fun genInterface(eRef: EReference): String {
            return "    fun set${eRef.name.capitalize()}(): ${eRef.eType.name.capitalize()}\n"
        }

        override fun genImpl(eRef: EReference): String {
            val type = eRef.eType.name.capitalize()
            return """
                @Relationship(type = "${eRef.name}")
                var ${eRef.name}: $type? = null
                    private set
                
                override fun set${eRef.name.capitalize()}() : $type {
                    val res = ${type}Neo4jImpl()
                    ${eRef.name} = res
                    return res
                }
        """.replaceIndent("\t").plus("\n\n")
        }
    }

    object CollectionUnboundedReferenceGenerator : IReferenceGenerator {

        override fun genInterface(eRef: EReference): String {
            return "    fun add${eRef.name.capitalize()}(): ${eRef.eType.name.capitalize()}\n"
        }

        override fun genImpl(eRef: EReference): String {
            val type = eRef.eType.name.capitalize()
            return """
                @Relationship(type = "${eRef.name}")
                val ${eRef.name} = hashSetOf<$type>()
                
                override fun add${eRef.name.capitalize()}(): $type {
                    val res = ${type}Neo4jImpl()
                    ${eRef.name}.add(res)
                    return res
                }
        """.replaceIndent("\t").plus("\n\n")
        }
    }

    object CollectionBoundedReferenceGenerator : IReferenceGenerator {

        override fun genInterface(eRef: EReference): String {
            return "    fun add${eRef.name.capitalize()}(): ${eRef.eType.name.capitalize()}\n"
        }

        override fun genImpl(eRef: EReference): String {
            val type = eRef.eType.name.capitalize()
            return """
                @Relationship(type = "${eRef.name}")
                val ${eRef.name} = util.BoundedList<$type>(${eRef.upperBound})
                
                override fun add${eRef.name.capitalize()}(): $type {
                    val res = ${type}Neo4jImpl()
                    ${eRef.name}.add(res)
                    return res
                }
        """.replaceIndent("\t").plus("\n\n")
        }
    }
}