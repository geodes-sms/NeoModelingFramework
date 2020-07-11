package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.controllers.IRelationshipController
import geodes.sms.neo4j.io.controllers.NodeController
import geodes.sms.neo4j.io.entity.INodeEntity
import geodes.sms.neo4j.io.entity.RelationshipEntity
import org.neo4j.driver.Session
import org.neo4j.driver.Values
import java.util.*
import kotlin.collections.HashMap


class Mapper(
    private val creator: BufferedCreator,
    private val updater: BufferedUpdater,
    private val remover: BufferedRemover,
    private val reader: DBReader
) {
    private val nodesToCreate = hashMapOf<Long, NodeController>()
    //private val refsToCreate = hashMapOf<Long, RelationshipController>()

    private val nodesToUpdate = hashMapOf<Long, NodeController>() //StateListener.Updatable
    //private val refsToUpdate = hashMapOf<Long, StateListener.Updatable>()

    /** Cache startNode alias (or ID for persisted node) --> (out ref type --> RelationshipEntity) */
    private val adjOutput = hashMapOf<Long, HashMap<String, LinkedList<RelationshipEntity>>>()

    /** DB nodeID --> NodeController */
    private val trackedNodes = hashMapOf<Long, NodeController>()

    fun createNode(label: String, props: Map<String, Any> = emptyMap()): INodeController {
        //val props = hashMapOf<String, Value>()   //create from kotlin map
        val innerID = creator.createNode(label/*, props*/)
        val node = NodeController.createForNewNode(this, innerID, label/*, props*/)
        nodesToCreate[innerID] = node
        return node
    }

    fun createChild(
        parent: INodeEntity,
        rType: String, childLabel: String
    ): INodeController { //:Pair<IRelationshipController, INodeController> {
        val childAlias = creator.createNode(childLabel)
        val nc = NodeController.createForNewNode(this, childAlias, childLabel)

        val rAlias = creator.createRelationship(rType, parent, nc,
            mapOf("containment" to Values.value(true)))
        nodesToCreate[nc._id] = nc

        adjOutput.getOrPut(parent._id) { hashMapOf() }
            .getOrPut(rType) { LinkedList() }
            .add(RelationshipEntity(rAlias, rType, parent, nc))
        return nc
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
            .add(RelationshipEntity(rAlias, rType, startNode, endNode))
    }

    // -------------------- READ section -------------------- //
    fun loadNode(id: Long, label: String): INodeController {
        val node = reader.findNodeWithOutputsCount(id, label)
        val nc = NodeController.createForDBNode(this, node.id, label, node.props, node.outRefCount)
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
                val nc = NodeController.createForDBNode(
                    this, it.id, endLabel,
                    props = it.props,
                    outRefCount = it.outRefCount
                )
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

    fun isPropertyUniqueForDB(propName: String, propValue: Any): Boolean {
        return reader.getNodeCountWithProperty(propName, propValue) == 0
    }

    fun isPropertyUniqueForCache(propName: String, propValue: Any): Boolean {
        val iterator = nodesToCreate.iterator()
        while (iterator.hasNext()) {
            val (_,v) = iterator.next()
            if (v.props[propName] == propValue) {
                return false
            }
        }
        return true
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
        //TODO("cascade remove on buffer not implemented in this function")
        val refList = adjOutput[startAlias]?.get(rType)
        if (refList != null) {
            val iterator = refList.iterator()
            while (iterator.hasNext()) {
                val re = iterator.next()
                if (re.endNode == endNode) {
                    creator.popRelationshipCreate(re._id)
                    creator.popNodeCreate(re.endNode._id)
                    nodesToCreate.remove(endNode._id)
                    iterator.remove()
                    break
                }
            }
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
//    fun updateNode(nc: NodeController, props: Map<String, Value>) {
//        // (id, props, Updatable)
//        nodesToUpdate[nc.id] = nc
//        updater.updateNode(nc.id, props)
//    }

//    fun updateRelationship(rc: RelationshipController) {
//
//    }

    fun putNodePropertyImmediately(nodeID: Long, propName: String, propVal: Any?) {
        updater.putNodePropertyImmediately(nodeID, propName, propVal)
    }

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
        updater.commitNodesUpdate(session)
        nodesToUpdate.forEach{ (_, v) -> v.onUpdate() }
        nodesToUpdate.clear()
        adjOutput.clear()
        //updater.commitRelationshipsUpdates(session)
    }

    fun clearCache() {
        trackedNodes.clear()
        adjOutput.clear()
    }
}