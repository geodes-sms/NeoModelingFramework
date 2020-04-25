package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.Mapper
import org.neo4j.driver.Value

class NodeController(/*private val mapper: Mapper,*/ id: Long) : INodeController, NodeEntity(id) {

    //private val outRefs = hashMapOf<String, List<IRelationshipController>>()     //crossRef
    //private val paths = hashMapOf<String, List<INodeController>>()

    override val props = hashMapOf<String, Value>() // mutable here
    override fun putProperty() {
        TODO("Not yet implemented")
    }


    // ---------- Create block ----------
    /*
    fun createContainment(rType: String) : INodeController {}*/

    fun createChild(rType: String) : INodeController {
        TODO()
    }

    fun createOutRef(rType: String, nc: INodeController) {

    }

    // ---------- Read block ----------
    /*fun getContainments(rType: String): List<INodeController> {

    }*/

    // get refController
    fun getOutRefs(rType: String): List<IRelationshipController> {
        TODO()
    }

    // get endNode controller; sync cache
    fun getChildren(rType: String): List<INodeController> {
        TODO()
    }

    fun getChildrenFromCache() {

    }

    // ---------- Remove block ----------
    /*
    fun removeContainment(nc: INodeController)/*: Boolean*/ {

    }*/

    fun removeChild(rType: String = ":containment", nc: INodeController)/*: Boolean*/ {

    }

    fun removeOutRef(rType: String, endNc: INodeController) {

    }
}