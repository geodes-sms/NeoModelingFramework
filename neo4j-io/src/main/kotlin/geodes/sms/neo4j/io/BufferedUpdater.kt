package geodes.sms.neo4j.io

import geodes.sms.neo4j.Values
import org.neo4j.driver.Driver
import org.neo4j.driver.Query
import org.neo4j.driver.Session
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.IntegerValue
import org.neo4j.driver.internal.value.ListValue
import org.neo4j.driver.internal.value.MapValue
import org.neo4j.driver.internal.value.StringValue

/** Update properties of db entities (nodes and refs) */
class BufferedUpdater(private val driver: Driver, val updateBatchSize: Int = 20000) {

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

    fun updateNodePropertyImmediately(nodeID: Long, propName: String, propVal: Any?) {
        val params = MapValue(mapOf(
            "id" to IntegerValue(nodeID),
            "props" to MapValue(mapOf(
                "propName" to StringValue(propName),
                "propValue" to Values.value(propVal)
            ))
        ))
        val session = driver.session()
        session.writeTransaction { it.run("MATCH (n) WHERE ID(n)=\$id SET n+=\$props", params) }
        session.close()
    }

    fun commitNodesUpdate(session: Session) {
        val paramsIterator = nodesToUpdate.asSequence().map { (id, props) ->
            MapValue(mapOf("id" to IntegerValue(id), "props" to MapValue(props)))
        }.iterator()

        fun commit(batchSize: Int) {
            session.writeTransaction { tx ->
                tx.run(Query("UNWIND \$batch AS row" +
                        " MATCH (node) WHERE ID(node)=row.id" +
                        " SET node += row.props",
                     MapValue(mapOf("batch" to ListValue(*Array(batchSize) { paramsIterator.next() })))
                ))
            }
        }

        for (i in 1..(nodesToUpdate.size / updateBatchSize)) {
            commit(updateBatchSize)
        }
        val rem = nodesToUpdate.size % updateBatchSize
        if (rem > 0)
            commit(rem)
        nodesToUpdate.clear()
    }

    fun commitRelationshipsUpdates(session: Session) {
        val paramsIterator = refsToUpdate.asSequence().map { (id, props) ->
            MapValue(mapOf("id" to IntegerValue(id), "props" to MapValue(props)))
        }.iterator()

        fun commit(batchSize: Int) {
            session.writeTransaction { tx ->
                tx.run(Query("UNWIND \$batch AS row" +
                        " MATCH ()-[r]-() WHERE ID(r)=row.id" +
                        " SET r += row.props",
                    MapValue(mapOf("batch" to ListValue(*Array(batchSize) { paramsIterator.next() })))
                ))
            }
        }

        for (i in 1..(refsToUpdate.size / updateBatchSize)) {
            commit(updateBatchSize)
        }
        val rem = refsToUpdate.size % updateBatchSize
        if (rem > 0) commit(rem)
        refsToUpdate.clear()
    }
}