package geodes.sms.nmf.editor.graph.neo4jImpl

import geodes.sms.neo4j.io.controllers.IGraphManager
import geodes.sms.nmf.editor.graph.*
    
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
    
	fun createGraph(): Graph {
	    return GraphNeo4jImpl(manager.createNode("Graph"))
	}
	
	fun loadGraphById(id: Long): Graph {
	    return GraphNeo4jImpl(manager.loadNode(id, "Graph"))
	}
	
	fun loadGraphByLabel(limit: Int = 100): List<Graph> {
	    return manager.loadNodes("Graph", limit) { GraphNeo4jImpl(it) }
	}
	
	fun unload(node: Graph) {
	    manager.unload(node)
	}
	
	fun remove(node: Graph) {
	    manager.remove(node)
	}
	
	fun createVertex(): Vertex {
	    return VertexNeo4jImpl(manager.createNode("Vertex"))
	}
	
	fun loadVertexById(id: Long): Vertex {
	    return VertexNeo4jImpl(manager.loadNode(id, "Vertex"))
	}
	
	fun loadVertexByLabel(limit: Int = 100): List<Vertex> {
	    return manager.loadNodes("Vertex", limit) { VertexNeo4jImpl(it) }
	}
	
	fun unload(node: Vertex) {
	    manager.unload(node)
	}
	
	fun remove(node: Vertex) {
	    manager.remove(node)
	}
	
	fun createCompositeVertex(): CompositeVertex {
	    return CompositeVertexNeo4jImpl(manager.createNode("CompositeVertex"))
	}
	
	fun loadCompositeVertexById(id: Long): CompositeVertex {
	    return CompositeVertexNeo4jImpl(manager.loadNode(id, "CompositeVertex"))
	}
	
	fun loadCompositeVertexByLabel(limit: Int = 100): List<CompositeVertex> {
	    return manager.loadNodes("CompositeVertex", limit) { CompositeVertexNeo4jImpl(it) }
	}
	
	fun unload(node: CompositeVertex) {
	    manager.unload(node)
	}
	
	fun remove(node: CompositeVertex) {
	    manager.remove(node)
	}
	
}