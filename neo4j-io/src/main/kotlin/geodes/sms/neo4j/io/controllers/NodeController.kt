package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.*
import geodes.sms.neo4j.io.entity.INodeEntity


internal class NodeController private constructor(
    private val mapper: Mapper,
    id: Long,
    override val label: String,
    override val props: MutableMap<String, Any>,
    override val outRefCount: MutableMap<String, Int>, //rType --> count
    //propsDiff: HashMap<String, Value>,
    state: EntityState
) : INodeController, StateListener {

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
        fun createForNewNode(mapper: Mapper, id: Long, label: String
            /*, props: HashMap<String, Value>*/
        ): NodeController {
            return NodeController(mapper, id, label, hashMapOf(),
                /*propsDiff = props,*/
                outRefCount = hashMapOf(),
                state = EntityState.NEW)
        }

        fun createForDBNode(mapper: Mapper, id: Long, label: String, props: MutableMap<String, Any>,
                            outRefCount: MutableMap<String, Int> = hashMapOf()
        ): NodeController {
            return NodeController(mapper, id, label, props,
                /*propsDiff = hashMapOf(),*/
                outRefCount,
                state = EntityState.PERSISTED)
        }
    }

    /////////////////////////////////////
    override fun onCreate(id: Long) {
        this._id = id
        this.state = StatePersisted()
    }

    override fun onUpdate() {
        //propsDiff.clear()
        state = StatePersisted()
    }

    override fun onRemove() {
        props.clear()
        outRefCount.clear()
        state = StateRemoved()
    }

    override fun onDetach() {
        props.clear()
        outRefCount.clear()
        state = StateDetached()
    }
    //////////////////////////////////////

    override fun putProperty(name: String, value: Any) {
        state.putProperty(name, value)
        TODO("dispatch types here!!")
    }

    override fun putUniqueProperty(name: String, value: Any) {
        if (mapper.isPropertyUniqueForCache(name, value) &&
            mapper.isPropertyUniqueForDB(name, value)
        ) {
            state.putUniqueProperty(name, value)
        } else throw Exception("Property already exists in DB")
    }

    override fun removeProperty(name: String) {
        state.removeProperty(name)
    }

    override fun createChild(rType: String, label: String): INodeController {
        return state.createChild(label, rType)
    }

    override fun createChild(rType: String, label: String, upperBound: Int): INodeController {
        return state.createChild(label, rType, upperBound)
    }

    override fun createOutRef(rType: String, endNode: INodeEntity, upperBound: Int) {
        state.createOutRef(rType, endNode, upperBound)
    }

    override  fun createOutRef(rType: String, endNode: INodeEntity)/*: IRelationshipController*/ {
        state.createOutRef(rType, endNode)
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

    override fun removeChild(rType: String, childNode: INodeEntity, lowerBound: Int) {
        state.removeChild(rType, childNode, lowerBound)
    }

    override  fun removeOutRef(rType: String, endNode: INodeEntity) {
        state.removeOutRef(rType, endNode)
    }

    override fun removeOutRef(rType: String, endNode: INodeEntity, lowerBound: Int) {
        state.removeOutRef(rType, endNode, lowerBound)
    }

    override fun loadChildren(
        rType: String,
        endLabel: String,
        filter: String,
        limit: Int
    ) : List<INodeController> {
        return state.loadChildren(rType, endLabel, limit, filter)
    }

    private interface NodeState {
        fun putProperty(name: String, input: Any)
        fun removeProperty(propName: String)
        fun putUniqueProperty(name: String, value: Any)
        fun remove()
        fun unload()

        fun createChild(label: String, rType: String): INodeController
        fun createChild(label: String, rType: String, upperBound: Int): INodeController

        fun createOutRef(rType: String, end: INodeEntity)
        fun createOutRef(rType: String, end: INodeEntity, upperBound: Int)

        fun removeChild(rType: String, n: INodeEntity)
        fun removeChild(rType: String, n: INodeEntity, lowerBound: Int)

        fun removeOutRef(rType: String, end: INodeEntity)
        fun removeOutRef(rType: String, end: INodeEntity, lowerBound: Int)

        //fun removeInputRef(rType: String, startNode: INodeEntity)
        //fun removeInputRef(rType: String, startID: Long)
        //fun _createInputRef(rType: String, start: INodeController)//: IRelationshipController
        //fun _createInputRef(rType: String, start: Long): IRelationshipController
        //fun _createOutputRef(rType: String, end: INodeEntity): IRelationshipController
        //fun _createOutputRef(rType: String, end: Long): IRelationshipController

        fun loadChildren(
            rType: String,
            endLabel: String,
            limit: Int = 100,
            filter: String = ""
        ): List<INodeController>

        //fun getChildrenFromCache(rType: String): Sequence<INodeController>
        fun getState(): EntityState
    }

    private abstract inner class StateActive : NodeState {
        override fun createChild(label: String, rType: String): INodeController {
            return mapper.createChild(this@NodeController, rType, label)
        }

        override fun createChild(label: String, rType: String, upperBound: Int): INodeController {
            val count = outRefCount.getOrDefault(rType, 0)
            return if (upperBound - count > 0) {
                outRefCount[rType] = count + 1
                mapper.createChild(this@NodeController, rType, label)
            } else throw Exception("Upper bound '$upperBound' exceeded for relationship '$rType'")
        }

        override fun createOutRef(rType: String, end: INodeEntity) {
            mapper.createRelationship(this@NodeController, rType, end)
        }

        override fun createOutRef(rType: String, end: INodeEntity, upperBound: Int) {
            val count = outRefCount.getOrDefault(rType, 0)
            if (upperBound - count > 0) {
                outRefCount[rType] = count + 1
                createOutRef(rType, end)
            } else throw Exception("Upper bound '$upperBound' exceeded for relationship '$rType'")
        }

        override fun removeChild(rType: String, n: INodeEntity, lowerBound: Int) {
            val count = outRefCount.getOrDefault(rType, 0)
            if (count - lowerBound > 0) {
                outRefCount[rType] = count - 1
                removeChild(rType, n)
            } else throw Exception("Lower bound '$lowerBound' exceeded for relationship '$rType'")
        }

        override fun removeOutRef(rType: String, end: INodeEntity, lowerBound: Int) {
            val count = outRefCount.getOrDefault(rType, 0)
            if (count - lowerBound > 0) {
                outRefCount[rType] = count - 1
                removeOutRef(rType, end)
            } else throw Exception("Lower bound '$lowerBound' exceeded for relationship '$rType'")
        }
    }

    private inner class StateNew : StateActive() {
        override fun putProperty(name: String, input: Any) {
            TODO("Not yet implemented")
        }

        override fun putUniqueProperty(name: String, value: Any) {
            props[name] = value
//            if (mapper.isPropertyUniqueForCache(name, value)) {
//                props[name] = value
//            } else throw Exception("property $name already exists in DB")
        }

        override fun removeProperty(propName: String) {
            props.remove(propName)
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
            mapper.removeChild(_id, rType, n)
        }

        override fun removeOutRef(rType: String, end: INodeEntity) {
            if (end is INodeController) {
                mapper.removeRelationship(_id, rType, end)
            } else throw Exception("EndNode $end must be instance of INodeController")
        }

        override fun loadChildren(
            rType: String,
            endLabel: String,
            limit: Int,
            filter: String
        ): List<INodeController> {
            TODO("Not yet implemented for new Node")
        }

        override fun getState() = EntityState.NEW
    }

    private open inner class StatePersisted : StateActive() {
        override fun putProperty(name: String, input: Any) {
            state = StateModified()
            //mapper.
        }

        override fun putUniqueProperty(name: String, value: Any) {
            props[name] = value
            mapper.putNodePropertyImmediately(_id, name, value)
        }

        override fun removeProperty(propName: String) {
            props.remove(propName)
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
            if (n is INodeController) {
                when (n.getState()) {
                    EntityState.NEW -> mapper.removeChild(_id, rType, n)
                    EntityState.PERSISTED,
                    EntityState.MODIFIED -> mapper.removeChild(_id, rType, n._id)
                    else -> throw Exception("End node was removed or unloaded")
                }
            } else throw Exception("object $n must be instance of INodeController")
        }

        override fun removeOutRef(rType: String, end: INodeEntity) {
            if (end is INodeController) {
                when (end.getState()) {
                    EntityState.NEW -> mapper.removeRelationship(_id, rType, end)
                    EntityState.PERSISTED,
                    EntityState.MODIFIED -> mapper.removeRelationship(_id, rType, end._id)
                    else -> throw Exception("End node was removed or unloaded")
                }
            } else throw Exception("object $end must be instance of INodeController")
        }

        override fun loadChildren(
            rType: String,
            endLabel: String,
            limit: Int,
            filter: String
        ): List<INodeController> {
            return mapper.loadConnectedNodes(_id, rType, endLabel, filter, limit)
        }

        override fun getState() = EntityState.PERSISTED
    }

    private inner class StateModified : StatePersisted() {
        override fun putProperty(name: String, input: Any) {
            TODO()
        }

        override fun getState() = EntityState.MODIFIED
    }

    private abstract inner class StateNotActive(val exceptionMsg: String): NodeState {
        override fun putProperty(name: String, input: Any) = throw Exception(exceptionMsg)
        override fun putUniqueProperty(name: String, value: Any) = throw Exception(exceptionMsg)
        override fun removeProperty(propName: String) = throw Exception(exceptionMsg)
        override fun remove() = throw Exception(exceptionMsg)
        override fun unload() = throw Exception(exceptionMsg)
        override fun createChild(label: String, rType: String) = throw Exception(exceptionMsg)
        override fun createChild(label: String, rType: String, upperBound: Int) =
            throw Exception(exceptionMsg)

        override fun createOutRef(rType: String, end: INodeEntity) = throw Exception(exceptionMsg)
        override fun createOutRef(rType: String, end: INodeEntity, upperBound: Int) =
            throw Exception(exceptionMsg)

        override fun removeChild(rType: String, n: INodeEntity) = throw Exception(exceptionMsg)
        override fun removeChild(rType: String, n: INodeEntity, lowerBound: Int) =
            throw Exception(exceptionMsg)

        override fun removeOutRef(rType: String, end: INodeEntity) = throw Exception(exceptionMsg)
        override fun removeOutRef(rType: String, end: INodeEntity, lowerBound: Int) =
            throw Exception(exceptionMsg)

        override fun loadChildren(
            rType: String,
            endLabel: String,
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