package geodes.sms.nmf.editor.graph.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.graph.*

open class VertexNeo4jImpl(nc: INodeController) : Vertex, INodeController by nc {

	override fun setName(v: String?) {
	    if (v == null) removeProperty("name")
	    else putProperty("name", v)
	}
	override fun getName(): String? {
	    return getProperty("name", AsString)
	}
	override fun setId(v: Int?) {
	    if (v == null) removeProperty("id")
	    else putUniqueProperty("id", v)
	}
	override fun getId(): Int? {
	    return getProperty("id", AsInt)
	}
	override fun setIs_initial(v: Boolean?) {
	    if (v == null) removeProperty("is_initial")
	    else putProperty("is_initial", v)
	}
	override fun getIs_initial(): Boolean? {
	    return getProperty("is_initial", AsBoolean)
	}
	                override fun loadEdge(limit: Int): List<Vertex> {
	                    return loadOutConnectedNodes("edge", null, limit, "") {
	                        when (it.label) {
		"CompositeVertex" -> CompositeVertexNeo4jImpl(it)
		"Vertex" -> VertexNeo4jImpl(it)
		else -> throw Exception("Cannot cast InodeController")
	}
	                    }
	                }
	override fun setEdge(v: Vertex) {
	    createOutRef("edge", v)
	}
	override fun unsetEdge(v: Vertex) {
	    removeOutRef("edge", v, 1)
	}
}