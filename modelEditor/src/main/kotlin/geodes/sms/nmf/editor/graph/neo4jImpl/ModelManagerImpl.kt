package geodes.sms.nmf.editor.graph.neo4jImpl
    
import geodes.sms.neo4j.io.GraphManager
import geodes.sms.neo4j.io.entity.INodeEntity
import geodes.sms.nmf.editor.graph.*
    
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
    
	fun createGraph(): Graph {
	    return GraphNeo4jImpl(manager.createNode("Graph"))
	}
	
	fun loadGraphById(id: Long): Graph {
	    return GraphNeo4jImpl(manager.loadNode(id, "Graph"))
	}
	
	fun unload(node: Graph) {
	    manager.unload(node)
	}
	
	fun createVertex(): Vertex {
	    return VertexNeo4jImpl(manager.createNode("Vertex"))
	}
	
	fun loadVertexById(id: Long): Vertex {
	    return VertexNeo4jImpl(manager.loadNode(id, "Vertex"))
	}
	
	fun unload(node: Vertex) {
	    manager.unload(node)
	}
	
	fun createCompositeVertex(): CompositeVertex {
	    return CompositeVertexNeo4jImpl(manager.createNode("CompositeVertex"))
	}
	
	fun loadCompositeVertexById(id: Long): CompositeVertex {
	    return CompositeVertexNeo4jImpl(manager.loadNode(id, "CompositeVertex"))
	}
	
	fun unload(node: CompositeVertex) {
	    manager.unload(node)
	}
	
}