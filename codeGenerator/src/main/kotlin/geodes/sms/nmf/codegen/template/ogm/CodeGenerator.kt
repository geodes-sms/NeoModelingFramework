package geodes.sms.nmf.codegen.template.ogm

import geodes.sms.nmf.codegen.core.AbstractGenerator
import geodes.sms.nmf.codegen.core.Context
import org.eclipse.emf.ecore.*


class CodeGenerator(context: Context) : AbstractGenerator(context) {

    override fun preProcessing() {

    }

    override fun postProcessing() {

    }

    override fun generate(eClass: EClass) {

    }

//    fun generate() {
//        for (eObj in ePack.eClassifiers) {
//            when (eObj) {
//                is EClass -> {
//                    if (!eObj.isAbstract && !eObj.isInterface){}  // gen impl only for not abstract EClasses
//                        //ImplementationWriter(eObj, implDir).doGenerate()
//                    //InterfaceWriter(eObj, interfaceDir).doGenerate()
//                }
//                is EEnum -> {
//                    File(interfaceDir, "${eObj.name}.kt").writeText("""
//                        package $packName
//
//                        enum class ${eObj.name} { ${eObj.eLiterals.joinToString { lit -> lit.name }} }
//                    """)
//                }
//            }
//        }
//    }

    /*
    private fun genSessionObject() {
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
    */
}