package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.EntityState
import geodes.sms.neo4j.io.Mapper
import geodes.sms.neo4j.io.StateListener

class RelationshipController (
    private val mapper: Mapper,
    id: Long,
    override val type: String,
    override val startUUID: Int,
    override val endUUID: Int
    //state: EntityState
): IRelationshipController, StateListener.Creatable {

    companion object {
        private var n = 0
    }

    override var _id: Long = id
        private set

    override val _uuid: Int = n++
    override fun _getState(): EntityState {
        TODO("Not yet implemented")
    }

    override val props: Map<String, Any>
        get() = TODO("Not yet implemented")

    override fun putProperty(name: String, value: Any?) {
        TODO("Not yet implemented")
    }

    override fun remove() {
        TODO("Not yet implemented")
    }

    override fun unload() {
        TODO("Not yet implemented")
    }

    override fun onCreate(id: Long) {
        this._id = id
        //state = StatePersisted()
    }
}