package geodes.sms.nmf.codegen.template.nmf

import geodes.sms.nmf.codegen.core.Context
import java.io.File

class ModelManagerWriter(context: Context) {
    private val writer = File(context.implDir, "ModelManagerImpl.kt").bufferedWriter()
    init {
        writer.write("""
            package ${context.basePackagePath}.neo4jImpl
                
            import geodes.sms.neo4j.io.GraphManager
            import geodes.sms.neo4j.io.entity.INodeEntity
            import ${context.basePackagePath}.*
                
            class ModelManagerImpl(dbUri: String, username: String, password: String): AutoCloseable {
                private val manager = GraphManager(dbUri, username, password)
                
                fun saveChanges() {
                    manager.saveChanges()
                }
            
                fun clearCache() {
                    manager.clearCache()
                }
            
                fun clearDB() {
                    manager.clearDB()
                }
            
                override fun close() {
                    manager.close()
                }
                
        """.trimIndent() + System.lineSeparator())
    }

    fun genClass(className: String) {
        writer.write(""" 
            fun create$className(): $className {
                return ${className}Neo4jImpl(manager.createNode("$className"))
            }
            
            fun load${className}ById(id: Long): $className {
                return ${className}Neo4jImpl(manager.loadNode(id, "$className"))
            }
            
            fun unload(node: $className) {
                manager.unload(node)
            }

        """.replaceIndent("\t") + System.lineSeparator())
    }

    fun close() {
        writer.write("}")
        writer.close()
    }
}