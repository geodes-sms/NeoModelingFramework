package geodes.sms.nmf.loader.emf2neo4j

import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import geodes.sms.nmf.neo4j.io.Entity
import geodes.sms.nmf.neo4j.io.Values
import org.eclipse.emf.common.util.AbstractTreeIterator
import org.eclipse.emf.common.util.TreeIterator
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.neo4j.driver.Value


class ReflectiveBatchLoader(private val writer: GraphBatchWriter) {

    private val nodes = hashMapOf<EObject, Entity>()
    private fun getIterator(eObj: EObject): TreeIterator<EObject> {
        return object : AbstractTreeIterator<EObject>(eObj, true) {
            override fun getChildren(obj: Any?): Iterator<EObject> {
                return (obj as EObject).eContents().iterator()
            }
        }
    }

    fun load(rootEObj: EObject): Pair<Int, Int> {
        val nodeCount = loadNodes(rootEObj)
        val refCount = loadRefs()
        return (nodeCount to refCount)
    }

    private fun getProps(eObj: EObject): HashMap<String, Value> {
        val res = hashMapOf<String, Value>()
        for (eAttr in eObj.eClass().eAllAttributes) {
            val value = Values.value(eObj.eGet(eAttr, true))
            if (!value.isNull) res[eAttr.name] = value
        }
        return res
    }

    private fun loadNodes(rootEObj: EObject): Int {
        val nodeStep = 25000
        var index = 0
        var nodeCount = 0

        for (eObject in getIterator(rootEObj)) {
            nodes[eObject] = writer.createNode(eObject.eClass().name, getProps(eObject))
            if (++index == nodeStep) {
                nodeCount += writer.saveNodes()
                index = 0
            }
        }
        if (index > 0) nodeCount += writer.saveNodes()
        return nodeCount
    }

    private fun loadRefs(): Int {
        val refStep = 10000
        var index = 0
        var refCount = 0

        fun save() {
            if (++index == refStep) {
                refCount += writer.saveRefs()
                index = 0
            }
        }

        for ((eObject, nodeEntity) in nodes) {
            eObject.eClass().eAllReferences
                .asSequence()
                .filter { eObject.eIsSet(it) }
                .forEach { eRef ->
                    when (val value = eObject.eGet(eRef, true)) {
                        is List<*> -> value.forEach {
                            writer.createRef(
                                start = nodeEntity,
                                rType = eRef.name,
                                end = nodes.getOrDefault(it as EObject, Entity()),
                                isContainment = eRef.isContainment
                            )
                            save()
                        }
                        is EObject -> {
                            writer.createRef(
                                start = nodeEntity,
                                rType = eRef.name,
                                end = nodes.getOrDefault(value, Entity()),
                                isContainment = eRef.isContainment
                            )
                            save()
                        }
                        else -> println("cannot parse EReference ${eRef.name}")
                    }
                }
        }
        nodes.clear()
        if (index > 0) refCount += writer.saveRefs()
        return refCount
    }
}