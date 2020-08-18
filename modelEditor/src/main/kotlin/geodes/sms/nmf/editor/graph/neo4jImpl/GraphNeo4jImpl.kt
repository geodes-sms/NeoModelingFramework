package geodes.sms.nmf.editor.graph.neo4jImpl

import geodes.sms.nmf.editor.graph.CompositeVertex
import geodes.sms.nmf.editor.graph.Graph
import geodes.sms.nmf.editor.graph.Vertex
import geodes.sms.neo4j.io.controllers.INodeController


class GraphNeo4jImpl(nc: INodeController): Graph, INodeController by nc {
    override fun setName(v: String?) {
        putProperty("name", v)
    }

    override fun getName(): String? {
        return getPropertyAsString("name")
    }

    override fun addVertex(): Vertex {
        return VertexNeo4jImpl(createChild("vertices", "Vertex"))
    }

    override fun removeVertex(v: Vertex) {
        removeChild("vertices", v)
    }

    override fun getVertices(): List<Vertex> {
        return loadChildren("vertices", "Vertex").map { VertexNeo4jImpl(it) }
    }

    override fun addCompositeVertex(): CompositeVertex {
        return CompositeVertexNeo4jImpl(createChild("vertices", "CompositeVertex"))
    }

    override fun getCompositeVertices(): List<CompositeVertex> {
        return loadChildren("vertices", "CompositeVertex").map { CompositeVertexNeo4jImpl(it) }
    }
}