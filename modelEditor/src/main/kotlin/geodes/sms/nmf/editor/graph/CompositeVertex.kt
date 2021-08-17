package geodes.sms.nmf.editor.graph

import geodes.sms.neo4j.io.entity.INodeEntity

interface CompositeVertex : Vertex {
	fun setCapacity(v: Int?)
	fun getCapacity(): Int?
	fun setDefault_vertex(v: Vertex)
	fun unsetDefault_vertex(v: Vertex)
	fun getDefault_vertex(): Vertex?
	fun unsetSub_vertices(v: Vertex)
	fun getSub_vertices(limit: Int = 100): List<Vertex>
	fun addSub_vertices(type: VertexType): Vertex
}