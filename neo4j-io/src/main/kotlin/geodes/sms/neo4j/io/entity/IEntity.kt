package geodes.sms.neo4j.io.entity

import geodes.sms.neo4j.io.EntityState

interface IEntity {
    val _id: Long
    val _uuid: Int
    fun _getState(): EntityState
}

interface INodeEntity : IEntity {
    //val labels: List<String>
    val label: String
}

interface IRelationshipEntity : IEntity {
    val type: String
    val startUUID: Int
    val endUUID: Int
}

interface IPathSegmentEntity : IEntity