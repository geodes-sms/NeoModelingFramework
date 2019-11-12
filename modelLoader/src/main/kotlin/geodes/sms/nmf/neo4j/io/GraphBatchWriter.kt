package geodes.sms.nmf.neo4j.io

import geodes.sms.nmf.neo4j.DBCredentials
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Statement
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.*


class GraphBatchWriter(cr: DBCredentials) : AutoCloseable {
    private val driver = GraphDatabase.driver(cr.dbUri, AuthTokens.basic(cr.username, cr.password))
    private val session = driver.session()

    private val onCreateListeners = hashMapOf<String, IDHolder>()

    /** Buffers used as parameters for query */
    private val nodesToCreate = mutableListOf<MapValue>()
    private val refsToCreate = mutableListOf<MapValue>()
    //private val nodesToRemove = mutableListOf<MapValue>()

    fun createNode(label: String, alias: String, props: Map<String, Value>) : IDHolder {
        nodesToCreate.add(MapValue(mapOf(
            "label" to StringValue(label),
            "alias" to StringValue(alias),
            "props" to MapValue(props)
        )))
        val node = IDHolder()
        onCreateListeners[alias] = node
        return node
    }

    fun createRef(startID: Long, endID: Long, type: String, containment: Boolean) {
        refsToCreate.add(MapValue(mapOf(
            "from" to IntegerValue(startID),
            "type" to StringValue(type),
            "ctm" to BooleanValue.fromBoolean(containment),
            "to" to IntegerValue(endID)
        )))
    }

    fun saveNodes() {
        session.writeTransaction { tx ->
            val res = tx.run(Statement("UNWIND \$batch AS row" +
                    " CALL apoc.create.node([row.label], row.props) YIELD node" +
                    " RETURN row.alias AS alias, id(node) AS id",
                MapValue(mapOf("batch" to ListValue(*Array(nodesToCreate.size) { i -> nodesToCreate[i] }))))
            )
            for (record in res) {
                onCreateListeners[record["alias"].asString()]!!.id = record["id"].asLong()
            }
        }
        onCreateListeners.clear()
        nodesToCreate.clear()
    }

    fun saveRefs() {
        session.writeTransaction { tx ->
            tx.run(Statement("UNWIND {batch} AS row" +
                    " MATCH (from) WHERE ID(from) = row.from" +
                    " MATCH (to) WHERE ID(to) = row.to" +
                    " CALL apoc.create.relationship(from, row.type, {}, to) YIELD rel" +
                    " SET rel.containment = row.ctm",
                MapValue(mapOf("batch" to ListValue(*Array(refsToCreate.size) { i -> refsToCreate[i]}))))
            )
        }
        refsToCreate.clear()
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

    override fun close() {
        session.close()
        driver.close()
    }
}