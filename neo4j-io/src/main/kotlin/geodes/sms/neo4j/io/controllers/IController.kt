package geodes.sms.neo4j.io.controllers

import org.neo4j.driver.Value

interface IController {
    //val id: Long
    //val state: State    //private set
    val props: Map<String, Value>   //immutable here
    fun putProperty()

    //fun remove()  // call stateRemoved instead -- this state recursively call state.remove on ctms
}

interface IRelationshipController : IController, IRelationshipEntity {
    //val startNode: INodeEntity
    //val endNode: INodeEntity
}

interface INodeController : IController, INodeEntity {
        //val label: String
    //fun addChild()    //outRef + new endNode
    //fun addOutRef(type:String, endNode: INodeController)
    //fun getOutRefs(type: String): List<INodeController>

    //fun removeOutRef()    -- getRefs then  ref.remove()
}
