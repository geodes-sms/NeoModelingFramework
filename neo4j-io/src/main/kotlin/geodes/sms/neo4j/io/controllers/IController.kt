package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.entity.INodeEntity
import geodes.sms.neo4j.io.entity.IRelationshipEntity

interface IController {
    val props: Map<String, Any>   //immutable here
    fun putProperty(name: String, value: Any?)

    fun remove()    // call stateRemoved instead -- this state recursively call state.remove on ctms
    fun unload()
}

interface IRelationshipController : IController, IRelationshipEntity

interface INodeController : IController, INodeEntity {
    //addLabel(l: String); //removeLabel(l: String)

    fun createChild(label: String, rType: String): INodeController
    fun createOutRef(rType: String, endNode: INodeEntity): IRelationshipController
    //fun createInputRef(rType: String, endNode: INodeController): IRelationshipController

    fun removeChild(rType: String, node: INodeEntity)   //extra for EMF
    fun removeOutRef(rType: String, node: INodeEntity)  //extra... //getOutRefs then  ref.remove()
    //fun removeInputRef(n: INodeEntity)

    fun getChildrenFromCache(rType: String): List<INodeController>
    fun loadChildren(
        rType: String, endLabel: String, limit: Int = 100, filter: String = ""
    ): List<INodeController>

    //fun getChildrenByOutRef(rType: String /*endLabel: String*/): List<INodeController>
    //fun getOutRefs(type: String): List<IRelationshipController>
}
