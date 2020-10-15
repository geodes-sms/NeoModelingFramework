package geodes.sms.neo4j.io.entity

interface IRelationshipEntity : IEntity {
    val type: String
    val startNode: INodeEntity
    val endNode: INodeEntity
//    val startUUID: Int
//    val endUUID: Int
}