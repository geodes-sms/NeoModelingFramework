package geodes.sms.nmf.codegen.core

import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EPackage
import java.io.File

class Context(val ePackage: EPackage, javaPackagePath: String, outputPath: String) {
    val packName = ePackage.name.decapitalize()
    val basePackagePath = "$javaPackagePath.$packName"
    val implDir = File("$outputPath/$packName/neo4jImpl")
    val interfaceDir = File("$outputPath/$packName")
    private val subClassMap = getSubClassMap()

    fun getSubClasses(eClass: EClass) = subClassMap.getOrDefault(eClass, emptyList())

    /** Return list of subclass excluding abstract classes and interfaces for each EClass in the package */
    private fun getSubClassMap(): Map<EClass, List<String>> {
        val map = hashMapOf<EClass, MutableList<String>>()
        ePackage.eClassifiers.asSequence()
            .filterIsInstance<EClass>()
            .filter { !it.isAbstract && !it.isInterface }
            .forEach { eClass ->
                for (superClass in eClass.eAllSuperTypes) {
                    val list = map[superClass]
                    if (list == null) {
                        map[superClass] = mutableListOf(eClass.name.capitalize())
                    } else {
                        list.add(eClass.name.capitalize())
                    }
                }
            }
        return map
    }
}