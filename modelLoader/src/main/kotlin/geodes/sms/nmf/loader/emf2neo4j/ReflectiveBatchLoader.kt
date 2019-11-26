package geodes.sms.nmf.loader.emf2neo4j

import geodes.sms.nmf.neo4j.Values
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import geodes.sms.nmf.neo4j.io.IDHolder
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource


class ReflectiveBatchLoader(private val resource: Resource, private val writer: GraphBatchWriter) {

    private val nodes = hashMapOf<EObject, IDHolder>()

    fun load(): Pair<Int, Int> {
        val nodeCount = loadNodes()
        val refCount = loadRefs()

        return (nodeCount to refCount)
    }

    private fun loadNodes(): Int {
        val nodeStep = 20000
        var cursor = 0
        var nodeCount = 0

        for (eObject in resource.allContents) {
            val eClass = eObject.eClass()

            val props = eClass.eAllAttributes
                .asSequence()
                .filter { eObject.eIsSet(it) }
                .associateBy ({ it.name }, { Values.value(eObject.eGet(it, true)) })

            nodes[eObject] = writer.createNode(label = eClass.name, props = props)

            if (++cursor == nodeStep) {
                nodeCount += writer.saveNodes()
                cursor = 0
            }
        }
        if (cursor > 0) nodeCount += writer.saveNodes()
        return nodeCount
    }

    private fun loadRefs(): Int {
        val refStep = 10000
        var cursor = 0
        var refCount = 0

        fun save() {
            if (++cursor == refStep) {
                refCount += writer.saveRefs()
                cursor = 0
            }
        }

        nodes.forEach { (eObject, idHolder) ->
            eObject.eClass().eAllReferences
                .asSequence()
                .filter { eObject.eIsSet(it) }
                .forEach { eRef ->
                    when (val value = eObject.eGet(eRef, true)) {
                        is List<*> -> value.forEach {
                            writer.createRef(
                                startID = idHolder.id,
                                type = eRef.name,
                                endID = nodes.getOrDefault(it as EObject, IDHolder()).id
                            )
                            save()
                        }
                        is EObject -> {
                            writer.createRef(
                                startID = idHolder.id,
                                type = eRef.name,
                                endID = nodes.getOrDefault(value, IDHolder()).id
                            )
                            save()
                        }
                        else -> throw Exception("cannot parse EReference ${eRef.name}")
                    }
                }
        }
        nodes.clear()
        if (cursor > 0) refCount += writer.saveRefs()
        return refCount
    }
}