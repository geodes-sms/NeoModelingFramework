package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.controllers.IRelationshipController
import kotlin.collections.HashMap
import kotlin.collections.HashSet


internal class EMFGraph<N: INodeController, R: IRelationshipController> {
    /** uuid --> IController */
    private val nodes = hashMapOf<Int, N>()
    private val refs = hashMapOf<Int, R>()

    /** node innerID -> outRefType -> list refs */
    private val adjOutputList = hashMapOf<Int, HashMap<String, HashSet<R>>>()

    /** node uuid  ->  input ref*/
    private val adjInput = hashMapOf<Int, HashMap<String, HashSet<R>>>()
    //private val adjOutput = hashMapOf<Int, LinkedList<Int>>()

    fun putNode(node: N) {
        nodes[node._uuid] = node
    }

    fun putRelationship(ref: R): Boolean {
        val outputs = adjOutputList[ref.startUUID]
        val inputs = adjInput[ref.endUUID]

        return if (outputs != null && inputs != null) {
            val outputList = outputs.getOrPut(ref.type) { hashSetOf() }
            val inputList = inputs.getOrPut(ref.type) { hashSetOf() }
            outputList.add(ref)
            inputList.add(ref)
            refs[ref._uuid] = ref
            true
        } else false
    }

    fun putEndPathSegment() {
        // -->O
    }

    fun getNode(uuid: Int): N? = nodes[uuid]
    fun getRelationship(uuid: Int): R? = refs[uuid]

    fun removeNode(uuid: Int): Pair<N?, List<R>> {
        //remove mapping
        val outputs = adjOutputList.getOrElse(uuid) { hashMapOf() }
        val inputs = adjInput.getOrElse(uuid) { hashMapOf() }

        //val outRefs = outputs.values.flatten()
        //val inputRef = inputs.values.flatten()
        val refs = outputs.values.flatten() + inputs.values.flatten()

        outputs.clear()
        inputs.clear()

        return nodes.remove(uuid) to refs
    }

    fun removeRelationship(uuid: Int): R? {
        val ref = refs.remove(uuid)
        return if (ref != null) {  //remove mapping
            val outputs = adjOutputList.getOrElse(ref.startUUID) { hashMapOf() }[ref.type]
            val inputs = adjInput.getOrElse(ref.endUUID) { hashMapOf() }[ref.type]
            outputs?.remove(ref)
            inputs?.remove(ref)
            ref
        } else null
    }

    //get connecting ref
    fun getRelationship(startUUID: Int, rType: String, endUUID: Int): R? {
        val outputs = adjOutputList.getOrElse(startUUID) { hashMapOf() }[rType]
        //val inputs = adjInput.getOrElse(endUUID) { hashMapOf() }[rType]

        return outputs?.find {it.endUUID == endUUID }
    } //+ fun rmRelationship(... same params)


    fun getConnectedOutputNodes(nodeUUID: Int, rType: String): List<N> {
        val outputs = adjOutputList.getOrElse(nodeUUID) { hashMapOf() } [rType]

        return outputs?.map {
            nodes[it.endUUID]!!
        } ?: emptyList()
        TODO()

    } //+ fun rmConnectedNodes(...) == getSubGraphNode ??


//    fun clear() {
//        //foreach -> detach
//    }
}