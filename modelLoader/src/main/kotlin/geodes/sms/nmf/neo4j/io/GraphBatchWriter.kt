package geodes.sms.nmf.neo4j.io

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Query
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.*
import java.util.*


class GraphBatchWriter(dbUri: String, username: String, password: String) : AutoCloseable {
    private val driver = GraphDatabase.driver(dbUri, AuthTokens.basic(username, password))
    private val session = driver.session()

    private var n: Long = 0   // alias creator
    private val onCreateListeners = hashMapOf<Long, Entity>()

    /** Buffers used as parameters for query */
    private val nodesToCreate = LinkedList<MapValue>()
    private val refsToCreate = LinkedList<ReferenceParameter>()

    fun createNode(label: String, props: Map<String, Value> = emptyMap()): Entity {
        val alias = n--
        nodesToCreate.add(MapValue(mapOf(
            "label" to StringValue(label),
            "alias" to IntegerValue(alias),
            "props" to MapValue(props)
        )))
        val node = Entity()
        onCreateListeners[alias] = node
        return node
    }

    fun createRef(rType: String, start: Entity, end: Entity, isContainment: Boolean) {
        refsToCreate.add(ReferenceParameter(rType, start, end,
            mapOf("containment" to BooleanValue.fromBoolean(isContainment))))
    }

    /** Save nodes from buffer and return count of saved nodes */
    fun saveNodes(): Int {
        session.writeTransaction { tx ->
            val res = tx.run(Query("UNWIND \$batch AS row" +
                    " CALL apoc.create.node([row.label], row.props) YIELD node" +
                    " RETURN row.alias AS alias, ID(node) AS id",
                MapValue(mapOf("batch" to ListValue(*Array(nodesToCreate.size) { i -> nodesToCreate[i] }))))
            )
            for (record in res) {
                onCreateListeners[record["alias"].asLong()]!!.id = record["id"].asLong()
            }
        }
        val count = onCreateListeners.size
        onCreateListeners.clear()
        nodesToCreate.clear()
        return count
    }

    fun saveRefs(): Int {
        val paramsIterator = refsToCreate.asSequence().map { MapValue(mapOf(
            "from" to IntegerValue(it.startNode.id),
            "type" to StringValue(it.type),
            "props" to MapValue(it.props),
            "to" to IntegerValue(it.endNode.id)))
        }.iterator()

        val count = session.writeTransaction { tx ->
            val res = tx.run(Query("UNWIND \$batch AS row" +
                    " MATCH (from) WHERE ID(from) = row.from" +
                    " MATCH (to) WHERE ID(to) = row.to" +
                    " CALL apoc.create.relationship(from, row.type, row.props, to) YIELD rel" +
                    " RETURN COUNT(rel) AS count",     // Query cannot conclude with CALL statement
                MapValue(mapOf("batch" to ListValue(*Array(refsToCreate.size) { paramsIterator.next() })))
            ))
            res.single()["count"].asInt()
        }

        refsToCreate.clear()
        return count
    }

    override fun close() {
        session.close()
        driver.close()
    }
}