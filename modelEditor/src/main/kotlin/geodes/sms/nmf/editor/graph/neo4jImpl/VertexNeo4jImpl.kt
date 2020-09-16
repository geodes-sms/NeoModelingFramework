package geodes.sms.nmf.editor.graph.neo4jImpl

import geodes.sms.nmf.editor.graph.Vertex
import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*

open class VertexNeo4jImpl(nc: INodeController) : Vertex, INodeController by nc {
    override fun setName(v: String?) {
        putProperty("name", v)
    }

    override fun getName(): String? {
        return getProperty("name", AsString)
    }

    override fun setId(v: Int) {
        putUniqueProperty("id", v)
    }

    override fun getId(): Int? {
        return getProperty("id", AsInt)
    }

    override fun setIsInitial(v: Boolean?) {
        putProperty("isInitial", v)
    }

    override fun getIsInitial(): Boolean? {
        return getProperty("isInitial", AsBoolean)
    }

    override fun addEdge(v: Vertex) {
        //createOutRef("edge", v, upperBound = 5)
        createOutRef("edge", v)
    }

    override fun unsetEdge(v: Vertex) {
        //removeOutRef("edge", v, lowerBound = 1)
        removeOutRef("edge", v)
    }

    override fun loadEdges(limit: Int): List<Vertex> {
        return loadChildren("vertices", "Vertex", limit = limit).map { VertexNeo4jImpl(it) }
    }
}