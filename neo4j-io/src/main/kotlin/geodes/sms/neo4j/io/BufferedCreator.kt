package geodes.sms.neo4j.io

import ManagerBatchSizes
import geodes.sms.neo4j.io.entity.INodeEntity
import org.neo4j.driver.Query
import org.neo4j.driver.Session
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.IntegerValue
import org.neo4j.driver.internal.value.ListValue
import org.neo4j.driver.internal.value.MapValue
import org.neo4j.driver.internal.value.StringValue

class BufferedCreator(val nodesBatchSize: Int = ManagerBatchSizes.creatorNodesBatchSize, val refsBatchSize: Int = 10000) {
    private val nodesToCreate = hashMapOf<Long, NodeParameter>()
    private val refsToCreate = hashMapOf<Long, ReferenceParameter>()
    private var n: Long = 0
    private var r: Long = 0

    fun createNode(label: String, props: Map<String, Value> = emptyMap()): Long {
        val innerID = n--
        nodesToCreate[innerID] = NodeParameter(innerID, label, props)
        return innerID
    }

    fun createRelationship(
        rType: String, start: INodeEntity, end: INodeEntity,
        props: Map<String, Value> = emptyMap()
    ): Long {
        val alias = r--
        refsToCreate[alias] = ReferenceParameter(alias, rType, start, end, props)
        return alias
    }

    fun popNodeCreate(alias: Long) { nodesToCreate.remove(alias) }
    fun popRelationshipCreate(alias: Long) { refsToCreate.remove(alias) }

    fun commitNodes(session: Session, mapFunction: (Sequence<Pair<Long, Long>>) -> Unit) {
        val paramsIterator = nodesToCreate.asSequence().map { (_, v) -> MapValue(mapOf(
            "label" to StringValue(v.label),
            "alias" to IntegerValue(v.alias),
            "props" to MapValue(v.props)    //Values.value(v.props)
        )) }.iterator()

        fun commit(dataSize: Int) {
            session.writeTransaction { tx ->
                val res = tx.run(Query("UNWIND \$batch AS row" +
                        " CALL apoc.create.node([row.label], row.props) YIELD node" +
                        " RETURN row.alias AS alias, ID(node) AS id",
                    MapValue(mapOf("batch" to ListValue(*Array(dataSize) { paramsIterator.next() })))
                ))
                mapFunction(Sequence { res }.map { it["alias"].asLong() to it["id"].asLong() })
            }
        }

        val rem = nodesToCreate.size % nodesBatchSize
        for (i in 1..(nodesToCreate.size / nodesBatchSize)) {
            commit(nodesBatchSize)
        }

        //process remaining elements
        if (rem > 0) commit(rem)
        nodesToCreate.clear()
        n = 0
    }

    fun commitRelationships(session: Session, mapFunction: (Sequence<Pair<Long, Long>>) -> Unit) {
        val paramsIterator = refsToCreate.asSequence().map { (_, v) ->
            MapValue(mapOf(
                "alias" to IntegerValue(v.alias),
                "from" to IntegerValue(v.startNode._id),
                "type" to StringValue(v.type),
                "props" to MapValue(v.props),
                "to" to IntegerValue(v.endNode._id)))
        }.iterator()

        fun commit(batchSize: Int) {
            session.writeTransaction { tx ->
                val res = tx.run(Query("UNWIND \$batch AS row" +
                        " MATCH (from) WHERE ID(from) = row.from" +
                        " MATCH (to) WHERE ID(to) = row.to" +
                        " CALL apoc.create.relationship(from, row.type, row.props, to) YIELD rel" +
                        " RETURN row.alias AS alias, ID(rel) AS id",
                    MapValue(mapOf("batch" to ListValue(*Array(batchSize) { paramsIterator.next() })))
                ))
                mapFunction(Sequence { res }.map { it["alias"].asLong() to it["id"].asLong() })
            }
        }

        val rem = refsToCreate.size % refsBatchSize
        for (i in 1..(nodesToCreate.size / refsBatchSize)) {
            commit(refsBatchSize)
        }
        if (rem > 0) commit(rem)
        refsToCreate.clear()
        r = 0
    }

    fun commitRelationshipsNoIDs(session: Session) {
        val paramsIterator = refsToCreate.asSequence().map { (_, v) -> MapValue(mapOf(
            //"alias" to IntegerValue(v.alias),
            "from" to IntegerValue(v.startNode._id),
            "type" to StringValue(v.type),
            "props" to MapValue(v.props),
            "to" to IntegerValue(v.endNode._id)))
        }.iterator()

        fun commit(batchSize: Int) {
            session.writeTransaction { tx ->
                tx.run(Query("UNWIND \$batch AS row" +
                        " MATCH (from) WHERE ID(from) = row.from" +
                        " MATCH (to) WHERE ID(to) = row.to" +
                        " CALL apoc.create.relationship(from, row.type, row.props, to) YIELD rel" +
                        " RETURN row.alias AS alias, ID(rel) AS id",
                    MapValue(mapOf("batch" to ListValue(*Array(batchSize) { paramsIterator.next() })))
                ))
//                tx.run(Query("CALL apoc.periodic.iterate(\"UNWIND \$batch AS row" +
//                        " MATCH (from) WHERE ID(from) = row.from" +
//                        " MATCH (to) WHERE ID(to) = row.to" +
//                        " RETURN from, to, row.type AS rType, row.props AS props\"," +
//                        "\"CALL apoc.create.relationship(from, rType, props, to) YIELD rel " +
//                        " RETURN count(*)\"," +
//                        " {batchSize:10000, parallel:false, params:{batch:\$batch}} ) YIELD batches, total" +
//                        " RETURN batches, total",
//                    MapValue(mapOf("batch" to ListValue(*Array(batchSize) { paramsIterator.next() })))
//                ))
            }
        }

        val rem = refsToCreate.size % refsBatchSize
        for (i in 1..(refsToCreate.size / refsBatchSize)) {
            commit(refsBatchSize)
        }
        if (rem > 0) commit(rem)
        refsToCreate.clear()
        r = 0
    }
}