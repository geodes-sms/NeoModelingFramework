package geodes.sms.neo4j.io.entity

class RelationshipEntity(
    override val _id: Long,
    override val type: String,
    override val startNode: INodeEntity,
    override val endNode: INodeEntity
) : IRelationshipEntity