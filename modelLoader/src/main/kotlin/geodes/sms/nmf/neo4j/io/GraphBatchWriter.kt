package geodes.sms.nmf.neo4j.io

import geodes.sms.nmf.neo4j.DBCredentials
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Query
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.*


class GraphBatchWriter(cr: DBCredentials) : AutoCloseable {
    private val driver = GraphDatabase.driver(cr.dbUri, AuthTokens.basic(cr.username, cr.password))
    private val session = driver.session()

    private var n = 0   // alias creator
    private val onCreateListeners = hashMapOf<String, IDHolder>()

    /** Buffers used as parameters for query */
    private val nodesToCreate = mutableListOf<MapValue>()
    private val refsToCreate = mutableListOf<MapValue>()
    //private val nodesToRemove = mutableListOf<MapValue>()

    fun createNode(label: String, props: Map<String, Value> = emptyMap()) : IDHolder {
        val alias = "n${n++}"
        nodesToCreate.add(MapValue(mapOf(
            "label" to StringValue(label),
            "alias" to StringValue(alias),
            "props" to MapValue(props)
        )))
        val node = IDHolder()
        onCreateListeners[alias] = node
        return node
    }

    fun createRef(startID: Long, endID: Long, type: String, props: Map<String, Value> = emptyMap()) {
        refsToCreate.add(MapValue(mapOf(
            "from" to IntegerValue(startID),
            "type" to StringValue(type),
            "props" to MapValue(props),
            "to" to IntegerValue(endID)
        )))
    }

    /** Save nodes from buffer and return count of saved nodes */
    fun saveNodes() : Int {
        session.writeTransaction { tx ->
            val res = tx.run(Query("UNWIND \$batch AS row" +
                    " CALL apoc.create.node([row.label], row.props) YIELD node" +
                    " RETURN row.alias AS alias, ID(node) AS id",
                MapValue(mapOf("batch" to ListValue(*Array(nodesToCreate.size) { i -> nodesToCreate[i] }))))
            )
            for (record in res) {
                onCreateListeners[record["alias"].asString()]!!.id = record["id"].asLong()
            }
        }
        val count = onCreateListeners.size
        onCreateListeners.clear()
        nodesToCreate.clear()
        return count
    }

    fun saveRefs() : Int {
        val count = session.writeTransaction { tx ->
            val res = tx.run(Query("UNWIND {batch} AS row" +
                    " MATCH (from) WHERE ID(from) = row.from" +
                    " MATCH (to) WHERE ID(to) = row.to" +
                    " CALL apoc.create.relationship(from, row.type, row.props, to) YIELD rel" +
                    " RETURN COUNT(rel) AS count",     // Query cannot conclude with CALL statement
                MapValue(mapOf("batch" to ListValue(*Array(refsToCreate.size) { i -> refsToCreate[i]}))))
            )
            res.single()["count"].asInt()
        }
        refsToCreate.clear()
        return count
    }

    /*
    //update props
    fun updateNodes() {
        //batch = MapValue(mapOf("id" to Long() , "props" to ...))
        val query = "UNWIND {batch} AS row" +
                " MATCH (node) WHERE ID(node) = row.id" +
                " SET node += row.props"
    }

    fun removeNodes() {
        val query = "UNWIND {batch} AS row" +
                " MATCH (node) WHERE ID(node) = row.id" +
                " DETACH DELETE node"
    }

    /** Save all changes in the buffer */
    fun save() {

    }*/

    fun clearDB() {
        session.writeTransaction {
            it.run(Query("CALL apoc.periodic.iterate(\"MATCH (n) return n\"," +
                    " \"DETACH DELETE n\", {batchSize:8000}) YIELD batches, total" +
                    " RETURN batches, total"))
        }
    }

    override fun close() {
        session.close()
        driver.close()
    }
}