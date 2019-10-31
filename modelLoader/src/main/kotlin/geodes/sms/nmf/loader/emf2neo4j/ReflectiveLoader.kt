package geodes.sms.nmf.loader.emf2neo4j

import geodes.sms.nmf.neo4j.io.IGraph
import geodes.sms.nmf.neo4j.io.INode
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.TreeIterator
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter


class ReflectiveLoader(
    private val graph: IGraph,
    private val adapter: ECrossReferenceAdapter) : EmfModelLoader() {

    override fun load(iterator: TreeIterator<EObject>) {

        while(iterator.hasNext()) {
            val eObject = iterator.next()
            val eClass = eObject.eClass()

            val node = cache.getOrPut(eObject) {
                graph.createNode(eClass.name)
            }

            //set Properties
            eClass.eAllAttributes.filter { eObject.eIsSet(it) }.forEach { eAttr ->
                node.setProperty(eAttr.name, eObject.eGet(eAttr, true))
            }

            //set References
            eClass.eAllReferences.filter { eObject.eIsSet(it) }.forEach { eRef ->
                when (val value = eObject.eGet(eRef, true)) {
                    is EList<*> -> value.asSequence().filterIsInstance<EObject>().forEach {
                        writeReference(node, eRef, it)
                    }
                    is EObject -> writeReference(node, eRef, value)
                    else -> throw Exception("cannot parse EReference ${eRef.name}")
                }
            }

            //if (eClass.name == "A") c++
            //if (c == 2) iterator.prune()
        }
    }

    private fun writeReference(startNode: INode, eRef: EReference, pointedValue: EObject) {
        val endNode = cache[pointedValue]
        if (endNode != null) {
            graph.createRelation(refType = eRef.name,
                start = startNode,
                end = endNode,
                containment = eRef.isContainment)
        } else {    //not visited endNode
            cache[pointedValue] = graph.createPath(start = startNode,
                endLabel = eRef.eReferenceType.name,
                refType = eRef.name,
                containment = eRef.isContainment
            )
        }
    }
}

