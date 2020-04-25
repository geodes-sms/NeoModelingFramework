package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.controllers.NodeController
import org.neo4j.driver.Session
import org.neo4j.driver.Value
import org.neo4j.driver.Values


class Mapper(val creator: BufferedCreator, val updater: BufferedUpdater, val remover: BufferedRemover,
             val reader: DBReader
) {
    private val nodeIDToInit = hashMapOf<Long, StateListener.Creatable>()
    private val refIDToInit = hashMapOf<Long, StateListener.Creatable>()

    private val nodesToUpdate = hashMapOf<Long, StateListener.Updatable>()
    private val refsToUpdate = hashMapOf<Long, StateListener.Updatable>()

    private val nodesToRemove = hashMapOf<Long, StateListener.Removable>()
    private val refsToRemove = hashMapOf<Long, StateListener.Removable>()

    private val graphCache = GraphCache()


    fun createNewNodeController(label: String, props: Map<String, Value> = emptyMap()): INodeController {
        val innerID = creator.createNode(label, props)
        val nc = NodeController(innerID /*this*/)
        nodeIDToInit[innerID] = nc
        return nc
    }

    fun createNewNodeControllerFromParent(
        parent: INodeController, rType: String, childLabel: String,
        childProps: Map<String, Value> = emptyMap()
    ): INodeController {
        //val nodeInnerID = creator.createNode()
        val child = createNewNodeController(childLabel, childProps)

        val refInnerID = creator.createRelationship(
            rType, startNode = parent, endNode = child,
            props = mapOf("containment" to Values.value(true))
        )

        return child
    }

    fun createRefController(
        rType: String, startNode: INodeController,
        endNode: INodeController, props: Map<String, Value> = emptyMap()
    ) {

    }

    fun createNodeControllerFromExistingNode(id: Long) {
        //reader.findNodeByID(id)
    }

    fun updateNode() {

    }

    fun updateRef() {

    }

    fun removeNode() {

    }


    fun saveChanges(session: Session) {
        creator.writeNodes(session) {
            it.forEach { (alias, id) -> nodeIDToInit[alias]?.onCreate(id) }
        }

    }
}