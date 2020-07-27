package geodes.sms.domain.graph.neo4jImpl

import geodes.sms.domain.graph.CompositeVertex
import geodes.sms.domain.graph.Vertex
import geodes.sms.neo4j.io.controllers.INodeController


class CompositeVertexNeo4jImpl(nc: INodeController): CompositeVertex,
    VertexNeo4jImpl(nc)
    //INodeEntity by nc
{
    override fun setCapacity(v: Int?) {
        putProperty("capacity", v)
    }

    override fun getCapacity(): Int? {
        return getPropertyAsInt("capacity")
    }

    override fun setDefaultVertex(v: Vertex) {
        createOutRef("default_vertex", v)
    }

    override fun removeDefaultVertex(v: Vertex) {
        removeOutRef("default_vertex", v)
    }

    override fun getDefaultVertex(): Vertex {
        return VertexNeo4jImpl(
            loadChildren("default_vertex", "Vertex", limit = 1).first()
        )
    }

    override fun addSubVertex(): Vertex {
        return VertexNeo4jImpl(createChild("sub_vertices", "Vertex"))
    }

    override fun removeSubVertex(v: Vertex) {
        removeChild("sub_vertices", v)
    }

    override fun getSubVertices(): List<Vertex> {
        return (loadChildren("sub_vertices", "Vertex").map { VertexNeo4jImpl(it) })
    }

    override fun addCompositeVertex(): CompositeVertex {
        return CompositeVertexNeo4jImpl(createChild("sub_vertices", "CompositeVertex"))
    }

    override fun getCompositeVertices(): List<CompositeVertex> {
        return (loadChildren("sub_vertices", "CompositeVertex").map { CompositeVertexNeo4jImpl(it) })
    }
}