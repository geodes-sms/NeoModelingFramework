package geodes.sms.nmf.neo4j.io

import geodes.sms.nmf.neo4j.DBCredentials
import org.neo4j.driver.internal.value.IntegerValue
import org.neo4j.driver.internal.value.MapValue
import org.neo4j.driver.*


class Neo4jGraph private constructor(private val driver: Driver) : IGraph, NodeStateListener {

    /** It is recommended to create no more then 75 entities (nodes or refs)
     *  at a time before calling graph.save() for performance reasons */
    private val bufferCapacity = 75

    /** Node aliases that appear in query within CREATE(alias) or MATCH(alias) clauses */
    //private val initedNodes = hashSetOf<INode>()

    /** Set IDs for those nodes after Graph.save() command */
    private val nodesToCreate = hashMapOf<String, GraphStateListener>()

    /** Update properties */
    private val nodesToUpdate = mutableListOf<GraphStateListener>()

    /*
     * Initial capacity calculated for creating 200 nodes or 200 refs
     * create node query line length ~32; create ref query line length ~64
     */
    private val qCreate = StringBuilder(bufferCapacity * 68)
    private val qMatch = StringBuilder(bufferCapacity * 36)
    private val qSet = StringBuilder()
    private val qReturn = StringBuilder(bufferCapacity * 14)
    private val properties = mutableMapOf<String, Value>()

    private val TYPE_DEFAULT = "TYPE_DEFAULT"

    companion object {
        fun create(cr: DBCredentials): IGraph =
            Neo4jGraph(GraphDatabase.driver(cr.dbUri, AuthTokens.basic(cr.username, cr.password)))
    }

    override fun createNode(label: String, props: Map<String, Value>): INode {
        val node = Node(this, props)
        val alias = node.alias

        qCreate.append("CREATE ($alias")
            .append(if (label.isNotEmpty()) ":$label)" else ")")
            .appendln()
        qReturn.append("$alias:ID($alias),")
        nodesToCreate[alias] = node
        return node
    }

    override fun matchNode(id: Long): INode {
        return Node(this, id)
    }

    override fun createRelation(refType: String, start: INode, end: INode) {
        (start as Node).nodeState.register()
        (end as Node).nodeState.register()
        val validType = if (refType.isEmpty()) TYPE_DEFAULT else refType
        qCreate.appendln("CREATE (${start.alias})-[:$validType]->(${end.alias})")
    }

    /**
     * Create new relation with new endNode ( -->(newNode) ). StartNode must already exist
     * @return newNode with specified label
     */
    override fun createPath(start: INode, refType: String, endLabel: String, props: Map<String, Value>): INode {
        (start as Node).nodeState.register()
        val validType = if (refType.isEmpty()) TYPE_DEFAULT else refType
        val end = Node(this, props)
        val prAlias = "pr_${end.alias}"
        properties[prAlias] = MapValue(end.props)

        qCreate.append("CREATE (${start.alias})-[:$validType]->(${end.alias}")
            .append(if (endLabel.isNotEmpty()) ":$endLabel)" else ")")
            .appendln()
        qReturn.append("${end.alias}:ID(${end.alias}),")

        nodesToCreate[end.alias] = end
        return end
    }

    //// Inherited from NodeStateListener
    override fun onMatch(node: Node) {
        nodesToUpdate.add(node)
        val alias = node.alias
        val idAlias = "id_$alias"
        qMatch.appendln("MATCH ($alias) WHERE ID($alias)=$$idAlias")
        properties[idAlias] = IntegerValue(node.id)
    }

    override fun onUpdate(node: Node, props: Map<String, Value>) {
        val prAlias = "pr_${node.alias}"
        properties[prAlias] = MapValue(props)
        qSet.appendln("SET ${node.alias}+=$$prAlias")
    }
    ////

    private fun qReturn() : String {
        if (qReturn.isNotEmpty())
            qReturn.setCharAt(qReturn.length - 1, ' ')
        return "RETURN {$qReturn} AS nodeIDs"
    }


    override fun save() {
        if (qCreate.isEmpty() && qSet.isEmpty()) return

//        println("\nonSave")
//        println("$qMatch  MATCH capacity: ${qMatch.capacity()}  length: ${qMatch.length}\n")
//        println("$qCreate  CREATE capacity: ${qCreate.capacity()}  length: ${qCreate.length}\n")
//        println("$qSet  SET capacity: ${qSet.capacity()}  length: ${qSet.length}\n")
//        println("${qReturn()}  RETURN capacity: ${qReturn.capacity()}  length: ${qReturn.length}/n")
//        println()

        val session = driver.session()
        //try {
        val map = session.writeTransaction { tx->
            val res = tx.run(Statement(qMatch.toString() + qCreate.toString() + qSet.toString()
                    + qReturn(), MapValue(properties))
            )
            res.single().get("nodeIDs").asMap(Values.ofLong())
        }

        map.forEach { (alias, id) -> nodesToCreate[alias]!!.onSave(id) }
        nodesToUpdate.forEach { it.onSave() }

        nodesToCreate.clear()
        nodesToUpdate.clear()
        qMatch.clear()  // clear() method preserves initial capacity of StringBuilder
        qCreate.clear()
        qSet.clear()
        qReturn.clear()
        properties.clear()

        //finally {
        session.close()
    }

    override fun clearDB() {
        driver.session().use { it.run(Statement("MATCH (n) DETACH DELETE n")) }
    }

    override fun close() {
        save()
        driver.close()
    }
}


/*
"CREATE (n001:Label{pr_n001})"      //length = 28  ~32

"CREATE (n001)-[:name{containment:true}]->(n002)" //length = 47   ~64

"MATCH (n001) WHERE ID(n001)=$id_n001"   //length = 36

"n001:ID(n001),"  //length = 14
 */