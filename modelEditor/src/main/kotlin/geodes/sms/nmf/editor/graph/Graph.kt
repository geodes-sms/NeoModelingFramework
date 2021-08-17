package geodes.sms.nmf.editor.graph

import geodes.sms.neo4j.io.entity.INodeEntity

interface Graph : INodeEntity {
	fun setName(v: String?)
	fun getName(): String?
	fun unsetVertices(v: Vertex)
	fun getVertices(limit: Int = 100): List<Vertex>
	fun addVertices(type: VertexType): Vertex
}