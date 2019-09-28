package geodes.sms.nmf.neo4j.io

interface IGraph {
    fun createNode(label: String) : INode
    fun createRelation(type: String, start: INode, end: INode, containment: Boolean)
    fun createPath(start: INode, endLabel: String, type: String, containment: Boolean): INode

    fun save()
    fun clearDB()
}