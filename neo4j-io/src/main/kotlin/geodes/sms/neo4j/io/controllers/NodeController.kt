package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.*
import geodes.sms.neo4j.io.entity.INodeEntity
import geodes.sms.neo4j.io.entity.RefBounds


internal class NodeController private constructor(
    private val mapper: Mapper,
    id: Long,
    override val label: String,
    override val props: MutableMap<String, Any>,
    val outRefBounds: Map<String, RefBounds>,    //rType --> count
    //propsDiff: HashMap<String, Value>,
    state: EntityState
) : INodeController, StateListener {
//:  NodeEntity(id)

    //override val _uuid: Int = n++
    override fun getState() = state.getState()
    override var _id: Long = id
        private set

    private var state: NodeState = when (state) {
        EntityState.NEW -> StateNew()
        EntityState.PERSISTED -> StatePersisted()
        EntityState.MODIFIED -> StateModified()
        EntityState.DETACHED -> StateDetached()
        else -> StateRemoved()
    }

    companion object {
        //private var n = 0
        fun createForNewNode(mapper: Mapper, id: Long, label: String,
                             outRefBounds: Map<String, RefBounds> = emptyMap()
            /*, props: HashMap<String, Value>*/
        ): NodeController {
            return NodeController(mapper, id, label, hashMapOf(),
                /*propsDiff = props,*/
                outRefBounds,
                state = EntityState.NEW)
        }

        fun createForDBNode(mapper: Mapper, id: Long, label: String, props: MutableMap<String, Any>,
                            outRefBounds: Map<String, RefBounds> = emptyMap()
        ): NodeController {
            return NodeController(mapper, id, label, props,
                /*propsDiff = hashMapOf(),*/
                outRefBounds,
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

    override fun onDetach() {
        state = StateDetached()
    }

    //override val props = hashMapOf<String, Any>() // mutable here
    //private val propsDiff = hashMapOf<String, Value>()
    override fun putProperty(name: String, value: Any?) {
        state.putProperty(name, value)
        TODO("dispatch types here!!")
    }

    override fun putUniqueProperty(name: String, value: Any?) {
        state.putUniqueProperty(name, value)
    }

    override fun createChild(
        rType: String,
        label: String,
        childRefBounds: Map<String, RefBounds>
    ): INodeController {
        return state.createChild(label, rType, childRefBounds)
    }

    override  fun createOutRef(rType: String, endNode: INodeEntity)/*: IRelationshipController*/ {
        if (endNode is INodeController)
            state.createOutRef(rType, endNode)
        else throw Exception("EndNode $endNode must be an instance of INodeController")
    }

    override fun remove() {
        state.remove()
    }

    override fun unload() {
        state.unload()
    }

    override fun removeChild(rType: String, childNode: INodeEntity) {
        state.removeChild(rType, childNode)
    }

    override  fun removeOutRef(rType: String, endNode: INodeEntity) {
        state.removeOutRef(rType, endNode)
    }

    override fun loadChildren(
        rType: String,
        endLabel: String,
        outRefBounds: Map<String, RefBounds>,
        filter: String,
        limit: Int
    ) : List<INodeController> {
        return state.loadChildren(rType, endLabel, outRefBounds, limit, filter)
    }

    private fun isUpperBoundExceeded(rType: String): Boolean {
        val b = outRefBounds[rType]
        return if (b != null) {
            if (b.upperBound - b.count > 0) {
                b.count++
                false
            } else true
        } else false
    }

    private fun isLowerBoundExceeded(rType: String): Boolean {
        val b = outRefBounds[rType]
        return if (b != null) {
            if (b.count - b.lowerBound > 0) {
                b.count--
                false
            } else true
        } else false
    }


    private interface NodeState {
        fun putProperty(name: String, input: Any?)
        fun putUniqueProperty(name: String, value: Any?)
        fun remove()
        fun unload()

        fun createChild(label: String, rType: String, childRefBounds: Map<String, RefBounds>): INodeController
        fun createOutRef(rType: String, end: INodeEntity)//: IRelationshipController

        fun removeChild(rType: String, n: INodeEntity)
        fun removeOutRef(rType: String, end: INodeEntity)

        //fun removeInputRef(rType: String, startNode: INodeEntity)
        //fun removeInputRef(rType: String, startID: Long)
        //fun _createInputRef(rType: String, start: INodeController)//: IRelationshipController
        //fun _createInputRef(rType: String, start: Long): IRelationshipController
        //fun _createOutputRef(rType: String, end: INodeEntity): IRelationshipController
        //fun _createOutputRef(rType: String, end: Long): IRelationshipController

        fun loadChildren(
            rType: String,
            endLabel: String,
            refBounds: Map<String, RefBounds> = emptyMap(),
            limit: Int = 100,
            filter: String = ""
        ): List<INodeController>

        //fun getChildrenFromCache(rType: String): Sequence<INodeController>
        fun getState(): EntityState
    }


    private inner class StateNew : NodeState {
        override fun putProperty(name: String, input: Any?) {
            TODO("Not yet implemented")
        }

        override fun putUniqueProperty(name: String, value: Any?) {
            val count = mapper.isPropertyUniqueForDB(name, value)
            if (count == 0) {
                putProperty(name, value)
            } else throw Exception("property $name already exists in DB")
        }

        override fun createChild(label: String, rType: String, childRefBounds: Map<String, RefBounds>): INodeController {
            if (isUpperBoundExceeded(rType)) throw Exception("Upper bound exceeded for ref '$rType'")
            else return mapper.createChild(this@NodeController, rType, label, childRefBounds)
        }

        override fun createOutRef(rType: String, end: INodeEntity) {
            if (isUpperBoundExceeded(rType)) throw Exception("Upper bound exceeded for ref '$rType'")
            else mapper.createRelationship(this@NodeController, rType, end)
        }

        override fun remove() { //remove from bufferedCreator
            mapper.removeNode(this@NodeController)
            state = StateRemoved()
        }

        override fun unload() { //unload from nodesToCreate map
            mapper.unload(this@NodeController)
            state = StateDetached()
        }

        override fun removeChild(rType: String, n: INodeEntity) {
            if (isLowerBoundExceeded(rType)) throw Exception("Lover bound limit for ref '$rType'")
            else mapper.removeChild(_id, rType, n)
        }

        override fun removeOutRef(rType: String, end: INodeEntity) {
            if (isLowerBoundExceeded(rType)) throw Exception("Lover bound limit for ref '$rType'")
            else if (end is INodeController) {
                mapper.removeRelationship(_id, rType, end)
            } else throw Exception("EndNode $end must be instance of INodeController")
        }

        override fun loadChildren(
            rType: String,
            endLabel: String,
            refBounds: Map<String, RefBounds>,
            limit: Int,
            filter: String
        ): List<INodeController> {
            TODO("Not yet implemented for new Node")
        }

        override fun getState() = EntityState.NEW
    }

    private open inner class StatePersisted : NodeState {
        override fun putProperty(name: String, input: Any?) {
            state = StateModified()
            //mapper.
        }

        override fun putUniqueProperty(name: String, value: Any?) {
            mapper.updateNodePropertyImmediately(_id, name, value)
        }

        override fun createChild(label: String, rType: String, childRefBounds: Map<String, RefBounds>): INodeController {
            if (isUpperBoundExceeded(rType)) throw Exception("Upper bound exceeded for ref '$rType'")
            else return mapper.createChild(this@NodeController, rType, label, childRefBounds)
        }

        override fun createOutRef(rType: String, end: INodeEntity) {
            if (isUpperBoundExceeded(rType)) throw Exception("Upper bound exceeded for ref '$rType'")
            else mapper.createRelationship(this@NodeController, rType, end)
        }

        override fun remove() {
            mapper.removeNode(_id)
            state = StateRemoved()
        }

        override fun unload() {
            mapper.unload(_id)
            state = StateDetached()
        }

        override fun removeChild(rType: String, n: INodeEntity) {
            if (isLowerBoundExceeded(rType)) throw Exception("Lover bound limit for ref '$rType'")
            else if (n is INodeController) {
                when {
                    n.getState() == EntityState.NEW -> mapper.removeChild(_id, rType, n)
                    n.getState() == EntityState.PERSISTED -> mapper.removeChild(_id, rType, n._id)
                    else -> throw Exception("End node was removed or unloaded")
                }
            } else throw Exception("object $n must be instance of INodeController")
        }

        override fun removeOutRef(rType: String, end: INodeEntity) {
            if (isLowerBoundExceeded(rType)) throw Exception("Lover bound limit for ref '$rType'")
            else if (end is INodeController) {
                when {
                    end.getState() == EntityState.NEW -> mapper.removeRelationship(_id, rType, end)
                    end.getState() == EntityState.PERSISTED -> mapper.removeRelationship(_id, rType, end._id)
                    else -> throw Exception("End node was removed or unloaded")
                }
            } else throw Exception("object $end must be instance of INodeController")
        }

        override fun loadChildren(
            rType: String,
            endLabel: String,
            refBounds: Map<String, RefBounds>,
            limit: Int,
            filter: String
        ): List<INodeController> {
            return mapper.loadConnectedNodes(_id, rType, endLabel, refBounds, filter, limit)
        }

        override fun getState() = EntityState.PERSISTED
    }

    private inner class StateModified : StatePersisted() {
        override fun putProperty(name: String, input: Any?) {
            TODO()
        }

        override fun getState() = EntityState.MODIFIED
    }

    private abstract inner class StateNotActive(val exceptionMsg: String): NodeState {
        override fun putProperty(name: String, input: Any?) = throw Exception(exceptionMsg)
        override fun putUniqueProperty(name: String, value: Any?) {
            throw Exception(exceptionMsg)
        }

        override fun remove() = throw Exception(exceptionMsg)
        override fun unload() = throw Exception(exceptionMsg)

        override fun createChild(
            label: String,
            rType: String,
            childRefBounds: Map<String, RefBounds>
        ): INodeController {
            throw Exception(exceptionMsg)
        }

        override fun createOutRef(rType: String, end: INodeEntity) {
            throw Exception(exceptionMsg)
        }

        override fun removeChild(rType: String, n: INodeEntity) {
            throw Exception(exceptionMsg)
        }

        override fun removeOutRef(rType: String, end: INodeEntity) {
            throw Exception(exceptionMsg)
        }

        override fun loadChildren(
            rType: String,
            endLabel: String,
            refBounds: Map<String, RefBounds>,
            limit: Int,
            filter: String
        ): List<INodeController> {
            throw Exception(exceptionMsg)
        }
    }

    private inner class StateRemoved : StateNotActive(
        "Node '$this' was removed. Cannot perform operation on removed node"
    ) {
        override fun getState() = EntityState.REMOVED
    }

    private inner class StateDetached : StateNotActive(
        "Node '$this' was unloaded. Cannot perform operation on unloaded node"
    ) {
        override fun getState(): EntityState = EntityState.DETACHED
    }
}