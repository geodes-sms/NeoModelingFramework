package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.entity.INodeEntity
import org.neo4j.driver.Value
import org.neo4j.driver.types.Node

//Query parameters
class NodeParameter(val alias: Long, val label: String, val props: Map<String, Value>)

class ReferenceParameter(
    val alias: Long,
    val type: String,
    val startNode: INodeEntity,
    val endNode: INodeEntity,
    val props: Map<String, Value>
)

//Query result
data class ContainmentResult(
    val node: Node,
    val containmentRefID: Long,
    val refBound: Map<String, Pair<Int, Int>> = emptyMap()
)

data class NodeResult(
    val node: Node,
    val refBound: Map<String, Pair<Int, Int>> = emptyMap()
)


/*
class Containment(
    val startNode: NodeEntity,
    val rType: String,
    val rProps: Map<String, Value>, //--> containment: true
    val nAlias: Int,    //endNode alias
    val nLabel: String,
    val nProps: Map<String, Value>
)*/