package geodes.sms.nmf.editor.attributes.neo4jImpl

import geodes.sms.neo4j.io.controllers.IGraphManager
import geodes.sms.nmf.editor.attributes.*
    
class ModelManagerImpl(dbUri: String, username: String, password: String): AutoCloseable {
    private val manager = IGraphManager.getDefaultManager(dbUri, username, password)
    
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
    
	fun createRoot(): Root {
	    return RootNeo4jImpl(manager.createNode("Root"))
	}
	
	fun loadRootById(id: Long): Root {
	    return RootNeo4jImpl(manager.loadNode(id, "Root"))
	}
	
	fun loadRootList(limit: Int = 100): List<Root> {
	    return manager.loadNodes("Root", limit) { RootNeo4jImpl(it) }
	}
	
	fun unload(node: Root) {
	    manager.unload(node)
	}
	
	fun remove(node: Root) {
	    manager.remove(node)
	}
	
}