package geodes.sms.neo4j.io

import geodes.sms.neo4j.Values
import org.neo4j.driver.Driver
import org.neo4j.driver.Query
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.IntegerValue
import org.neo4j.driver.internal.value.MapValue
import org.neo4j.driver.internal.value.StringValue
import org.neo4j.driver.types.Node


class DBReader(private val driver: Driver) {

    fun findNode(id: Long, label: String): Node {
        val session = driver.session()
        val node = session.readTransaction { tx ->
            val res = tx.run(Query("MATCH (node)" +
                    " WHERE ID(node)=\$id AND labels(node)[0]=\$label" +
                    " RETURN node",
                MapValue(mapOf("id" to IntegerValue(id), "label" to StringValue(label)))
            ))
            res.single()["node"].asNode()
        }
        session.close()
        return node
    }

    fun findNodeWithOutputsCount(id: Long, label: String): NodeResult {
        val session = driver.session()
        val nodeRes = session.readTransaction { tx ->
            val res = tx.run(Query("MATCH (node)" +
                    " WHERE ID(node)=\$id AND labels(node)[0]=\$label" +
                    " OPTIONAL MATCH (node)-[r]->()" +
                    " WITH ID(node) AS id, type(r) AS rType, count(r) AS count" +
                    " RETURN id, apoc.map.fromPairs(collect([rType, count])) AS count",
                MapValue(mapOf("id" to IntegerValue(id), "label" to StringValue(label)))
            ))
            val record = res.single()
            val nodeID = record["id"].asLong()
            val outputsCount = record["count"].asMap { it.asInt() }
            NodeResult(nodeID, outputsCount)
        }
        session.close()
        return nodeRes
    }

//    fun findRelationshipByID(id: Long) {
//
//    }

//    /** return endNode */
//    fun findConnectedNodes(
//        startID: Long, rType: String,
//        filter: String = "",
//        limit: Int = 100
//        //mapFunction: (Sequence<ContainmentResult>) -> Unit
//    ): List<NodeResult> {
//        val params = MapValue(mapOf(
//            "startID" to IntegerValue(startID),
//            //"labelFilter" to StringValue("+$endLabel"),
//            "refPattern" to StringValue("$rType>"),
//            "limit" to IntegerValue(limit.toLong())
//        ))
//
//        return driver.session().readTransaction { tx ->
//            val res = tx.run(Query("MATCH (start) WHERE ID(start)=\$startID" +
//                    " CALL apoc.path.expand(start, \$refPattern, \$labelFilter, 1, 1) YIELD path " +
//                    filter +
//                    //" RETURN nodes(path)[1] AS node, collect(ID(relationships(path)[0])) AS refID " +
//                    " RETURN nodes(path)[1] AS endNode LIMIT \$limit", params)
//            )
//            res.stream().map { NodeResult( it["endNode"].asNode() )  }.toList()
//
//
////            mapFunction(Sequence { res }.map {
////                ContainmentResult(it["endNode"].asNode())
////            })
//        }
//    }

    /** @return endNode plus count of output refs aggregated by count*/
    fun findConnectedNodesWithOutputsCount(
        startID: Long, rType: String, endLabel: String,
        filter: String = "",
        limit: Int = 100,
        mapFunction: (Sequence<NodeResult>) -> Unit
    ) {
        val params = MapValue(mapOf(
            "startID" to IntegerValue(startID),
            "labelFilter" to StringValue("+$endLabel"),
            "refPattern" to StringValue("$rType>"),
            "limit" to IntegerValue(limit.toLong())
        ))

        val session = driver.session()
        session.readTransaction { tx ->
            val res = tx.run(Query("MATCH (start) WHERE ID(start)=\$startID" +
                    " CALL apoc.path.subgraphNodes(start, {" +
                    "  relationshipFilter: \$refPattern, labelFilter: \$labelFilter," +
                    "  minLevel:1,maxLevel:1}) YIELD node " +
                    filter +
                    " WITH node LIMIT \$limit" +
                    " OPTIONAL MATCH (node)-[r]->()" +
                    " WITH ID(node) AS id, type(r) AS rType, count(r) AS count" +
                    " RETURN id, apoc.map.fromPairs(collect([rType, count])) AS count", params)
            )
            mapFunction(Sequence { res }.map { record ->
                val nodeID = record["id"].asLong()
                val outputsCount = record["count"].asMap { it.asInt() }
                NodeResult(nodeID, outputsCount)
            })
        }
        session.close()
    }

    fun loadNodes(query: String) {
        val session = driver.session()
        session.readTransaction { tx ->
            tx.run(query)
        }
        session.close()
    }

    fun getNodeCountWithProperty(label: String, propName: String, propValue: Any): Int {
        val session = driver.session()
        val count = session.readTransaction { tx ->
            val res = tx.run(Query("MATCH (n:$label) WHERE n[\$property]=\$value RETURN count(n) AS count",
                MapValue(mapOf("property" to StringValue(propName), "value" to Values.value(propValue)))
            ))
            res.single()["count"].asInt()
        }
        session.close()
        return count
    }

    fun readNodeProperty(id: Long, propName: String): Value {
        val session = driver.session()
        val propValue = session.readTransaction { tx ->
            val res = tx.run(Query("MATCH (n) WHERE ID(n)=\$id RETURN n[\$property] AS prop",
                MapValue(mapOf("id" to IntegerValue(id), "property" to StringValue(propName)))
            ))
            res.single()["prop"]
        }
        session.close()
        return propValue
    }

    fun readRelationshipProperty(id: Long, propName: String): Value {
        val session = driver.session()
        val propValue = session.readTransaction { tx ->
            val res = tx.run(Query("MATCH ()-[r]-() WHERE ID(r)=\$id RETURN r[\$property] AS prop",
                MapValue(mapOf("id" to IntegerValue(id), "property" to StringValue(propName)))
            ))
            res.single()["prop"]
        }
        session.close()
        return propValue
    }
}