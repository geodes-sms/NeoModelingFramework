package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.EntityState
import geodes.sms.neo4j.io.entity.INodeEntity
import geodes.sms.neo4j.io.entity.IPropertyAccessor
import geodes.sms.neo4j.io.entity.IRelationshipEntity

interface IController : IPropertyAccessor {
    fun remove()
    fun unload()
    fun getState(): EntityState
}

interface IRelationshipController : IController, IRelationshipEntity

interface INodeController : IController, INodeEntity {
    val outRefCount: Map<String, Int>

    fun createChild(rType: String, label: String): INodeController
    fun createChild(rType: String, label: String, upperBound: Int): INodeController

    fun createOutRef(rType: String, endNode: INodeEntity)//: IRelationshipController
    fun createOutRef(rType: String, endNode: INodeEntity, upperBound: Int)
    //fun createInputRef(rType: String, startNode: INodeEntity): IRelationshipController

    fun removeChild(rType: String, childNode: INodeEntity)
    fun removeChild(rType: String, childNode: INodeEntity, lowerBound: Int)

    fun removeOutRef(rType: String, endNode: INodeEntity)
    fun removeOutRef(rType: String, endNode: INodeEntity, lowerBound: Int)
    //fun removeInputRef(rType: String, startNode: INodeEntity)

    fun loadOutConnectedNodes(
        rType: String,
        endLabel: String?,
        limit: Int = 100,
        filter: String = ""
    ): List<INodeController>

    fun <R> loadOutConnectedNodes(
        rType: String,
        endLabel: String?,
        limit: Int,
        filter: String,
        mapFunction: (INodeController) -> R
    ): List<R>
}