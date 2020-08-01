package geodes.sms.domain.graph.neo4jImpl

import geodes.sms.domain.graph.CompositeVertex
import geodes.sms.domain.graph.Graph
import geodes.sms.domain.graph.Vertex
import geodes.sms.neo4j.io.GraphManager


class ModelManagerImpl(dbUri: String, username: String, password: String): AutoCloseable {
    private val manager = GraphManager(dbUri, username, password)

    fun createGraph(): Graph {
        return GraphNeo4jImpl(manager.createNode("Graph"))
    }

    fun loadGraphByID(id: Long): Graph {
        return GraphNeo4jImpl(manager.loadNode(id, "Graph"))
    }

    fun unloadGraph(g: Graph) {
        manager.unload(g)
    }

    fun createVertex(): Vertex {
        return VertexNeo4jImpl(manager.createNode("Vertex"))
    }

    fun loadVertexByID(id: Long): Vertex {
        return VertexNeo4jImpl(manager.loadNode(id, "Vertex"))
    }

    fun unloadVertex(v: Vertex) {
        manager.unload(v)
    }

    fun createCompositeVertex(): CompositeVertex {
        return CompositeVertexNeo4jImpl(manager.createNode("CompositeVertex"))
    }

    fun loadCompositeVertexByID(id: Long): CompositeVertex {
        return CompositeVertexNeo4jImpl(manager.loadNode(id, "CompositeVertex"))
    }

    fun unloadCompositeVertex(v: CompositeVertex) {
        manager.unload(v)
    }

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
}