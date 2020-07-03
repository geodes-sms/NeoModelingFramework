//package geodes.sms.neo4j.io
//
//import geodes.sms.neo4j.io.controllers.INodeController
//import geodes.sms.neo4j.io.controllers.IRelationshipController
//import kotlin.collections.HashMap
//import kotlin.collections.HashSet
//
//
//internal class EMFGraph<N: INodeController, R: IRelationshipController> {
//    /** uuid --> IController */
//    private val nodes = hashMapOf<Int, N>()
//    private val refs = hashMapOf<Int, R>()  //R data is enough to restore RC from adj
//
//    /** node uuid -> outRefType -> list refs */
//    private val adjOutput = hashMapOf<Int, HashMap<String, HashSet<R>>>()
//
//    /** node uuid  -> inputRefType -> input ref*/
//    private val adjInput = hashMapOf<Int, HashMap<String, HashSet<R>>>()
//
//
//    fun putNode(node: N) { nodes[node._uuid] = node }
//
//    fun putRelationship(rel: R): Boolean {
//        val outputs = adjOutput[rel.startUUID]
//        val inputs = adjInput[rel.endUUID]
//        return if (outputs != null && inputs != null) {
//            outputs.getOrPut(rel.type) { hashSetOf() }.add(rel)
//            inputs.getOrPut(rel.type) { hashSetOf() }.add(rel)
//            refs[rel._uuid] = rel
//            true
//        } else false
//    }
//
//    fun putEndPathSegment() {
//        // -->O
//    }
//
//    fun getNode(uuid: Int): N? = nodes[uuid]
//    fun getRelationship(uuid: Int): R? = refs[uuid]
//
//    inline fun removeNode(nodeUUID: Int, onNodeRemove: (N) -> Unit/*, onRelRemove: (R) -> Unit*/): N? {
//        val node = nodes.remove(nodeUUID)
//        if (node != null) {
//            //remove mapping
//            adjOutput.remove(nodeUUID)//?.asSequence()?.map {it.value}?.flatten()?.forEach(onRelRemove)
//            adjInput.remove(nodeUUID)//?.asSequence()?.map {it.value }?.flatten()?.forEach(onRelRemove)
//            onNodeRemove(node)
//        }
//        return node
//    }
//
//    fun removeRelationship(uuid: Int): R? {
//        val ref = refs.remove(uuid)
//        if (ref != null) {
//            //remove mapping
//            val outputs = adjOutput.getOrElse(ref.startUUID) { hashMapOf() }[ref.type]
//            val inputs = adjInput.getOrElse(ref.endUUID) { hashMapOf() }[ref.type]
//            outputs?.remove(ref)
//            inputs?.remove(ref)
//        }
//        return ref
//    }
//
////    fun rmRelationship(startUUID: Int, rType: String, endUUID: Int): R? {
////        TODO()
////    }
//
//    //get connecting ref
//    fun getRelationship(startUUID: Int, rType: String, endUUID: Int): R? {
//        val outputs = adjOutput.getOrElse(startUUID) { hashMapOf() } [rType]
//        //val inputs = adjInput.getOrElse(endUUID) { hashMapOf() } [rType]
//        return outputs?.find { it.endUUID == endUUID }
//    }
//
//    fun getConnectedNodesByOutRel(nodeUUID: Int, rType: String): Sequence<N> {
//        val outputs = adjOutput.getOrElse(nodeUUID) { hashMapOf() } [rType]
//
//        return outputs?.asSequence()?.map { nodes[it.endUUID] }?.filterNotNull() ?: emptySequence()
//    } //+ fun rmConnectedNodes(...) == getSubGraphNode ??
//
//
//    fun clear() {
//        nodes.clear()
//        refs.clear()
////        adjInput.forEach { (_, v) -> //v.forEach { (_, vv) -> vv.clear() }
////            v.clear() }
////        adjOutput.forEach { (_, v) -> v.clear() }
//        adjInput.clear()
//        adjOutput.clear()
//    }
//}