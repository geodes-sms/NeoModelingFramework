package geodes.sms.nmf.loader.emf2neo4j

import geodes.sms.nmf.neo4j.Values
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import geodes.sms.nmf.neo4j.io.IDHolder
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource


class ReflectiveBatchLoader(private val resource: Resource, private val writer: GraphBatchWriter) {

    private val nodes = hashMapOf<EObject, IDHolder>()

    fun load() {
        val nodeCount = loadNodes()
        val crossRefCount = loadRefs()

        println("nodes loaded: $nodeCount")
        println("crossRef loaded: $crossRefCount")
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

            val alias = "n${nodeCount++}"
            val idHolder = writer.createNode(label = eClass.name, alias = alias, props = props)
            nodes[eObject] = idHolder

            if (++cursor == nodeStep) {
                writer.saveNodes()
                cursor = 0
            }
        }
        if (cursor > 0) writer.saveNodes()
        return nodeCount
    }

    private fun loadRefs(): Int {
        val refStep = 10000
        var cursor = 0
        var refCount = 0

        fun save() {
            refCount++
            if (++cursor == refStep) {
                writer.saveRefs()
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
                                containment = eRef.isContainment,
                                endID = nodes.getOrDefault(it as EObject, IDHolder()).id
                            )
                            save()
                        }
                        is EObject -> {
                            writer.createRef(
                                startID = idHolder.id,
                                type = eRef.name,
                                containment = eRef.isContainment,
                                endID = nodes.getOrDefault(value, IDHolder()).id
                            )
                            save()
                        }
                        else -> throw Exception("cannot parse EReference ${eRef.name}")
                    }
                }
        }
        if (cursor > 0) writer.saveRefs()
        return refCount
    }
}