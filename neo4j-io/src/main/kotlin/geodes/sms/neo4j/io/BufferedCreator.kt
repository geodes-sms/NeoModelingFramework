package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.controllers.INodeEntity
import org.neo4j.driver.Query
import org.neo4j.driver.Session
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.IntegerValue
import org.neo4j.driver.internal.value.ListValue
import org.neo4j.driver.internal.value.MapValue
import org.neo4j.driver.internal.value.StringValue


class BufferedCreator {
    private val nodesToCreate = hashMapOf<Long, NodeParameter>()    // inner id (cacheID) --> Entity
    private val refsToCreate = hashMapOf<Long, ReferenceParameter>()
    private var n: Long = 0   // innerID creator

    fun createNode(label: String, props: Map<String, Value> = emptyMap()): Long {
        val alias = n--
        nodesToCreate[alias] = NodeParameter(alias, label, props)
        return alias
    }

    fun createRelationship(rType: String, startNode: INodeEntity, endNode: INodeEntity,
                           props: Map<String, Value> = emptyMap()
    ): Long {
        val alias = n--
        refsToCreate[alias] = ReferenceParameter(rType, startNode, endNode, props)
        return alias
    }

    //fun createUncontrollableNode(label: String, props: Map<String, Value> = emptyMap()) {}
    //fun createUncontrollableReference() {}

    //fun flush() {}

    fun writeNodes(session: Session, mapFunction: (Sequence<Pair<Long, Long>>) -> Unit) {
        val batchData = nodesToCreate.values.map { MapValue(mapOf(
            "label" to StringValue(it.label),
            "alias" to IntegerValue(it.alias),
            "props" to MapValue(it.props)
        )) }

        session.writeTransaction { tx ->
            val res = tx.run(Query("UNWIND \$batch AS row" +
                    " CALL apoc.create.node([row.label], row.props) YIELD node" +
                    " RETURN row.alias AS alias, ID(node) AS id",
                MapValue(mapOf("batch" to ListValue(*Array(batchData.size) { i -> batchData[i] })))
            ))

            mapFunction(Sequence { res }.map {
                it["alias"].asLong() to it["id"].asLong()
            })
        }
        nodesToCreate.clear()
    }

    fun writeRelationships(session: Session) {

    }

    /* not work for ()--> ()--> () ...
    fun addContainment(): Int { }*/

    fun popNode(innerID: Long) {}
    fun popRef(innerID: Long) {}
    //fun removeCtm(innerID: Int) {}
}