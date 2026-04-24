package geodes.sms.nmf.codegen.template.ogm

import org.eclipse.emf.ecore.EReference

object CrossRefTemplate {
    object SingleReferenceTemplate : IReferenceGenerator {
        override fun genInterface(eRef: EReference): String {
            return "    var ${eRef.name}: ${eRef.eType.name.capitalize()}?\n"
        }

        override fun genImpl(eRef: EReference): String = """
            @Relationship(type = "${eRef.name}")
            override var ${eRef.name}: ${eRef.eType.name.capitalize()}? = null
        """.replaceIndent("\t").plus("\n\n")
    }

    object CollectionUnboundedReferenceTemplate : IReferenceGenerator {
        override fun genInterface(eRef: EReference): String {
            return "    val ${eRef.name}: HashSet<${eRef.eType.name.capitalize()}>\n"
        }

        override fun genImpl(eRef: EReference): String = """
            @Relationship(type = "${eRef.name}")
            override val ${eRef.name} = hashSetOf<${eRef.eType.name.capitalize()}>()
        """.replaceIndent("\t").plus("\n\n")
    }

    object CollectionBoundedReferenceTemplate : IReferenceGenerator {
        override fun genInterface(eRef: EReference): String {
            return "    val ${eRef.name}: util.BoundedList<${eRef.eType.name.capitalize()}>\n"
        }

        override fun genImpl(eRef: EReference): String = """
            @Relationship(type = "${eRef.name}")
            override val ${eRef.name} = util.BoundedList<${eRef.eType.name.capitalize()}>(${eRef.upperBound})
        """.replaceIndent("\t").plus("\n\n")
    }
}