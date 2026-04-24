package geodes.sms.nmf.loader.emf2neo4j

import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import geodes.sms.nmf.neo4j.io.Entity
import geodes.sms.nmf.neo4j.io.Values
import org.eclipse.emf.common.util.AbstractTreeIterator
import org.eclipse.emf.common.util.TreeIterator
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.neo4j.driver.Value
import java.util.IdentityHashMap

class ReflectiveBatchLoader(
    private val writer: GraphBatchWriter,
    // IdentityHashMap: EObject equality is always identity, skip equals()/hashCode() overhead
    private val nodes: MutableMap<EObject, Entity> = IdentityHashMap()
) {
    // Cache eAllAttributes and eAllReferences per EClass — never recompute per instance
    private val attrCache = HashMap<EClass, List<EAttribute>>()
    private val refCache  = HashMap<EClass, List<EReference>>()

    private fun attrsOf(eClass: EClass) =
        attrCache.getOrPut(eClass) { eClass.eAllAttributes }

    private fun refsOf(eClass: EClass) =
        refCache.getOrPut(eClass) { eClass.eAllReferences }

    private fun getIterator(eObj: EObject): TreeIterator<EObject> =
        object : AbstractTreeIterator<EObject>(eObj, true) {
            override fun getChildren(obj: Any?): Iterator<EObject> =
                (obj as EObject).eContents().iterator()
        }

    fun load(rootEObj: EObject): Pair<Int, Int> {
        val nodeCount = loadNodes(rootEObj)
        val refCount  = loadRefs()
        return nodeCount to refCount
    }

    private fun getProps(eObj: EObject): HashMap<String, Value> {
        val attrs = attrsOf(eObj.eClass())
        // Preallocate to exact attribute count — avoids resize copies
        val res = HashMap<String, Value>(attrs.size * 2)
        for (eAttr in attrs) {
            val value = Values.value(eObj.eGet(eAttr, true))
            if (!value.isNull) res[eAttr.name] = value
        }
        return res
    }

    fun loadNodes(rootEObj: EObject): Int {
        val nodeStep = 25_000
        var index     = 0
        var nodeCount = 0

        for (eObject in getIterator(rootEObj)) {
            nodes[eObject] = writer.createNode(eObject.eClass().name, getProps(eObject))
            if (++index == nodeStep) {
                nodeCount += writer.commitNodes()
                index = 0
            }
        }
        if (index > 0) nodeCount += writer.commitNodes()
        return nodeCount
    }

    fun loadRefs(): Int {
        val refStep = 20_000
        var index    = 0
        var refCount = 0

        fun maybeCommit() {
            if (++index == refStep) {
                refCount += writer.commitRefs()
                index = 0
            }
        }

        for ((eObject, nodeEntity) in nodes) {
            val refs = refsOf(eObject.eClass())
            for (eRef in refs) {
                if (!eObject.eIsSet(eRef)) continue
                when (val value = eObject.eGet(eRef, true)) {
                    is List<*> -> for (item in value) {
                        // Skip unresolved cross-refs instead of writing phantom Entity()
                        val end = nodes[item as EObject] ?: continue
                        writer.createRef(eRef.name,nodeEntity,  end, eRef.isContainment)
                        maybeCommit()
                    }
                    is EObject -> {
                        val end = nodes[value] ?: continue
                        writer.createRef(eRef.name,nodeEntity,  end, eRef.isContainment)
                        maybeCommit()
                    }
                    else -> println("cannot parse EReference ${eRef.name} on ${eObject.eClass().name}")
                }
            }
        }

        nodes.clear()
        if (index > 0) refCount += writer.commitRefs()
        return refCount
    }
}