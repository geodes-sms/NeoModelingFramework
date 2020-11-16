package geodes.sms.nmf.editor.graph

import geodes.sms.neo4j.io.entity.INodeEntity

interface Vertex : INodeEntity {
	fun setName(v: String?)
	fun getName(): String?
	fun setId(v: Int?)
	fun getId(): Int?
	fun setIs_initial(v: Boolean?)
	fun getIs_initial(): Boolean?
	fun setEdge(v: Vertex)
	fun unsetEdge(v: Vertex)
		fun loadEdge(limit: Int = 100): List<Vertex>
	
}