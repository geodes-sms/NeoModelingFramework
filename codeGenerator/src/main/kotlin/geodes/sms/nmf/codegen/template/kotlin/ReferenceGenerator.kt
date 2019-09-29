package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EStructuralFeature


object SingleReferenceGenerator : FeatureGenerator {

    override fun genInterface(f: EStructuralFeature, type: String): String {
        return "    var ${f.name}: $type\n"
    }

    override fun genImpl(f: EStructuralFeature, type: String): String {
        return """
        @Relationship(type = "${f.name}")
        override lateinit var ${f.name}: $type
        
        
        """//.trimIndent()
    }
}

object CollectionReferenceFenerator : FeatureGenerator {

    override fun genInterface(f: EStructuralFeature, type: String): String {
        return "    val ${f.name}: HashSet<$type>\n"
    }

    override fun genImpl(f: EStructuralFeature, type: String): String {
        return """
        @Relationship(type = "${f.name}")
        override val ${f.name} = hashSetOf<$type>()
        
        
        """//.trimIndent()
    }
}