package geodes.sms.nmf.editor.graph.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.graph.*

class CompositeVertexNeo4jImpl(nc: INodeController) : CompositeVertex, Vertex by VertexNeo4jImpl(nc), INodeController by nc {
	override val _id = nc._id
	override val label = nc.label
	override fun setCapacity(v: Int?) {
	    if (v == null) removeProperty("capacity")
	    else putProperty("capacity", v)
	}
	override fun getCapacity(): Int? {
	    return getProperty("capacity", AsInt)
	}
	                override fun loadDefault_vertex(): Vertex? {
	                    val data = loadOutConnectedNodes("default_vertex", null, 1, "") {
	                        when (it.label) {
		"CompositeVertex" -> CompositeVertexNeo4jImpl(it)
		"Vertex" -> VertexNeo4jImpl(it)
		else -> throw Exception("Cannot cast InodeController")
	}
	                    }
	                    return if (data.isEmpty()) null else data[0]
	                }
	override fun setDefault_vertex(v: Vertex) {
	    createOutRef("default_vertex", v, 1)
	}
	override fun unsetDefault_vertex(v: Vertex) {
	    removeOutRef("default_vertex", v)
	}
	override fun addCompositeVertexSub_vertices(): CompositeVertex {
	    return CompositeVertexNeo4jImpl(createChild("sub_vertices", "CompositeVertex"))
	}
	override fun addSub_vertices(): Vertex {
	    return VertexNeo4jImpl(createChild("sub_vertices", "Vertex"))
	}

	                override fun loadSub_vertices(limit: Int): List<Vertex> {
	                    return loadOutConnectedNodes("sub_vertices", null, limit, "") {
	                        when (it.label) {
		"CompositeVertex" -> CompositeVertexNeo4jImpl(it)
		"Vertex" -> VertexNeo4jImpl(it)
		else -> throw Exception("Cannot cast InodeController")
	}
	                    }
	                }
	override fun removeSub_vertices(v: Vertex) {
	    removeChild("sub_vertices", v)
	}
}