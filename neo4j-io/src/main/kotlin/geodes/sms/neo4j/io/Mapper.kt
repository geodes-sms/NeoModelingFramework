package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.controllers.IRelationshipController
import geodes.sms.neo4j.io.controllers.NodeController
import geodes.sms.neo4j.io.entity.INodeEntity
import geodes.sms.neo4j.io.entity.RelationshipEntity
import org.neo4j.driver.Session
import org.neo4j.driver.Value
import org.neo4j.driver.Values
import java.util.*
import kotlin.collections.HashMap


internal class Mapper(
    private val creator: BufferedCreator,
    private val updater: BufferedUpdater,
    private val remover: BufferedRemover,
    private val reader: DBReader
) {
    private val nodesToCreate = hashMapOf<Long, NodeController>()
    //private val refsToCreate = hashMapOf<Long, RelationshipController>()

    /** Cache startNode alias (or ID for persisted node) --> (out ref type --> RelationshipEntity) */
    private val adjOutput = hashMapOf<Long, HashMap<String, LinkedList<RelationshipEntity>>>()

    /** DB nodeID --> NodeController */
    private val trackedNodes = hashMapOf<Long, NodeController>()

    fun createNode(label: String): INodeController {
        val propsDiff = hashMapOf<String, Value>() //props.entries.associateBy({ it.key }, { Values.value(it.value) })
        val innerID = creator.createNode(label, propsDiff)
        val node = NodeController.createForNewNode(this, innerID, label, propsDiff)
        nodesToCreate[innerID] = node
        return node
    }

    fun createChild(
        parent: INodeEntity,
        rType: String, childLabel: String
    ): INodeController {
        val propsDiff = hashMapOf<String, Value>()
        val childAlias = creator.createNode(childLabel, propsDiff)
        val childNode = NodeController.createForNewNode(this, childAlias, childLabel, propsDiff)

        val rAlias = creator.createRelationship(rType, parent, childNode,
            mapOf("containment" to Values.value(true)))
        nodesToCreate[childNode._id] = childNode

        adjOutput.getOrPut(parent._id) { hashMapOf() }
            .getOrPut(rType) { LinkedList() }
            .add(RelationshipEntity(rAlias, rType, parent, childNode, true))
        return childNode
    }

    fun createRelationship(
        startNode: INodeEntity,
        rType: String,
        endNode: INodeEntity
    )/*: IRelationshipController*/ {
        val rAlias = creator.createRelationship(rType, startNode, endNode,
            mapOf("containment" to Values.value(false))
        )

        adjOutput.getOrPut(startNode._id) { hashMapOf() }
            .getOrPut(rType) { LinkedList() }
            .add(RelationshipEntity(rAlias, rType, startNode, endNode, false))
    }

    // -------------------- READ section -------------------- //
    fun loadNode(id: Long, label: String): INodeController {
        val node = reader.findNodeWithOutputsCount(id, label)
        val nc = NodeController.createForDBNode(this, node.id, label, node.outRefCount)
        trackedNodes.remove(nc._id)?.onDetach() //unload prev if exist
        trackedNodes[nc._id] = nc  //merge Node if already exist in cache!!
        return nc
    }

    fun loadConnectedNodes(
        startID: Long,
        rType: String,
        endLabel: String,
        filter: String = "",
        limit: Int
    ): List<INodeController> {
        val result = LinkedList<INodeController>()
        reader.findConnectedNodesWithOutputsCount(startID, rType, endLabel, filter, limit) { res ->
            res.forEach {
                val nc = NodeController.createForDBNode(this, it.id, endLabel, it.outRefCount)
                trackedNodes.remove(nc._id)?.onDetach()
                trackedNodes[nc._id] = nc
                result.add(nc)
                //nc
            }
        }
        return result
    }

    fun loadWithCustomQuery(query: String) {

    }

    // Two nodes with different labels may have the same property value
    fun isPropertyUniqueForDBNode(label: String, propName: String, propValue: Value): Boolean {
        return reader.getNodeCountWithProperty(label, propName, propValue) == 0
    }

    fun isPropertyUniqueForCacheNode(label: String, propName: String, propValue: Any): Boolean {
        for ((_, v) in nodesToCreate)
            if (v.label == label && v.getPropertyAsAny(propName) == propValue) return false
        return true
    }

    fun readNodeProperty(id: Long, propName: String): Value {
        return reader.readNodeProperty(id, propName)
    }

    fun readRelationshipProperty(id: Long, propName: String): Value {
        return reader.readRelationshipProperty(id, propName)
    }
    // ------------------ READ section end ------------------- //


    // -------------------- REMOVE section -------------------- //
    fun removeNode(node: INodeEntity) {
        creator.popNodeCreate(node._id)
        nodesToCreate.remove(node._id)
    }

    fun removeNode(id: Long) {
        remover.removeNode(id)
    }

    //from db
    fun removeChild(startID: Long, rType: String, endID: Long) {
        remover.removeChild(startID, rType, endID)
    }

    //from cache
    fun removeChild(startAlias: Long, rType: String, endNode: INodeEntity) {
        var removedFl = false
        val refList = adjOutput[startAlias]?.get(rType)
        if (refList != null) {
            val iterator = refList.iterator()
            while (iterator.hasNext()) {
                val re = iterator.next()
                if (re.endNode._id == endNode._id) {    //find nodeToRemove
                    creator.popRelationshipCreate(re._id)
                    creator.popNodeCreate(re.endNode._id)
                    nodesToCreate.remove(endNode._id)
                    removedFl = true
                    iterator.remove()
                    if (re.endNode is NodeController)
                        re.endNode.onRemove()
                    break
                }
            }
        }
        if (removedFl) removeContainmentsCascade(endNode._id)
    }

    private fun removeContainmentsCascade(id: Long) {
        val stack = LinkedList<Long>()
        stack.push(id)
        while (stack.isNotEmpty()) {
            val startID = stack.pop()
            adjOutput[startID]?.let { map ->
                for ((_, refs) in map) {
                    while (refs.isNotEmpty()) {
                        val re = refs.pop()
                        if (re.isContainment) {
                            val endID = re.endNode._id
                            creator.popNodeCreate(endID)
                            nodesToCreate.remove(endID)
                            stack.push(endID)
                            if (re.endNode is NodeController)
                                re.endNode.onRemove()
                        }
                        creator.popRelationshipCreate(re._id)
                    }
                }
            }
            adjOutput.remove(startID)
        }
    }

    fun removeRelationship(rel: IRelationshipController) {
        creator.popRelationshipCreate(rel._id)
    }

    // from cache
    fun removeRelationship(startAlias: Long, rType: String, endNode: INodeEntity) {
        val refList = adjOutput[startAlias]?.get(rType)
        if (refList != null) {
            val iterator = refList.iterator()
            while (iterator.hasNext()) {
                val re = iterator.next()
                if (re.endNode == endNode) {
                    creator.popRelationshipCreate(re._id)
                    iterator.remove()
                    break
                }
            }
        }
    }

    // from BD
    fun removeRelationship(start: Long, rType: String, end: Long) {
        remover.removeRelationship(start, rType, end)
    }


//    fun popNodeRemove(id: Long) {}
//    fun popRelationshipRemove(id: Long) {}
    // -------------------- REMOVE section end ---------------- //


    // -------------------- UPDATE section -------------------- //
    fun updateNode(id: Long, propName: String, prValue: Value) {
        updater.updateNode(id, propName, prValue)
    }

//    fun updateRelationship(rc: RelationshipController) {
//
//    }

//    fun putNodePropertyImmediately(nodeID: Long, propName: String, propVal: Value) {
//        updater.putNodePropertyImmediately(nodeID, propName, propVal)
//    }

    fun popNodeUpdate(nodeID: Long) {
        updater.popNodeUpdate(nodeID)
    }

    fun popRelationshipUpdate(refID: Long) {
        updater.popRelationshipUpdate(refID)
    }
    // -------------------- UPDATE section end ---------------- //


    // -------------------- UNLOAD section -------------------- //
    fun unload(n: INodeEntity) {
        nodesToCreate.remove(n._id)
    }

    fun unload(id: Long) {
        trackedNodes.remove(id)
        updater.popNodeUpdate(id)
    }
    // -------------------- UNLOAD section end ---------------- //


    fun saveChanges(session: Session) {
        remover.commitContainmentsRemove(session) { ids ->
            for (id in ids) {
                trackedNodes.remove(id)?.onRemove()
            }
        }
        remover.commitRelationshipsRemoveByHost(session)
        updater.commitNodesUpdate(session)
        creator.commitNodes(session) {
            it.forEach { (alias, id) ->
                val nc = nodesToCreate[alias]
                if (nc != null) {
                    nc.onCreate(id)
                    trackedNodes[id] = nc
                }
            }
            nodesToCreate.clear()
        }
        creator.commitRelationshipsNoIDs(session)
        adjOutput.clear()
    }

    fun clearCache() {
        trackedNodes.forEach { (_, v) -> v.onDetach() }
        trackedNodes.clear()
        adjOutput.clear()
    }
}