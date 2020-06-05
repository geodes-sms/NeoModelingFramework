package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.entity.INodeEntity
import org.neo4j.driver.Query
import org.neo4j.driver.Session
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.IntegerValue
import org.neo4j.driver.internal.value.ListValue
import org.neo4j.driver.internal.value.MapValue
import org.neo4j.driver.internal.value.StringValue


class BufferedCreator {
    private val nodesToCreate = hashMapOf<Long, NodeParameter>()
    private val refsToCreate = hashMapOf<Long, ReferenceParameter>()

    private val pathNodes = hashMapOf<Long, NodeParameter>()
    private val pathRefs = hashMapOf<Long, ReferenceParameter>()
    private var n: Long = 0

    fun createNode(label: String, props: Map<String, Value> = emptyMap()): Long {
        val alias = n--
        nodesToCreate[alias] = NodeParameter(alias, label, props)
        return alias
    }

    /*
    // -->() ; create new ref + new endNode
    fun createPathSegment(
        start: INodeEntity, rType: String, endLabel: String,
        nProps: Map<String, Value> = emptyMap(),
        rProps: Map<String, Value> = emptyMap()
    ) : Pair<INodeEntity, IRelationshipEntity> {
        val nAlias = n--
        nodesToCreate[nAlias] = NodeParameter(nAlias, endLabel, nProps)

        val rAlias = n--
        refsToCreate[rAlias] = ReferenceParameter(rAlias, rType, start, end, props)

        return Pair(NodeEntity(nAlias), RelationshipEntity(rAlias))
    }

    fun createPathSegment(
        start: Long, rType: String, endLabel: String,
        nProps: Map<String, Value> = emptyMap(),
        rProps: Map<String, Value> = emptyMap()
    ) {

    }*/

    fun createRelationship(
        rType: String, start: INodeEntity, end: INodeEntity,
        props: Map<String, Value> = emptyMap()
    ): Long {
        val alias = n--
        refsToCreate[alias] = ReferenceParameter(alias, rType, start, end, props)
        //return RelationshipEntity(alias)
        return alias
    }

    /*
    fun createRelationship(
        rType: String, start: Long, end: INodeEntity,
        props: Map<String, Value> = emptyMap()
    ): Long {
        TODO()
    }

    fun createRelationship(
        rType: String, start: INodeEntity, end: Long,
        props: Map<String, Value> = emptyMap()
    ): Long {
        TODO()
    }

    fun createRelationship(
        rType: String, start: Long, end: Long,
        props: Map<String, Value> = emptyMap()
    ): Long {
        TODO()
    }*/


    fun popNodeCreate(alias: Long) {

    }

    fun popRelationshipCreate(alias: Long) {

    }

    /*fun popPathCreate(id: Long) {

    }*/

    //fun createUncontrollableNode(label: String, props: Map<String, Value> = emptyMap()) {}
    //fun createUncontrollableReference() {}
    //fun flush() {}

    fun commitNodes(session: Session, mapFunction: (Sequence<Pair<Long, Long>>) -> Unit) {
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

    fun commitRelationships(session: Session, mapFunction: (Sequence<Pair<Long, Long>>) -> Unit) {
        val batchData = refsToCreate.values.map { MapValue(mapOf(
            "alias" to IntegerValue(it.alias),
            "from" to IntegerValue(it.startNode._id),
            "type" to StringValue(it.type),
            "props" to MapValue(it.props),
            "to" to IntegerValue(it.endNode._id)))
        }

        session.writeTransaction { tx ->
            val res = tx.run(Query("UNWIND \$batch AS row" +
                    " MATCH (from) WHERE ID(from) = row.from" +
                    " MATCH (to) WHERE ID(to) = row.to" +
                    " CALL apoc.create.relationship(from, row.type, row.props, to) YIELD rel" +
                    //" RETURN COUNT(rel) AS count",
                    " RETURN row.alias AS alias, ID(rel) AS id",
                MapValue(mapOf("batch" to ListValue(*Array(batchData.size) { i -> batchData[i] })))
            ))
            //res.single()["count"].asInt()

            mapFunction(Sequence { res }.map {
                it["alias"].asLong() to it["id"].asLong()
            })
        }
        refsToCreate.clear()
    }

    fun commitSubGraph(session: Session) {

        val nodes = pathNodes.values.map { MapValue(mapOf(
            "label" to StringValue(it.label),
            "alias" to IntegerValue(it.alias),
            "props" to MapValue(it.props)))
        }.toTypedArray()

        val refs = pathRefs.values.map { MapValue(mapOf(
            "from" to IntegerValue(it.startNode._id),
            "type" to StringValue(it.type),
            "props" to MapValue(it.props),
            "to" to IntegerValue(it.endNode._id)))
        }.toTypedArray()

        session.writeTransaction { tx ->
            val res = tx.run("UNWIND \$nodes as n" +
                    " CALL apoc.create.node([n.label], n.props) YIELD node" +
                    " WITH apoc.map.fromPairs(collect([n.alias, node])) as map" +
                    " UNWIND \$refs as r" +
                    " WITH apoc.map.get(map, r.from) AS start, apoc.map.get(map, r.to) AS end" +
                    " CALL apoc.create.relationship(start, r.type, r.props, end) YIELD rel" +
                    " RETURN rel, start.alias AS startAlias, end.alias AS endAlias",
                MapValue(mapOf("nodes" to ListValue(*nodes), "refs" to ListValue(*refs)))
            )
        }
    }

}