package geodes.sms.neo4j.io.entity

import geodes.sms.neo4j.io.EntityState

open class NodeEntity(id: Long, override val label: String) : INodeEntity/*, StateListener.Creatable */{

    final override var _id: Long = id
        private set

    override val _uuid = getUUID()
    override fun _getState(): EntityState {
        TODO("Not yet implemented")
    }

//    override fun onCreate(id: Long) {
//        this.id = id
//    }

    private companion object {
        private var n: Int = 0
        fun getUUID() = n--
    }

    override fun equals(other: Any?): Boolean {
        return other is NodeEntity && other._uuid == this._uuid
    }

    override fun hashCode(): Int = _uuid.hashCode()
}
