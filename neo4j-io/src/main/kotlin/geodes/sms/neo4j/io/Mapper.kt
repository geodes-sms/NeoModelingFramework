package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.controllers.IRelationshipController
import geodes.sms.neo4j.io.controllers.NodeController
import geodes.sms.neo4j.io.controllers.RelationshipController
import geodes.sms.neo4j.io.entity.NodeEntity
import org.neo4j.driver.Session
import org.neo4j.driver.Values


class Mapper(
    private val creator: BufferedCreator,
    private val updater: BufferedUpdater,
    private val remover: BufferedRemover,
    private val reader: DBReader
) {
    private val nodesToCreate = hashMapOf<Long, NodeController>()
    private val refsToCreate = hashMapOf<Long, RelationshipController>()

    private val nodesToUpdate = hashMapOf<Long, StateListener.Updatable>()
    private val refsToUpdate = hashMapOf<Long, StateListener.Updatable>()

    //private val nodesToRemove = hashMapOf<Long, StateListener.Removable>()
    //private val refsToRemove = hashMapOf<Long, StateListener.Removable>()

    /** DB id to uuid mapping */
    private val nodesUUID = hashMapOf<Long, Int>()
    private val refsUUID = hashMapOf<Long, Int>()
    private val graphCache = EMFGraph<NodeController, RelationshipController>()


    fun createNode(label: String/*, props: Map<String, Any> = emptyMap()*/): INodeController {
        //val props = hashMapOf<String, Value>()   //create from kotlin map
        val innerID = creator.createNode(label)
        val node = NodeController.createForNewNode(this, graphCache, innerID, label/*, props*/)
        graphCache.putNode(node)
        nodesToCreate[innerID] = node
        return node
    }

    fun createChild(
        parent: INodeController,
        rType: String, childLabel: String/*,
         childProps: Map<String, Value> = emptyMap()*/
    ): Pair<INodeController, IRelationshipController> {
        //val child = createNewNodeController(childLabel)
        //nodesToCreate[child.id] = child

        val child = createNode(childLabel/*, childProps*/) as NodeController
        val refInnerID = creator.createRelationship(
            rType, parent, end = child,
            props = mapOf("containment" to Values.value(true))
        )
        val ref = RelationshipController(this, refInnerID, rType, parent._uuid, child._uuid)
        graphCache.putNode(child)
        graphCache.putRelationship(ref)

        //nodesToCreate.add()
        refsToCreate[refInnerID] = ref

        return child to ref
    }

    fun createChild(
        parentID: Long,
        rType: String, childLabel: String
        /*,childProps: Map<String, Value> = emptyMap()*/
    ): Pair<INodeController, IRelationshipController> {
        //fix this
        val start = graphCache.getNode(nodesUUID[parentID]!!)!!
        val child = createNode(childLabel/*, childProps*/) as NodeController

        val refInnerID = creator.createRelationship(
            rType, start, end = child,
            props = mapOf("containment" to Values.value(true))
        )
        val ref = RelationshipController(this, refInnerID, rType, start._uuid, child._uuid)
        graphCache.putNode(child)
        graphCache.putRelationship(ref)

        //nodesToCreate.add()
        refsToCreate[refInnerID] = ref

        return child to ref
    }

    fun createRelationship(
        rType: String,
        startNode: INodeController,
        endNode: INodeController/*, props: Map<String, Value> = emptyMap()*/
    ): IRelationshipController {
        val innerID = creator.createRelationship(rType, startNode, endNode,
            mapOf("containment" to Values.value(true)))
        TODO()

    }

    fun createRelationship(
        rType: String,
        startNode: Long,
        endNode: INodeController/*, props: Map<String, Value> = emptyMap()*/
    ): IRelationshipController {
        TODO()
    }

    fun createRelationship(
        rType: String,
        startNode: INodeController,
        endNode: Long/*, props: Map<String, Value> = emptyMap()*/
    ): IRelationshipController {
        TODO()
    }

    fun createRelationship(
        rType: String,
        startNode: Long,
        endNode: Long/*, props: Map<String, Value> = emptyMap()*/
    ): IRelationshipController {
        TODO()
    }

    // -------------------- READ section -------------------- //
    fun loadNode(id: Long, label: String): INodeController {
        val dbNode = reader.findNodeByID(id)
        val nc = NodeController.createForDBNode(this, graphCache, dbNode.id(), label, dbNode.asMap())
        graphCache.putNode(nc)  //merge Node if already exist in cache!!
        return nc
    }

    fun loadConnectedNodes(startID: Long, rType: String, endLabel: String, filter: String = "", limit: Int = 100) {
        reader.findConnectedNodes(startID, rType, endLabel, filter, limit) {
            it.forEach { ctmRes ->
                val nc = NodeController.createForDBNode(this, graphCache, ctmRes.node.id(), endLabel,
                    ctmRes.node.asMap())
                //val rc = RelationshipController.createForDBRef()

            }
        }
    }
    // ------------------ READ section end ------------------- //


    // -------------------- REMOVE section -------------------- //
    fun removeNode(node: INodeController) {
        creator.popNodeCreate(node._id)
        nodesToCreate.remove(node._id)
        //graphCache.removeNode(node.uuid)
    }

    fun removeNode(nodeID: Long) {
        remover.removeNode(nodeID)
    }

    //fun rmRef()
    // -------------------- REMOVE section end ---------------- //

/*
    // -------------------- UPDATE section -------------------- //
    fun updateNode(nc: NodeController, props: Map<String, Value>) {
        // (id, props, Updatable)
        nodesToUpdate[nc.id] = nc
        updater.updateNode(nc.id, props)
    }

    fun updateRelationship(rc: RelationshipController) {

    }

    fun popNodeUpdate(innerID: Long) {

    }

    fun popRelationshipUpdate(innerID: Long) {

    }
    // -------------------- UPDATE section end ---------------- //

    // -------------------- REMOVE section -------------------- //
    fun removeNode(id: Long) {
        //nodesToRemove[id] =
        remover.removeNode(id)
    }

    fun removeRelationship(id: Long) {

    }

    fun popNodeRemove(id: Long) {}
    fun popRelationshipRemove(id: Long) {}
    // -------------------- REMOVE section end ---------------- //
*/

    fun saveChanges(session: Session) {

        remover.commitRelationshipsRemove(session) { ids ->
            for (id in ids) {
                val refUUID = refsUUID[id]
                if (refUUID != null) {
                    val rc = graphCache.removeRelationship(refUUID)
                    //rc.onRemove()
                }
            }
        }

        remover.commitNodesRemove(session) { ids ->
            for (id in ids) {
                val nodeUUID = nodesUUID[id]
                if (nodeUUID != null) {
                    val (nc, refs) = graphCache.removeNode(nodeUUID)
                    nc?.onRemove()
                    //refs.forEach { rc -> rc.onRemove() }
                }
            }
        }

        creator.commitNodes(session) {
            it.forEach { (alias, id) ->
                val nc = nodesToCreate[alias]
                if (nc != null) {
                    nc.onCreate(id)
                    nodesUUID[id] = nc._uuid
                }
            }
        }

        creator.commitRelationships(session) {
            it.forEach { (alias, id) ->
                val rc = refsToCreate[alias]
                if (rc != null) {
                    rc.onCreate(id)
                    refsUUID[id] = rc._uuid
                }
            }
        }

        updater.commitNodesUpdate(session)
        //updater.commitRelationshipsUpdates(session)
    }
}