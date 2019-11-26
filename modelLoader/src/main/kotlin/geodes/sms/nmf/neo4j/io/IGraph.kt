package geodes.sms.nmf.neo4j.io

import org.neo4j.driver.Value

interface IGraph : AutoCloseable {
    fun createNode(label: String, props: Map<String, Value> = emptyMap()): INode
    fun matchNode(id: Long) : INode
    fun createRelation(refType: String, start: INode, end: INode)
    fun createPath(start: INode, refType: String, endLabel: String, props: Map<String, Value> = emptyMap()): INode

    fun save()
    fun clearDB()
}