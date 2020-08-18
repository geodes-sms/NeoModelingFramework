package geodes.sms.nmf.editor.graph

import geodes.sms.neo4j.io.entity.INodeEntity

interface Vertex: INodeEntity {
    fun setName(v: String?)
    fun getName(): String?

    fun setId(v: Int)   //id cannot be null
    fun getId(): Int?

    fun setIsInitial(v: Boolean?)
    fun getIsInitial(): Boolean?

    fun addEdge(v: Vertex)
    fun removeEdge(v: Vertex)
    fun loadEdges(limit: Int = 100): List<Vertex>
}