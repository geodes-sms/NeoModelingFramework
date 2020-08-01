package geodes.sms.domain.graph.neo4jImpl

import geodes.sms.domain.graph.CompositeVertex
import geodes.sms.domain.graph.Graph
import geodes.sms.domain.graph.Vertex
import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.entity.RefBounds


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