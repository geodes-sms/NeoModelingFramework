package geodes.sms.nmf.neo4j.io

interface IGraph : AutoCloseable {
    fun createNode(label: String) : INode
    fun matchNode(id: Long) : INode
    fun createRelation(refType: String, start: INode, end: INode, containment: Boolean)
    fun createPath(start: INode, endLabel: String, refType: String, containment: Boolean): INode

    fun save()
    fun clearDB()
}