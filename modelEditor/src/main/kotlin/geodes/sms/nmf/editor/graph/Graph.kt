package geodes.sms.nmf.editor.graph

import geodes.sms.neo4j.io.entity.INodeEntity

interface Graph : INodeEntity {
	fun setName(v: String?)
	fun getName(): String?
	fun addCompositeVertexVertices(): CompositeVertex
	fun loadVertices(limit: Int = 100): List<Vertex>
	fun addVertices(): Vertex
	fun removeVertices(v: Vertex)
}