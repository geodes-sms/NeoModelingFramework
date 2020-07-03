package geodes.sms.neo4j.io.entity

interface IEntity {
    val _id: Long
    //val _uuid: Int
}

interface INodeEntity : IEntity {
    //val labels: List<String>
    //val label: String
}

interface IRelationshipEntity : IEntity {
    val type: String
    val startNode: INodeEntity
    val endNode: INodeEntity
//    val startUUID: Int
//    val endUUID: Int
}
