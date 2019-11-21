package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EReference


object CrossRefGenerator {

    object SingleReferenceGenerator : IReferenceGenerator {

        override fun genInterface(eRef: EReference): String {
            return "    var ${eRef.name}: ${eRef.eType.name.capitalize()}?\n"
        }

        override fun genImpl(eRef: EReference): String = """
            @Relationship(type = "${eRef.name}")
            override var ${eRef.name}: ${eRef.eType.name.capitalize()}? = null
        """.replaceIndent("\t").plus("\n\n")
    }

    object CollectionUnboundedReferenceGenerator : IReferenceGenerator {

        override fun genInterface(eRef: EReference): String {
            return "    val ${eRef.name}: HashSet<${eRef.eType.name.capitalize()}>\n"
        }

        override fun genImpl(eRef: EReference): String = """
            @Relationship(type = "${eRef.name}")
            override val ${eRef.name} = hashSetOf<${eRef.eType.name.capitalize()}>()
        """.replaceIndent("\t").plus("\n\n")
    }

    object CollectionBoundedReferenceGenerator : IReferenceGenerator {

        override fun genInterface(eRef: EReference): String {
            return "    val ${eRef.name}: util.BoundedList<${eRef.eType.name.capitalize()}>\n"
        }

        override fun genImpl(eRef: EReference): String = """
            @Relationship(type = "${eRef.name}")
            override val ${eRef.name} = util.BoundedList<${eRef.eType.name.capitalize()}>(${eRef.upperBound})
        """.replaceIndent("\t").plus("\n\n")
    }
}