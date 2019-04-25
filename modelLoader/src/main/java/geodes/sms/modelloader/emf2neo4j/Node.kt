package geodes.sms.modelloader.emf2neo4j

interface Node {
    val alias: String
    var usageCount: Int   //inputRefs + outputRefs

    fun createOutputRef(endNode: Node, refName: String, isContainment: Boolean) : List<Query>
}

class BufferedNode(override val alias: String, override var usageCount: Int) : Node {

    override fun createOutputRef(endNode: Node, refName: String, isContainment: Boolean): List<Query> {
        val queryCreate = QueryCreateRef(parentAlias = this.alias,
            refName = refName,
            isContainment = isContainment,
            childAlias = endNode.alias)

        return when (endNode) {
            is BufferedNode -> listOf(queryCreate)
            is Neo4jNode -> listOf(QueryMatch(endNode.alias, endNode.id), queryCreate)
            else -> emptyList()
        }
    }

    /* Create single node */
    fun create(label: String, propsAlias: String) : Query {
        return QueryCreateSingleNode(alias, label, propsAlias)
    }

}


class Neo4jNode(override val alias: String, val id: Long, override var usageCount: Int) : Node {

    override fun createOutputRef(endNode: Node, refName: String, isContainment: Boolean): List<Query> {
        val matchThis = QueryMatch(this.alias, this.id)
        val createRef = QueryCreateRef(parentAlias = this.alias,
            refName = refName,
            isContainment = isContainment,
            childAlias = endNode.alias)

        return when (endNode) {
            is BufferedNode -> listOf(matchThis, createRef)
            is Neo4jNode -> listOf(QueryMatch(endNode.alias, endNode.id), matchThis, createRef)
            else -> emptyList()
        }
    }
}
