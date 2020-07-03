package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EReference
import geodes.sms.nmf.codegen.template.kotlin.Util.eType


object ContainmentRefGenerator {

    object SingleReferenceGenerator : IReferenceGenerator {

        override fun genInterface(eRef: EReference): String {
            return "    fun <T:${eRef.eType()}> set${eRef.name.capitalize()}(c: Class<T>): ${eRef.eType()}\n"
        }

        override fun genImpl(eRef: EReference): String = """
            @Relationship(type = "${eRef.name}")
            var ${eRef.name}: ${eRef.eType()}? = null
                private set
            
            override fun <T:${eRef.eType()}> set${eRef.name.capitalize()}(c: Class<T>): ${eRef.eType()} {
                val res = c.newInstance()
                ${eRef.name} = res
                return res
            }
        """.replaceIndent("\t").plus("\n\n")
    }

    object CollectionUnboundedReferenceGenerator : IReferenceGenerator {

        override fun genInterface(eRef: EReference): String {
            val type = eRef.eType()
            return "    fun <T:$type> add${eRef.name.capitalize()}(c: Class<T>): $type\n"
        }

        override fun genImpl(eRef: EReference): String {
            val type = eRef.eType()
            return """
                @Relationship(type = "${eRef.name}")
                val ${eRef.name} = hashSetOf<$type>()
                
                override fun <T:$type> add${eRef.name.capitalize()}(c: Class<T>): $type {
                    val res = c.newInstance()
                    ${eRef.name}.add(res)
                    return res
                }
        """.replaceIndent("\t").plus("\n\n")
        }
    }

    object CollectionBoundedReferenceGenerator : IReferenceGenerator {

        override fun genInterface(eRef: EReference): String {
            val type = eRef.eType()
            return "    fun <T:$type> add${eRef.name.capitalize()}(c: Class<T>): $type\n"
        }

        override fun genImpl(eRef: EReference): String {
            val type = eRef.eType()
            return """
                @Relationship(type = "${eRef.name}")
                val ${eRef.name} = util.BoundedList<$type>(${eRef.upperBound})
                
                override fun <T:$type> add${eRef.name.capitalize()}(c: Class<T>): $type {
                    val res = c.newInstance()
                    ${eRef.name}.add(res)
                    return res
                }
        """.replaceIndent("\t").plus("\n\n")
        }
    }
}