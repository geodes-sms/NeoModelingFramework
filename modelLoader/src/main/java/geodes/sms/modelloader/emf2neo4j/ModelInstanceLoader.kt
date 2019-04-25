package geodes.sms.modelloader.emf2neo4j

import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter
import kotlin.Exception


class ModelInstanceLoader(
    private val resource: Resource,
    private val adapter: ECrossReferenceAdapter) : EmfModelLoader {

    override fun load(dbWriter: Neo4jBufferedWriter) {
        val iterator = resource.allContents
        while(iterator.hasNext()) {
            val eObject = iterator.next()
            val eClass = eObject.eClass()

            //val label = eClass.eAllSuperTypes.joinToString("", prefix = eClass.name, transform = {":${it.name}"})
            val label = eClass.name
            val nodeUri = resource.getURIFragment(eObject)
            val props = eClass.eAllAttributes.filter { eObject.eIsSet(it) }.associateBy ({ it.name }, { eObject.eGet(it) })

            /**
             * Output crossRefs without containment
             * List<Pair<eRef -> endNodeUri>>
             */
            val outputCrossRefs = eClass.eAllReferences
                .filter { eObject.eIsSet(it) && !it.isContainment }
                .flatMap { eRef ->
                    val value = eObject.eGet(eRef)
                    when (value) {
                        is EList<*> -> value.filterIsInstance<EObject>().map { eRef to resource.getURIFragment(it) }
                        is EObject -> listOf(Pair(eRef, resource.getURIFragment(value)))
                        else -> throw Exception("cannot parse EReference ${eRef.name}")
                    }
                }

            /** Input refs including containment */
            val inputRefs = adapter.getInverseReferences(eObject, true)

            println("${eClass.name}  $nodeUri  ${outputCrossRefs.size + inputRefs.size}")
            props.forEach { (k, v) -> println(" $k  $v   ${v?.javaClass?.name}") }

            /** Write node with attributes */
            dbWriter.writeNode(nodeUri, label, props, outputCrossRefs.size + eObject.eContents().size + inputRefs.size)

            inputRefs.forEach {
                val startNode = it.eObject
                val eRef = it.eStructuralFeature as EReference

                /** Write ref from startNode to this node */
                dbWriter.writeOutputRef(
                    startNodeUri = resource.getURIFragment(startNode),
                    endNodeUri = nodeUri,
                    refName = eRef.name,
                    isContainment = eRef.isContainment
                )
                //println("  in:  " + eRef.name + " <-- " + resource.getURIFragment(startNode))
            }

            outputCrossRefs.forEach { (eRef, endNodeUri) ->
                dbWriter.writeOutputRef(
                    startNodeUri = nodeUri,
                    endNodeUri = endNodeUri,
                    refName = eRef.name,
                    isContainment = eRef.isContainment
                )
                //println("  out: " + eRef.name + " --> " + endNodeUri)
            }

            //if (eClass.name == "A") c++
            //if (c == 2) iterator.prune()
        }
    }
}

