package geodes.sms.nmf.loader.emf2neo4j

import org.eclipse.emf.ecore.resource.Resource
import org.neo4j.driver.*
import org.neo4j.driver.internal.value.ListValue
import org.neo4j.driver.internal.value.MapValue
import org.neo4j.driver.internal.value.StringValue


class ReflectiveBatchLoader(private val resource: Resource, driver: Driver) {
    private val session = driver.session()

    private val nodeStep = 10000
    private val nodeBatch = mutableListOf<MapValue>()  //Array(nodeStep) {Values.NULL}
    private var nodeCount = 0

    /** nodeAlias --> nodeID */
    private val nodeID = hashMapOf<String, Long>()


    fun load() {
        var cursor = 0
        for (eObject in resource.allContents) {
            val eClass = eObject.eClass()

            val props = hashMapOf<String, Value>()
            /*
            eClass.eAllAttributes.filter { eObject.eIsSet(it) }.forEach { eAttr ->
                node.setProperty(eAttr.name, eObject.eGet(eAttr, true))
            }*/

            val alias = "n${nodeCount++}"
            val batchRow = hashMapOf<String,Value>()
            batchRow["label"] = StringValue(eClass.name)
            batchRow["alias"] = StringValue(alias)
            batchRow["props"] = MapValue(props)

            nodeBatch.add(MapValue(batchRow))
            if (++cursor == nodeStep) {
                loadNodes()
                cursor = 0
            }
        }

        if (cursor > 0) loadNodes()

        session.close()
        println("nodes loaded: $nodeCount")
    }

    private fun loadNodes() {
        //println("loading nodes")

        session.writeTransaction { tx ->
            val res = tx.run(Statement("UNWIND \$batch AS row" +
                    " CALL apoc.create.node([row.label], row.props) yield node" +
                    " RETURN row.alias AS alias, id(node) AS id",
                MapValue(mapOf("batch" to ListValue(*Array(nodeBatch.size) { i -> nodeBatch[i] }))))
            )
            for (record in res) {
                nodeID[record["alias"].asString()] = record["id"].asLong()
            }
        }
        nodeBatch.clear()
    }


    fun loadRefs() {

    }
}