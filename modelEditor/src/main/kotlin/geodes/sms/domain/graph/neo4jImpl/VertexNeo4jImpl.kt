package geodes.sms.domain.graph.neo4jImpl

import geodes.sms.domain.graph.Vertex
import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.entity.RefBounds


open class VertexNeo4jImpl(nc: INodeController) : Vertex, INodeController by nc {

//    companion object {
//        val edgeBounds = RefBounds(1,5)
//    }

    override fun setName(v: String?) {
        putProperty("name", v)
    }

    override fun getName(): String? {
        return getPropertyAsString("name")
    }

    override fun setId(v: Int) {
        putUniqueProperty("id", v)
    }

    override fun getId(): Int? {
        return getPropertyAsInt("id")
    }

    override fun setIsInitial(v: Boolean?) {
        putProperty("isInitial", v)
    }

    override fun getIsInitial(): Boolean? {
        return getPropertyAsBoolean("isInitial")
    }

    override fun addEdge(v: Vertex) {
        //createOutRef("edge", v, edgeBounds.upperBound)
        createOutRef("edge", v)
    }

    override fun removeEdge(v: Vertex) {
        //removeOutRef("edge", v, edgeBounds.lowerBound)
        removeOutRef("edge", v)
    }

    override fun loadEdges(limit: Int): List<Vertex> {
        return loadChildren("vertices", "Vertex", limit = limit).map { VertexNeo4jImpl(it) }
    }
}