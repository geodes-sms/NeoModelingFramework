package geodes.sms.neo4j.io

import org.neo4j.driver.Query
import org.neo4j.driver.Session
import org.neo4j.driver.internal.value.IntegerValue
import org.neo4j.driver.internal.value.ListValue
import org.neo4j.driver.internal.value.MapValue


class BufferedRemover {

    private val nodesToRemove = hashSetOf<Long>()
    private val refsToRemove = hashSetOf<Long>()

    fun removeNode(id: Long) {
        nodesToRemove.add(id)
    }

//    fun removeNodeCascading(id: Long/*, rType: String*/) {
//
//    }

    fun removeRelationship(id: Long) {
        refsToRemove.add(id)
    }

    fun popNodeRemove(id: Long) {
        nodesToRemove.remove(id)
    }

    fun popRelationshipRemove(id: Long) {
        refsToRemove.remove(id)
    }

    fun commitNodesRemove(session: Session, mapFunction: (Sequence<Long>) -> Unit) {
        if (nodesToRemove.isEmpty()) return

        val batchData = nodesToRemove.map { IntegerValue(it) }
        session.writeTransaction { tx ->
            val res = tx.run(Query("UNWIND \$batch AS id" +
                    " MATCH (n)-[*0..{containment:true}]->(d)" +
                    //" MATCH (n)-[*{containment:true}]->(d)" +
                    " WHERE ID(n)=id" +
                    " WITH d, ID(d) AS removedIDs" +
                    " DETACH DELETE d" +
                    " RETURN removedIDs",
                MapValue(mapOf("batch" to ListValue(*Array(batchData.size) {i -> batchData[i]} )))
            ))
            mapFunction(Sequence { res }.map { it["removedIDs"].asLong() })
        }
    }

    fun commitRelationshipsRemove(session: Session, mapFunction: (Sequence<Long>) -> Unit) {
        if (refsToRemove.isEmpty()) return

        val batchData = refsToRemove.map { IntegerValue(it) }
        session.writeTransaction { tx ->
            val res = tx.run(Query("UNWIND \$batch AS id" +
                    " MATCH ()-[r]->()" +
                    " WHERE ID(r)=id" +
                    " WITH ID(r) AS removedIDs" +
                    " DELETE d" +
                    " RETURN removedIDs",
                MapValue(mapOf("batch" to ListValue(*Array(batchData.size) {i -> batchData[i]} )))
            ))
            mapFunction(Sequence { res }.map { it["removedIDs"].asLong() })
        }
    }
}