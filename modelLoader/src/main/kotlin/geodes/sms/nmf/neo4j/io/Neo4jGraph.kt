package geodes.sms.nmf.neo4j.io

import org.neo4j.driver.internal.value.IntegerValue
import org.neo4j.driver.internal.value.MapValue
import org.neo4j.driver.v1.*


class Neo4jGraph(dbUri: String, username: String, pwd: String): IGraph, AutoCloseable {
    private val driver = GraphDatabase.driver(dbUri, AuthTokens.basic(username, pwd))

    /** It is recommended to create no more then 75 entities (nodes or refs)
     *  at a time before calling graph.save() for performance reasons */
    private val buffCapacity = 75

    /** Node aliases that appear in query within CREATE(alias) or MATCH(alias) clauses */
    private val initedNodes = hashSetOf<INode>()

    /** Set IDs for those nodes after Graph.save() command */
    private val onSaveListeners = hashMapOf<String, StateListener>()

    /*
     * Initial capacity calculated for creating 200 nodes or 200 refs
     * create node query line length ~32; create ref query line length ~64
     */
    private val qCreate = StringBuilder(buffCapacity * 68)
    private val qMatch = StringBuilder(buffCapacity * 36)
    private val qReturn = StringBuilder(buffCapacity * 14)
    private val properties = mutableMapOf<String, Value>()  //Str -> MapValue

    override fun createNode(label: String) : INode {
        val localProps = hashMapOf<String, Value>()
        val node = Node(localProps)
        val prAlias = "pr_${node.alias}"
        properties[prAlias] = MapValue(localProps)

        qCreate.append("CREATE (${node.alias}")
        if (label.isNotEmpty()) qCreate.append(":$label")
        qCreate.appendln(" $$prAlias)")
        qReturn.append("${node.alias}:ID(${node.alias}),")

        initedNodes.add(node)
        onSaveListeners[node.alias] = node
        return node
    }

    override fun createRelation(type: String, start: INode, end: INode, containment: Boolean) {
        matchNode(start)
        matchNode(end)
        qCreate.appendln("CREATE (${start.alias})-[:$type{containment:$containment}]->(${end.alias})")
    }

    /**
     * Create relation with endNode ( -->(newNode) ). StartNode must already exist
     * @return newNode
     */
    override fun createPath(start: INode, endLabel: String, type: String, containment: Boolean): INode {
        matchNode(start)

        val localProps = hashMapOf<String, Value>()
        val end = Node(localProps)
        val prAlias = "pr_${end.alias}"
        properties[prAlias] = MapValue(localProps)

        qCreate.append("CREATE (${start.alias})-[:$type{containment:$containment}]->(${end.alias}")
            .append(if (endLabel.isNotEmpty()) ":$endLabel $$prAlias)" else " $$prAlias)" )
        qReturn.append("${end.alias}:ID(${end.alias}),")

        initedNodes.add(end)
        onSaveListeners[end.alias] = end
        return end
    }

    /*
    fun setProperties(node: INode) {
        matchNode(node)
        //=
    }

    fun appendProperties(node: INode) {
        matchNode(node)
        //+=
    }*/

    private fun matchNode(node: INode) {
        if (!initedNodes.contains(node)) {
            val idAlias = "id_${node.alias}"
            qMatch.appendln("MATCH (${node.alias}) WHERE ID(${node.alias})=$$idAlias")
            properties[idAlias] = IntegerValue(node.id)
            initedNodes.add(node)
        }
    }

    private fun getQueryReturn() : String {
        qReturn.setCharAt(qReturn.length - 1, '}')
        return "RETURN {${qReturn.append("AS nodeIDs")}"
    }

    override fun save() {
        if (initedNodes.isEmpty()) return
        val session = driver.session()  //.use { } instead of try catch block
//        try {
            //there are nodes and/or refs to create

//            println("MATCH   capacity: ${qMatch.capacity()}  length: ${qMatch.length}")
//            println("CREATE  capacity: ${qCreate.capacity()}  length: ${qCreate.length}")
//            println("RETURN  capacity: ${qReturn.capacity()}  length: ${qReturn.length}")
//            println()

            if (onSaveListeners.isNotEmpty()) {
                val map = session.writeTransaction {
                    val res = it.run(Statement(qMatch.toString() + qCreate.toString() +
                            getQueryReturn(), MapValue(properties)))
                    res.single().get("nodeIDs").asMap(Values.ofLong())
                }

                map.forEach { (alias, id) -> onSaveListeners[alias]!!.onSave(id) }
                onSaveListeners.clear()
                qReturn.clear()
            } else {    //nothing to return; create ref only
                session.writeTransaction { it.run(Statement(
                    qMatch.toString() + qCreate.toString(), MapValue(properties))) }
            }

            initedNodes.clear()
            qCreate.clear() //clear() preserve initial capacity of StringBuilder
            qMatch.clear()
            properties.clear()
//        } catch (e: Exception) {
//            println("Transaction exception:")
//            println(e.printStackTrace())
//            throw e
//        } finally {
//            session.close()
//        }
            session.close() //to finally block
    }

    override fun clearDB() {
        driver.session().use { it.run(Statement("MATCH (n) DETACH DELETE n")) }
    }

    fun execute(query: String) {
        driver.session().use { it.run(query) }
    }

    override fun close() {
        driver.close()
    }
}


/*
"CREATE (n001:Label{pr_n001})"      //length = 28  ~32

"CREATE (n001)-[:name{containment:true}]->(n002)" //length = 47   ~64

"MATCH (n001) WHERE ID(n001)=$id_n001"   //length = 36

"n001:ID(n001),"  //length = 14
 */