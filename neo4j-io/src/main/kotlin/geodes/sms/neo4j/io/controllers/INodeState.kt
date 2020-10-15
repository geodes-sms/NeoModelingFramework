package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.EntityState
import geodes.sms.neo4j.io.entity.INodeEntity
import geodes.sms.neo4j.io.entity.IPropertyAccessor

interface INodeState : IPropertyAccessor {
    fun remove()
    fun unload()

    fun createChild(label: String, rType: String): INodeController
    fun createChild(label: String, rType: String, upperBound: Int): INodeController

    fun createOutRef(rType: String, end: INodeEntity)
    fun createOutRef(rType: String, end: INodeEntity, upperBound: Int)

    fun removeChild(rType: String, n: INodeEntity)
    fun removeChild(rType: String, n: INodeEntity, loverBound: Int)

    fun removeOutRef(rType: String, end: INodeEntity)
    fun removeOutRef(rType: String, end: INodeEntity, loverBound: Int)

    //fun removeInputRef(rType: String, startNode: INodeEntity)
    //fun removeInputRef(rType: String, startID: Long)
    //fun _createInputRef(rType: String, start: INodeController)//: IRelationshipController
    //fun _createInputRef(rType: String, start: Long): IRelationshipController
    //fun _createOutputRef(rType: String, end: INodeEntity): IRelationshipController
    //fun _createOutputRef(rType: String, end: Long): IRelationshipController

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

    //fun getChildrenFromCache(rType: String): Sequence<INodeController>
    fun getState(): EntityState
}