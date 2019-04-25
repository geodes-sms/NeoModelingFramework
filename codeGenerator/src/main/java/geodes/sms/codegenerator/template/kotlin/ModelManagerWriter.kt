/*package geodes.sms.codegenerator.template.kotlin

import java.io.File

class ModelManagerWriter(interfacePath: File, implPath : File, packageName: String) {

    val interfaceWriter = File(interfacePath, "ModelManager.kt").bufferedWriter()
    val implWriter = File(implPath, "ModelManagerNeo4jImpl.kt").bufferedWriter()
    
    init {
        interfaceWriter.write("""
            package geodes.sms.modeleditor.$packageName

            interface ModelManager {
                fun close()
        """.trimIndent())

    }
}*/