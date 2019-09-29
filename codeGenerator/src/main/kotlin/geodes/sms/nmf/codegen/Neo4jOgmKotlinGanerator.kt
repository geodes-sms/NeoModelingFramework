package geodes.sms.nmf.codegen

import geodes.sms.nmf.codegen.template.kotlin.FeatureGenerator
import geodes.sms.nmf.codegen.template.kotlin.Util
import org.eclipse.emf.ecore.*
import java.io.File


class KotlinCodeGenerator(private val ePack: EPackage, outputPath: String) {

    private val implPath = File("$outputPath/${ePack.name}/neo4jImpl")
    private val interfacePath = File("$outputPath/${ePack.name}")

    init {
        implPath.mkdirs()
        interfacePath.mkdirs()
    }

    fun generate() = ePack.eClassifiers.forEach {
        when (it) {
            is EClass -> {
                val interfaceWriter = File(interfacePath, "${it.name}.kt").bufferedWriter()
                val implWriter = File(implPath, "${it.name}Neo4jImpl.kt").bufferedWriter()

                interfaceWriter.write(genClassInterface(it))
                implWriter.write(genClassImpl(it))

                it.eAttributes.forEach { eFeature ->
                    val type = Util.type[eFeature.eType.name]

                    if (type != null) {
                        val fGen = FeatureGenerator.getAttrInstance(eFeature.upperBound)
                        interfaceWriter.write(fGen.genInterface(eFeature, type))
                        implWriter.write(fGen.genImpl(eFeature, type))
                    } else {    //write as reference
                        val fGen = FeatureGenerator.getRefInstance(eFeature.upperBound)
                        interfaceWriter.write(fGen.genInterface(eFeature, eFeature.eType.name))
                        implWriter.write(fGen.genImpl(eFeature, eFeature.eType.name))
                    }
                }

                it.eReferences.forEach { eFeature ->
                    val fGen = FeatureGenerator.getRefInstance(eFeature.upperBound)
                    interfaceWriter.write(fGen.genInterface(eFeature, eFeature.eType.name))
                    implWriter.write(fGen.genImpl(eFeature, eFeature.eType.name))
                }

                implWriter.write("}")
                interfaceWriter.write("}")
                interfaceWriter.close()
                implWriter.close()
            }
            is EEnum -> {
                File(interfacePath, "${it.name}.kt").writeText(
                    "enum class ${it.name} { ${it.eLiterals.joinToString {lit -> lit.name}} }")
            }
            is EDataType -> File(interfacePath, "${it.name}.kt").writeText("class ${it.name}")
        }
    }

    fun genInterfaceOnly() {}

    private fun genClassImpl(eClass: EClass) : String {
        val superTypes = eClass.eSuperTypes.joinToString("", prefix = eClass.name) {
            ", ${it.name} by ${it.name}Neo4jImpl()"
        }

        return """
        package ${eClass.ePackage.name}.neo4jImpl
        
        import ${eClass.ePackage.name}.*
        import org.neo4j.ogm.annotation.GeneratedValue
        import org.neo4j.ogm.annotation.Id
        import org.neo4j.ogm.annotation.NodeEntity
        import org.neo4j.ogm.annotation.Relationship
        
        @NodeEntity
        class ${eClass.name}Neo4jImpl : $superTypes {
        
            @Id @GeneratedValue
            val id: Long? = null

        """.trimIndent()    //close bracket '}' missing. Close manually
    }

    private fun genClassInterface(eClass: EClass) : String {
        val superTypes = if(eClass.eSuperTypes.size > 0)
            eClass.eSuperTypes.joinToString(prefix = ":") { it.name } else ""

        return """
            package ${eClass.ePackage.name}
        
            interface ${eClass.name} $superTypes {
            
        """.trimIndent()
    }
}