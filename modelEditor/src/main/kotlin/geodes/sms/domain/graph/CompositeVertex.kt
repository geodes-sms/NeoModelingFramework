package geodes.sms.domain.graph

interface CompositeVertex: Vertex {
    //var capacity: Int
    fun setCapacity(v: Int?)
    fun getCapacity(): Int?

    fun setDefaultVertex(v: Vertex) //crossRef
    fun removeDefaultVertex(v: Vertex)
    fun getDefaultVertex(): Vertex

    fun addSubVertex(): Vertex
    fun removeSubVertex(v: Vertex)
    fun getSubVertices(): List<Vertex>

    fun addCompositeVertex(): CompositeVertex
    //fun removeCompositeVertex(v: CompositeVertex)
    fun getCompositeVertices(): List<CompositeVertex>
}