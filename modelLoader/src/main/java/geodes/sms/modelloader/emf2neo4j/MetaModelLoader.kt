package geodes.sms.modelloader.emf2neo4j

import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.*
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter


class MetaModelLoader(
    private val resource: Resource,
    private val adapter: ECrossReferenceAdapter) : EmfModelLoader {

    /** lists must not contain containment refs */
    private fun allowedOutputRefs(eObject: EModelElement) : List<String> {
        return when (eObject) {
            is EClass     -> listOf("eSuperTypes")
            is EReference -> listOf("eType", "eKeys", "eOpposite")
            is EAttribute -> {
                if (resource.getURIFragment(eObject.eType) != "/-1") listOf("eType") else emptyList()
                /**
                 * If attribute's eType is not defined in current .ecore file, uri of this eType is /-1
                 * EString, EInt... are custom types defined in MOF model, so they will be stored only as props of EAttribute node
                 */
            }
            else -> emptyList()
        }
    }

    private fun allowedInputRefs(eObject: EModelElement) : List<String> {
        return when (eObject) {
            is EClass     -> listOf("eClassifiers", "eType", "eSuperTypes")
            is EReference -> listOf("eStructuralFeatures", "eOpposite")
            is EAttribute -> listOf("eStructuralFeatures", "eKeys")
            is EPackage   -> listOf("eSubpackages")
            is EDataType  -> listOf("eClassifiers", "eType")    //EDataType includes EEnum
            is EEnumLiteral -> listOf("eLiterals")
            else -> emptyList()
        }
    }

    override fun load(dbWriter: Neo4jBufferedWriter) {

        resource.allContents.asSequence().filterIsInstance<EModelElement>().forEach { eObject ->
            val eClass = eObject.eClass()

            //val label = eClass.eAllSuperTypes.joinToString("", prefix = eClass.name, transform = {":${it.name}"})
            val label = eClass.name
            val nodeUri = resource.getURIFragment(eObject)
            val props = eClass.eAllAttributes.associateBy ({ it.name }, { eObject.eGet(it) })
                .minus(listOf(/*"defaultValue",*/ "instanceClass", "instance"))
                .plus(listOfNotNull(if (eObject is EAttribute) "eType" to eObject.eAttributeType.instanceClassName else null))

            /**
             * Output crossRefs without children containment
             * List<Pair<eRef -> endNodeUri>>
             */
            val outputCrossRefs = eClass.eAllReferences
                .filter { eObject.eIsSet(it) && it.name in allowedOutputRefs(eObject) }
                .flatMap { eRef ->
                    val value = eObject.eGet(eRef)
                    when (value) {
                        is EList<*> -> value.filterIsInstance<EObject>().map { eRef to resource.getURIFragment(it) }
                        is EObject -> listOf(Pair(eRef, resource.getURIFragment(value)))
                        else ->  throw Exception("cannot parse EReference ${eRef.name} eType to $value")
                    }
                }

            /** Input refs including containment */
            val inputRefs = adapter.getInverseReferences(eObject, true)
                .filter { it.eObject is ENamedElement && it.eStructuralFeature.name in allowedInputRefs(eObject) }

            //println("${eClass.name}  $nodeUri  ${outputCrossRefs.size + inputRefs.size}")
            //props.forEach { (k, v) -> println(" $k  $v   ${v?.javaClass?.name}") }
            //println("  outputCrossRefs: ${outputCrossRefs.size}  inputRefs: ${inputRefs.size}  eContents: ${eObject.eContents().size}")
            val usageCount = outputCrossRefs.size + eObject.eContents().filterIsInstance<EModelElement>().size + inputRefs.size

            dbWriter.writeNode(nodeUri, label, props, usageCount)
            inputRefs.forEach {
                val startNode = it.eObject
                val eRef = it.eStructuralFeature as EReference

                //write ref from startNode to this node
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
        }
        //dbWriter.close()
    }
}