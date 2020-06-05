package geodes.sms.neo4j.io

import org.neo4j.driver.Query
import org.neo4j.driver.Session
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.IntegerValue
import org.neo4j.driver.internal.value.ListValue
import org.neo4j.driver.internal.value.MapValue

/** Update properties of db entities (nodes and refs) */
class BufferedUpdater {

    private val nodesToUpdate = hashMapOf<Long, Map<String, Value>>()
    private val refsToUpdate = hashMapOf<Long, Map<String, Value>>()

    fun updateNode(id: Long, props: Map<String, Value>) {
        nodesToUpdate[id] = props
    }

    fun updateRelationship(id: Long, props: Map<String, Value>) {
        refsToUpdate[id] = props
    }

    fun popNodeUpdate(id: Long) {
        nodesToUpdate.remove(id)
    }

    fun popRelationshipUpdate(id: Long) {
        refsToUpdate.remove(id)
    }

    fun commitNodesUpdate(session: Session) {
        val batchData = nodesToUpdate.map { MapValue(mapOf(
            "id" to IntegerValue(it.key),
            "props" to MapValue(it.value)
        )) }

        session.writeTransaction { tx ->
            tx.run(Query("UNWIND \$batch AS row" +
                    " MATCH (node) WHERE ID(node)=row.id" +
                    " SET node += row.props",
                    // + " RETURN ids ??",
                MapValue(mapOf("batch" to ListValue(*Array(batchData.size) { i -> batchData[i] })))
            ))
        }
        nodesToUpdate.clear()
    }

    fun commitRelationshipsUpdates(session: Session) {
        val batchData = refsToUpdate.map { MapValue(mapOf(
            "id" to IntegerValue(it.key),
            "props" to MapValue(it.value)
        )) }

        session.writeTransaction { tx ->
            tx.run(Query("UNWIND \$batch AS row" +
                    " MATCH ()-[r]-() WHERE ID(r)=row.id" +
                    " SET r += row.props",
                MapValue(mapOf("batch" to ListValue(*Array(batchData.size) { i -> batchData[i] })))
            ))
        }
        refsToUpdate.clear()
    }
}