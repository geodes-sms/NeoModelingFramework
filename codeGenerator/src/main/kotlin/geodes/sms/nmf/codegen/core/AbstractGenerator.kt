package geodes.sms.nmf.codegen.core

import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EEnum
import java.io.File

abstract class AbstractGenerator(protected val context: Context) {

    init {
        //clear dirs
        context.interfaceDir.deleteRecursively()
        context.implDir.mkdirs()
    }

    protected abstract fun preProcessing()
    protected abstract fun postProcessing()

    fun generate() {
        preProcessing()
        for (eObj in context.ePackage.eClassifiers) {
            when (eObj) {
                is EClass -> generate(eObj)
                is EEnum -> generate(eObj)
            }
        }
        postProcessing()
    }

    protected abstract fun generate(eClass: EClass)

    protected open fun generate(eObj: EEnum) {
        File(context.interfaceDir, "${eObj.name}.kt").writeText("""
            package ${context.basePackagePath}

            enum class ${eObj.name} { ${eObj.eLiterals.joinToString { lit -> lit.name }} }
        """.trimIndent())
    }
}