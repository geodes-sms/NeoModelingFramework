package geodes.sms.neo4j.io.controllers

interface IEntity {
    val id: Long
}

interface INodeEntity : IEntity
interface IRelationshipEntity : IEntity
