package geodes.sms.nmf.codegen

import geodes.sms.nmf.codegen.template.kotlin.ImplementationWriter
import geodes.sms.nmf.codegen.template.kotlin.InterfaceWriter
import org.eclipse.emf.ecore.*
import java.io.File


class Neo4jOgmKotlinGenerator(private val ePack: EPackage, outputPath: String) {

    private val implDir = File("$outputPath/${ePack.name}/neo4jImpl")
    private val interfaceDir = File("$outputPath/${ePack.name}")

    init {
        implDir.deleteRecursively()
        implDir.mkdirs()
    }

    fun generate() = ePack.eClassifiers.forEach {
        when (it) {
            is EClass -> {
                if (!it.isAbstract && !it.isInterface)  // gen impl only for not abstract EClasses
                    ImplementationWriter(it, implDir.path).doGenerate()
                InterfaceWriter(it, interfaceDir.path).doGenerate()
            }
            is EEnum -> {
                File(interfaceDir, "${it.name}.kt").writeText("package ${it.ePackage.name}\n" +
                        "enum class ${it.name} { ${it.eLiterals.joinToString { lit -> lit.name }} }"
                )
            }
            is EDataType -> File(interfaceDir, "${it.name}.kt").writeText("class ${it.name}")
        }
    }
}