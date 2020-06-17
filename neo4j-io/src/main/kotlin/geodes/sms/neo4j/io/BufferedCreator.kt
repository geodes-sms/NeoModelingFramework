package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.entity.INodeEntity
import org.neo4j.driver.Query
import org.neo4j.driver.Session
import org.neo4j.driver.Transaction
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.IntegerValue
import org.neo4j.driver.internal.value.ListValue
import org.neo4j.driver.internal.value.MapValue
import org.neo4j.driver.internal.value.StringValue
//import java.util.HashMap

class BufferedCreator {
    private val nodesToCreate = hashMapOf<Long, NodeParameter>()
    private val refsToCreate = hashMapOf<Long, ReferenceParameter>()
    private var n: Long = 0
    private var r: Long = 0


    fun createNode(label: String, props: Map<String, Value> = emptyMap()): Long {
        val innerID = n--
        nodesToCreate[innerID] = NodeParameter(innerID, label, props)
        return innerID
    }

//    /** -->()   Create new ref + new endNode */
//    fun createPathSegment(
//        start: INodeEntity, rType: String, endLabel: String,
//        nProps: Map<String, Value> = emptyMap(),
//        rProps: Map<String, Value> = emptyMap()
//    ) : Pair<Long, Long> {
//        val endNodeAlias = n--
//        nodesToCreate[endNodeAlias] = NodeParameter(endNodeAlias, endLabel, nProps)
//
//        val rAlias = r--
//        refsToCreate[rAlias] = ReferenceParameter(rAlias, rType,
//            start, endNodeAlias, rProps)
//
//        return rAlias to endNodeAlias
//    }

    fun createRelationship(
        rType: String, start: INodeEntity, end: INodeEntity,
        props: Map<String, Value> = emptyMap()
    ): Long {
        val alias = r--
        refsToCreate[alias] = ReferenceParameter(alias, rType, start, end, props)
        return alias
    }

    /*
    fun createRelationship(
        rType: String, start: Long, end: INodeEntity,
        props: Map<String, Value> = mapOf("containment" to Values.value(false))
    ): Long {
        val alias = n--
        refsToCreate[alias] = ReferenceParameter(alias, rType,
            object : ID { override val _id = start },
            end,
            props
        )
        return alias
    }

    fun createRelationship(
        rType: String, start: INodeEntity, end: Long,
        props: Map<String, Value> =  mapOf("containment" to Values.value(false))
    ): Long {
        val alias = n--
        refsToCreate[alias] = ReferenceParameter(alias, rType, start,
            object : ID { override val _id = end }, props
        )
        return alias
    }

    fun createRelationship(
        rType: String, start: Long, end: Long,
        props: Map<String, Value> = emptyMap()// mapOf("containment" to Values.value(false))
    ): Long {
        val alias = n--
        refsToCreate[alias] = ReferenceParameter(alias, rType,
            object: ID { override val _id = start },
            object: ID { override val _id = end },
            props
        )
        return alias
    }*/

    fun popNodeCreate(alias: Long) { nodesToCreate.remove(alias) }
    fun popRelationshipCreate(alias: Long) { refsToCreate.remove(alias) }

    fun commitNodes(session: Session, mapFunction: (Sequence<Pair<Long, Long>>) -> Unit) {

        val batchIterator = nodesToCreate.asSequence().map { (_, v) -> MapValue(mapOf(
            "label" to StringValue(v.label),
            "alias" to IntegerValue(v.alias),
            "props" to MapValue(v.props)
        )) }.iterator()

        fun commit(dataSize: Int) {
            session.writeTransaction { tx ->
                val res = tx.run(Query("UNWIND \$batch AS row" +
                        " CALL apoc.create.node([row.label], row.props) YIELD node" +
                        " RETURN row.alias AS alias, ID(node) AS id",
                    MapValue(mapOf("batch" to ListValue(*Array(dataSize) { batchIterator.next() })))
                ))
                mapFunction(Sequence { res }.map { it["alias"].asLong() to it["id"].asLong() })
            }
        }

        val batchSize = 30000
        val rem = nodesToCreate.size % batchSize
        for (i in 1..(nodesToCreate.size / batchSize)) {
            commit(batchSize)
        }

        //process remaining elements
        if (rem > 0) commit(rem)

        nodesToCreate.clear()
        n = 0
    }

//    private fun saveNodes(tx: Transaction) {
//
//    }

    fun commitRelationships(session: Session, mapFunction: (Sequence<Pair<Long, Long>>) -> Unit) {
        val batchData = refsToCreate.map { (_, v) -> MapValue(mapOf(
            "alias" to IntegerValue(v.alias),
            "from" to IntegerValue(v.startNode._id),
            "type" to StringValue(v.type),
            "props" to MapValue(v.props),
            "to" to IntegerValue(v.endNode._id)))
        }

        session.writeTransaction { tx ->
            val res = tx.run(Query("UNWIND \$batch AS row" +
                    " MATCH (from) WHERE ID(from) = row.from" +
                    " MATCH (to) WHERE ID(to) = row.to" +
                    " CALL apoc.create.relationship(from, row.type, row.props, to) YIELD rel" +
                    " RETURN row.alias AS alias, ID(rel) AS id",
                MapValue(mapOf("batch" to ListValue(*Array(batchData.size) { i -> batchData[i] })))
            ))
            mapFunction(Sequence { res }.map { it["alias"].asLong() to it["id"].asLong() })
        }
        refsToCreate.clear()
        r = 0
    }

