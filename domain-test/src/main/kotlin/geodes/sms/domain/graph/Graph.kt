package geodes.sms.domain.graph

import geodes.sms.neo4j.io.entity.INodeEntity

interface Graph: INodeEntity {
    fun setName(v: String?)
    fun getName(): String?

    fun addVertex(): Vertex
    fun removeVertex(v: Vertex)
    fun getVertices(): List<Vertex>

    fun addCompositeVertex(): CompositeVertex
    //fun removeCompositeVertex(v: CompositeVertex)
    fun getCompositeVertices(): List<CompositeVertex>
}