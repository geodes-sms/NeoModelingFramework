package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.*
import geodes.sms.neo4j.io.entity.INodeEntity


internal class NodeController private constructor(
    private val mapper: Mapper,
    private val cache: EMFGraph<NodeController, RelationshipController>,
    id: Long,
    override val label: String,
    override val props: MutableMap<String, Any>,
    //private val propsDiff: HashMap<String, Value>,
    state: EntityState
) : INodeController, StateListener.Creatable, StateListener.Updatable, StateListener.Removable {
//:  NodeEntity(id)

    override val _uuid: Int = n++
    override fun _getState() = state.getState()
    override var _id: Long = id
        private set

    private var state: NodeState = when (state) {
        EntityState.NEW -> StateNew()
        EntityState.PERSISTED -> StatePersisted()
        EntityState.MODIFIED -> StateModified()
        else -> StateRemoved()
    }

    companion object {
        private var n = 0

        fun createForNewNode(mapper: Mapper,
            cache: EMFGraph<NodeController, RelationshipController>,
            id: Long, label: String/*, props: HashMap<String, Value>*/
        ): NodeController {
            return NodeController(mapper, cache, id, label, props = hashMapOf(), /*propsDiff = props,*/
                state = EntityState.NEW)
        }

        fun createForDBNode(mapper: Mapper,
            cache: EMFGraph<NodeController, RelationshipController>,
            id: Long, label: String, props: MutableMap<String, Any>
        ): NodeController {
            return NodeController(mapper,cache, id, label, props = props, /*propsDiff = hashMapOf(),*/
                state = EntityState.PERSISTED)
        }
    }

    override fun onCreate(id: Long) {
        this._id = id
        this.state = StatePersisted()
    }

    override fun onUpdate() {
        //propsDiff.clear()
        state = StatePersisted()
    }

    override fun onRemove() {
        state = StateRemoved()
    }

//    override fun onUnload() {
//
//    }

    //override val props = hashMapOf<String, Any>() // mutable here
    //private val propsDiff = hashMapOf<String, Value>()
    override fun putProperty(name: String, value: Any?) {
        state.putProperty(name, value)
        TODO("dispatch types here!!")
    }

    override fun createChild(label: String, rType: String): INodeController {
        return state.createChild(label, rType)
    }

    override  fun createOutRef(rType: String, endNode: INodeEntity): IRelationshipController {
        return state.createOutRef(rType, endNode)
    }

    override fun remove() {
        TODO("Not yet implemented")
    }

    override fun unload() {
        TODO("Not yet implemented")
    }

    override fun removeChild(rType: String, node: INodeEntity) {

    }

    override  fun removeOutRef(rType: String, node: INodeEntity) {

    }

    override fun loadChildren(
        rType: String,
        endLabel: String,
        limit: Int,
        filter: String
    ) : List<INodeController> {
        return state.loadChildren(rType, endLabel, filter, limit)
    }

    override fun getChildrenFromCache(rType: String): List<INodeController> {
        TODO()
    }

    /*  First true api
    //-------------------------------------------------------
    // ---------- Create block ----------
    /*
    fun createContainment(rType: String) : INodeController {}*/

    fun createChild(rType: String): INodeController {
        TODO()
    }

    fun createOutRef(rType: String, end: INodeEntity /*INodeController*/) {

    }

    // ---------- Read block ----------
    /*fun getContainments(rType: String): List<INodeController> {

    }*/

    // get refController
    fun getOutRefs(rType: String): List<IRelationshipController> {
        TODO()
    }

    // get endNode controller; sync cache
    fun getChildren(rType: String): List<INodeController> {
        TODO()
    }

    fun getChildrenFromCache() {

    }

    // ---------- Remove block ----------
    /*
    fun removeContainment(nc: INodeController)/*: Boolean*/ {

    }*/

    fun removeChild(rType: String,/* ":containment" */ nc: INodeController)/*: Boolean*/ {

    }

    fun removeOutRef(rType: String, endNc: INodeEntity /*INodeController*/) {

    }
    //-------------------------------------------------------
*/

    private interface NodeState {
        fun putProperty(name: String, input: Any?)
        //fun getProperty(name: String)

        fun createChild(label: String, rType: String): INodeController
        fun createOutRef(rType: String, end: INodeEntity): IRelationshipController
        //fun createInputRef()

        fun removeChild(rType: String, n: INodeEntity)
        fun removeOutRef(rType: String, n: INodeEntity)

        fun _createInputRef(rType: String, start: INodeController): IRelationshipController
        fun _createInputRef(rType: String, start: Long): IRelationshipController
        //fun _createOutputRef(rType: String, end: INodeEntity): IRelationshipController
        //fun _createOutputRef(rType: String, end: Long): IRelationshipController

        fun loadChildren(rType: String, endLabel: String, filter: String = "", limit: Int = 100) : List<INodeController>
        fun getChildrenFromCache(rType: String): List<INodeController>

        fun getState(): EntityState
    }


    private inner class StateNew : NodeState {
        override fun putProperty(name: String, input: Any?) {
            TODO("Not yet implemented")
        }

        override fun createChild(label: String, rType: String): INodeController {
            val (node, ref) = mapper.createChild(this@NodeController, rType, label)
            return node
        }

        override fun createOutRef(rType: String, end: INodeEntity): IRelationshipController {
            val endNC = cache.getNode(end._uuid)
            return endNC?.state?._createInputRef(rType, this@NodeController)
                ?: throw Exception("Start node was unloaded or removed")
        }

        override fun removeChild(rType: String, n: INodeEntity) {
            val endNC = cache.getNode(n._uuid)
        }

        override fun removeOutRef(rType: String, n: INodeEntity) {
            TODO("Not yet implemented")
        }

        override fun _createInputRef(rType: String, start: INodeController): IRelationshipController {
            return mapper.createRelationship(rType, start, this@NodeController)
        }

        override fun _createInputRef(rType: String, start: Long): IRelationshipController {
            return mapper.createRelationship(rType, start, this@NodeController)
        }

        override fun loadChildren(rType: String, endLabel: String, filter: String, limit: Int): List<INodeController> {
            TODO("Not yet implemented")
        }

        override fun getChildrenFromCache(rType: String): List<INodeController> {
            TODO("Not yet implemented")
        }

        override fun getState() = EntityState.NEW
    }

    private open inner class StatePersisted : NodeState {
        override fun putProperty(name: String, input: Any?) {
            state = StateModified()
            TODO()
        }

        override fun createChild(label: String, rType: String): INodeController {
            val (node, ref) = mapper.createChild(_id, rType, label)
            return node
        }

        override fun createOutRef(rType: String, end: INodeEntity): IRelationshipController {
            TODO("Not yet implemented")
        }

        override fun removeChild(rType: String, n: INodeEntity) {
            TODO("Not yet implemented")
        }

        override fun removeOutRef(rType: String, n: INodeEntity) {
            TODO("Not yet implemented")
        }

        override fun _createInputRef(rType: String, start: INodeController): IRelationshipController {
            TODO("Not yet implemented")
        }

        override fun _createInputRef(rType: String, start: Long): IRelationshipController {
            TODO("Not yet implemented")
        }

        override fun loadChildren(rType: String, endLabel: String, filter: String, limit: Int): List<INodeController> {
            TODO("Not yet implemented")
        }

        override fun getChildrenFromCache(rType: String): List<INodeController> {
            TODO("Not yet implemented")
        }

        override fun getState() = EntityState.PERSISTED
    }

    private inner class StateModified : StatePersisted() {
        override fun putProperty(name: String, input: Any?) {
            TODO()
        }

    /*
        inherit from StatePersisted class
        override fun createChild(label: String, rType: String): INodeController {
            TODO("Not yet implemented")
        }

        override fun createOutRef(rType: String, end: INodeEntity): IRelationshipController {
            TODO("Not yet implemented")
        }

        override fun removeChild(rType: String, n: INodeEntity) {
            TODO("Not yet implemented")
        }

        override fun removeOutRef(rType: String, n: INodeEntity) {
            TODO("Not yet implemented")
        }

        override fun _createInputRef(
            rType: String,
            start: INodeController
        ): IRelationshipController {
            TODO("Not yet implemented")
        }

        override fun _createInputRef(rType: String, start: Long): IRelationshipController {
            TODO("Not yet implemented")
        }

        override fun loadChildren(rType: String): List<INodeController> {
            TODO("Not yet implemented")
        }
    */

        override fun getState() = EntityState.MODIFIED
    }

    private inner class StateRemoved : NodeState {
        private val exceptionMsg = "Node was removed. Cannot perform operation on removed node"
        override fun putProperty(name: String, input: Any?) = throw Exception(exceptionMsg)

        override fun createChild(label: String, rType: String): INodeController {
            throw Exception(exceptionMsg)
        }

        override fun createOutRef(rType: String, end: INodeEntity): IRelationshipController {
            throw Exception(exceptionMsg)
        }

        override fun removeChild(rType: String, n: INodeEntity) {
            throw Exception(exceptionMsg)
        }

        override fun removeOutRef(rType: String, n: INodeEntity) {
            throw Exception(exceptionMsg)
        }

        override fun _createInputRef(rType: String, start: INodeController): IRelationshipController {
            throw Exception(exceptionMsg)
        }

        override fun _createInputRef(rType: String, start: Long): IRelationshipController {
            throw Exception(exceptionMsg)
        }

        override fun loadChildren(
            rType: String, endLabel: String, filter: String, limit: Int
        ): List<INodeController> = throw Exception(exceptionMsg)


        override fun getChildrenFromCache(rType: String): List<INodeController> {
            TODO("Not yet implemented")
        }

        override fun getState() = EntityState.REMOVED
    }

    //private inner class StateUnloaded
}