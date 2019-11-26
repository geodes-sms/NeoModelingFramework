package geodes.sms.nmf.codegen

import geodes.sms.nmf.codegen.template.kotlin.ImplementationWriter
import geodes.sms.nmf.codegen.template.kotlin.InterfaceWriter
import org.eclipse.emf.ecore.*
import java.io.File


class Neo4jOgmKotlinGenerator(private val ePack: EPackage, outputPath: String) {

    private val implDir = File("$outputPath/${ePack.name}/neo4jImpl")
    private val interfaceDir = File("$outputPath/${ePack.name}")
    private val subClassMap = getSubClassMap()

    init {
        interfaceDir.deleteRecursively()
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
//            is EDataType -> File(interfaceDir, "${it.name}.kt").writeText(
//                "package ${it.ePackage.name}\n class ${it.name}")
        }
    }

    private fun getSubClassMap(): Map<EClass, List<EClass>> {
        val map = hashMapOf<EClass, MutableList<EClass>>()
        ePack.eClassifiers.asSequence()
            .filterIsInstance<EClass>()
            .filter { !it.isAbstract && !it.isInterface }
            .forEach { eClass ->
                eClass.eAllSuperTypes.forEach { superClass ->
                    val list = map[superClass]
                    if (list == null) {
                        map[superClass] = mutableListOf(eClass)
                    } else {
                        list.add(eClass)
                    }
                }
            }
        return map
    }

    fun genSessionObject() {
        File(interfaceDir, "Session.kt").writeText("""
            package ${ePack.name}

            import org.neo4j.ogm.config.Configuration
            import org.neo4j.ogm.session.Session
            import org.neo4j.ogm.session.SessionFactory

            object Session {
                private val configuration = Configuration.Builder()
                    .uri("bolt://localhost:7687")
                    .credentials("neo4j", "admin")
                    .useNativeTypes()
                    .build()

                val sessionFactory = SessionFactory(configuration, "${ePack.name}")

                val session: Session = sessionFactory.openSession()
            }
        """.trimIndent())
    }

}