    fun commitRelationshipsParallel(session: Session, mapFunction: (Sequence<Pair<Long, Long>>) -> Unit) {

    }

    fun commitRelationshipsNoIDs(session: Session) {

        /*val batchIterator = refsToCreate.map { (_, v) -> MapValue(mapOf(
            "alias" to IntegerValue(v.alias),
            "from" to IntegerValue(v.startNode._id),
            "type" to StringValue(v.type),
            "props" to MapValue(v.props),
            "to" to IntegerValue(v.endNode._id)))
        }*/

        //val batchData = refsToCreate.map { (_, v) -> MapValue(mapOf(
        val size = 100
        val batchIterator = (0L..size).asSequence().map { MapValue(mapOf(
            "alias" to IntegerValue(it),
            "from" to IntegerValue(it),
            "type" to StringValue("r"),
            "props" to MapValue(emptyMap()),
            "to" to IntegerValue(it)))
        }.iterator()

        session.writeTransaction { tx ->
/*            val res = tx.run(Query("CALL apoc.periodic.iterate(\"UNWIND \$batch AS row" +
                    " MATCH (from) WHERE ID(from) = row.from" +
                    " MATCH (to) WHERE ID(to) = row.to" +
                    " RETURN from, to, row.type AS rType, row.props AS props\"," +
                    "\"CALL apoc.create.relationship(from, rType, props, to) YIELD rel " +
                    " RETURN ID(rel)\"," +
                    //"\" CREATE (from)-[:rr]->(to) \"," +
                    " {batchSize:10000, parallel:true, params:{batch:\$batch}} ) YIELD batches, total" +
                    " RETURN batches, total",
                MapValue(mapOf("batch" to ListValue(*Array(size) { batchIterator.next() })))
            ))*/

            val res = tx.run(Query("CALL apoc.periodic.iterate(\"UNWIND \$batch AS row RETURN row\"," +
                    "\"MATCH (from) WHERE ID(from) = row.from" +
                    " MATCH (to) WHERE ID(to) = row.to" +
                    //" RETURN from, to, row.type AS rType, row.props AS props\"," +
                    "CALL apoc.create.relationship(from, row.type, row.props, to) YIELD rel " +
                    " RETURN ID(rel)\"," +
                    " {batchSize:10000, parallel:true, params:{batch:\$batch}} ) YIELD batches, total" +
                    " RETURN batches, total",
                MapValue(mapOf("batch" to ListValue(*Array(size) { batchIterator.next() })))
            ))
            println(res.single()["batches"].asLong() )
        }

        refsToCreate.clear()
        r = 0
    }

    /*
    fun commitSubGraph(session: Session /*,mapFunction: (Sequence<Pair<Long, Long>>) -> Unit*/) {
        val nodesIterator = Sequence { nodesToCreate.iterator() }.map { (_, v) ->
            MapValue(mapOf(
                "label" to StringValue(v.label),
                "alias" to IntegerValue(v.alias),
                "props" to MapValue(v.props)))
        }.iterator()

        val refsIterator = Sequence { refsToCreate2.iterator() }.map { (_, v) ->
            MapValue(mapOf(
                "alias" to IntegerValue(v.alias),
                "from" to StringValue(v.startAlias),
                "type" to StringValue(v.type),
                "props" to MapValue(v.props),
                "to" to StringValue(v.endAlias)))
        }.iterator()

        session.writeTransaction { tx ->
            val res = tx.run("UNWIND \$nodes as n" +
                    " CALL apoc.create.node([n.label], n.props) YIELD node" +
                    " WITH apoc.map.fromPairs(collect([n.alias, node])) AS map" +
                    " UNWIND \$refs as r" +
                    " WITH map[r.from] AS start, map[r.to] AS end, r" +
                    " CALL apoc.create.relationship(start, r.type, r.props, end) YIELD rel" +
                    " RETURN r.alias AS rAlias, rel, start.alias AS startAlias, end.alias AS endAlias",
                MapValue(mapOf(
                    "nodes" to ListValue(*Array(nodesToCreate.size) { nodesIterator.next() }),
                    "refs" to ListValue(*Array(refsToCreate2.size) { refsIterator.next() })))
            )

//            Sequence { res }.map {
//                it["rAlias"].asLong()
//            }

//            for (record in res) {
//                println("${record["rAlias"].asLong()} -> ${record["rel"].asRelationship().id()}}")
//            }
        }
    }*/
}