package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.controllers.INodeEntity
import org.neo4j.driver.Value


class NodeParameter(val alias: Long, val label: String, val props: Map<String, Value>)

class ReferenceParameter(
    //val alias: Long
    val type: String,
    val startNode: INodeEntity,
    val endNode: INodeEntity,
    val props: Map<String, Value>
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