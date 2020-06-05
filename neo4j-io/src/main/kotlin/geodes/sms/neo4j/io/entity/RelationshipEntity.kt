package geodes.sms.neo4j.io.entity

class RelationshipEntity(id: Long) : IRelationshipEntity {
    override var _id: Long = id
        private set

    override fun equals(other: Any?): Boolean {
        return other is RelationshipEntity && other._id == this._id
    }

    override fun hashCode(): Int = _id.hashCode()
}