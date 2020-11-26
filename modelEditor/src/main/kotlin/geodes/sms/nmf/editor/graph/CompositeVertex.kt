package geodes.sms.nmf.editor.graph

import geodes.sms.neo4j.io.entity.INodeEntity

interface CompositeVertex : Vertex {
	fun setCapacity(v: Int?)
	fun getCapacity(): Int?
	fun setDefault_vertex(v: Vertex)
	fun unsetDefault_vertex(v: Vertex)
	fun loadDefault_vertex(): Vertex?
	fun removeSub_vertices(v: Vertex)
	fun loadSub_vertices(limit: Int = 100): List<Vertex>
	fun addSub_vertices(type: VertexType): Vertex
}