package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.EntityState
import geodes.sms.neo4j.io.entity.INodeEntity
import geodes.sms.neo4j.io.entity.IRelationshipEntity
import geodes.sms.neo4j.io.entity.RefBounds

interface IController {
    val props: Map<String, Any>   //immutable here
    fun putProperty(name: String, value: Any?)
    fun putUniqueProperty(name: String, value: Any?)
    fun remove()
    fun unload()
    fun getState(): EntityState
}

interface IRelationshipController : IController, IRelationshipEntity

interface INodeController : IController, INodeEntity {
    //addLabel(l: String); //removeLabel(l: String)
    val label: String
    fun createChild(rType: String, label: String, childRefBounds: Map<String, RefBounds> = emptyMap()): INodeController
    fun createOutRef(rType: String, endNode: INodeEntity)//: IRelationshipController
    //fun createInputRef(rType: String, startNode: INodeEntity): IRelationshipController

    fun removeChild(rType: String, childNode: INodeEntity)
    fun removeOutRef(rType: String, endNode: INodeEntity)
    //fun removeInputRef(rType: String, startNode: INodeEntity)

    //fun getChildrenFromCache(rType: String): Sequence<INodeController>
    fun loadChildren(
        rType: String,
        endLabel: String,
        outRefBounds: Map<String, RefBounds> = emptyMap(),
        filter: String = "",
        limit: Int = 100
    ): List<INodeController>
}
