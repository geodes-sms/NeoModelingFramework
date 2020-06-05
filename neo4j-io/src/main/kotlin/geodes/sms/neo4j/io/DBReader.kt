package geodes.sms.neo4j.io

import org.neo4j.driver.Driver
import org.neo4j.driver.Query
import org.neo4j.driver.internal.value.IntegerValue
import org.neo4j.driver.internal.value.MapValue
import org.neo4j.driver.internal.value.StringValue
import org.neo4j.driver.types.Node


class DBReader(private val driver: Driver) {

    fun findNodeByID(id: Long): Node {
        val session = driver.session()
        val node = session.readTransaction { tx ->
            val res = tx.run(Query("MATCH (node) WHERE ID(node)=\$id RETURN node",
                MapValue(mapOf("id" to IntegerValue(id)))
            ))
            res.single()["node"].asNode()
        }
        session.close()
        return node
    }

    fun findRelationshipByID(id: Long) {

    }

    fun findConnectedNodes(
        startID: Long, rType: String, endLabel: String,
        filter: String = "",
        limit: Int = 100,
        mapFunction: (Sequence<ContainmentResult>) -> Unit
    ) {
        val params = MapValue(mapOf(
            "startID" to IntegerValue(startID),
            "labelFilter" to StringValue("+$endLabel"),
            "refPattern" to StringValue("$rType>"),
            "limit" to IntegerValue(limit.toLong())
        ))

        return driver.session().readTransaction { tx ->
            val res = tx.run(Query("MATCH (start) WHERE ID(start)=\$startID" +
                    " CALL apoc.path.expand(start, \$refPattern, \$labelFilter, 1, 1) YIELD path " +
                    filter +
                    " RETURN nodes(path)[1] AS node, collect(ID(relationships(path)[0])) AS refID " +
                    " LIMIT \$limit", params)
            )
            mapFunction(Sequence { res }.map {
                ContainmentResult(it["node"].asNode(), it["refID"].asLong())
            })
        }
    }
}