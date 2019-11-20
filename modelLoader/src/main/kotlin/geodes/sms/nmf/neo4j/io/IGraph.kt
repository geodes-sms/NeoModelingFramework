package geodes.sms.nmf.neo4j.io

interface IGraph : AutoCloseable {
    fun createNode(label: String) : INode
    fun matchNode(id: Long) : INode
    fun createRelation(refType: String, start: INode, end: INode)
    fun createPath(start: INode, endLabel: String, refType: String): INode

    fun save()
    fun clearDB()
}