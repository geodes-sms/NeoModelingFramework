package geodes.sms.nmf.editor.graph.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.graph.*

class GraphNeo4jImpl(nc: INodeController) : Graph, INodeController by nc {

	override fun setName(v: String?) {
	    if (v == null) removeProperty("name")
	    else putProperty("name", v)
	}
	override fun getName(): String? {
	    return getProperty("name", AsString)
	}
	override fun addCompositeVertexVertices(): CompositeVertex {
	    return CompositeVertexNeo4jImpl(createChild("vertices", "CompositeVertex"))
	}
	override fun addVertices(): Vertex {
	    return VertexNeo4jImpl(createChild("vertices", "Vertex"))
	}

	                override fun loadVertices(limit: Int): List<Vertex> {
	                    return loadOutConnectedNodes("vertices", null, limit, "") {
	                        when (it.label) {
		"CompositeVertex" -> CompositeVertexNeo4jImpl(it)
		"Vertex" -> VertexNeo4jImpl(it)
		else -> throw Exception("Cannot cast InodeController")
	}
	                    }
	                }
	override fun removeVertices(v: Vertex) {
	    removeChild("vertices", v)
	}
}