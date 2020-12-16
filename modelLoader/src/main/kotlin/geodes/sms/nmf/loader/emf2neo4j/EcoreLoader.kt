package geodes.sms.nmf.loader.emf2neo4j

import geodes.sms.nmf.neo4j.io.DeferredRelationship
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import geodes.sms.nmf.neo4j.io.Entity
import geodes.sms.nmf.neo4j.io.Values
import org.eclipse.emf.common.util.AbstractTreeIterator
import org.eclipse.emf.common.util.TreeIterator
import org.eclipse.emf.ecore.*
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.StringValue
import java.util.*
import kotlin.collections.HashMap

class EcoreLoader(private val writer: GraphBatchWriter) {
    private val nodes = hashMapOf<EObject, Entity>()
    private val deferredRefs = hashMapOf<Entity, List<DeferredRelationship>>()

    private companion object {
        const val eLITERAL_REF = "eLiterals"
        const val eCLASSIFIER_REF = "eClassifiers"
        const val eSUBPACKAGE_REF = "eSubpackages"
        const val eSUPERTYPE_REF = "eSuperTypes"
        const val eREFERENCE_REF = "eReferences"
        const val eATTRIBUTE_REF = "eAttributes"
    }

    private fun getIterator(eObj: EObject): TreeIterator<EObject> {
        return object : AbstractTreeIterator<EObject>(eObj, true) {
            override fun getChildren(obj: Any?): MutableIterator<EObject> {
                return (obj as EObject).eContents().iterator()
            }
        }
    }

    fun load(ePackage: EPackage): Pair<Int, Int> {
        for (eObject in getIterator(ePackage)) {
            when (eObject) {
                is EClass -> load(eObject)
                is EReference -> load(eObject)
                is EAttribute -> load(eObject)
                is EEnumLiteral -> loadContainment(eLITERAL_REF, "EEnumLiteral", eObject)
                is EEnum -> loadContainment(eCLASSIFIER_REF, "EEnum", eObject)
                is EPackage -> loadContainment(eSUBPACKAGE_REF, "EPackage", eObject)
//                is EGenericType -> {}
//                else -> println("Skipping the element $eObject")
            }
        }

        //load deferred refs
        for ((start, refList) in deferredRefs) {
            for (ref in refList) {
                val end = nodes[ref.end]
                if (end != null)
                    writer.createRef(ref.rType, start, end, false)
            }
        }

        nodes.clear()
        return writer.commitNodes() to writer.commitRefs()
    }

    private fun loadContainment(
        containingRefName: String,
        metaClassName: String,
        eObject: EObject
    ): Entity {
        val node = writer.createNode(metaClassName, getProps(eObject))
        nodes[eObject] = node

        val parent = nodes[eObject.eContainer()]
        if (parent != null)
            writer.createRef(containingRefName, parent, node, true)
        return node
    }

    private fun load(eClass: EClass) {
        val node = loadContainment(eCLASSIFIER_REF, "EClass", eClass)
        val list = eClass.eSuperTypes.map { DeferredRelationship(eSUPERTYPE_REF, it) }
        if (list.isNotEmpty())
            deferredRefs[node] = list
    }

    private fun load(eAttr: EAttribute) {
        val attrNode = writer.createNode("EAttribute",
            getProps(eAttr).apply { put("eType", StringValue(eAttr.eType.name)) }
        )
        nodes[eAttr.eContainingClass]?.let { writer.createRef(eATTRIBUTE_REF, it, attrNode, true) }
        nodes[eAttr] = attrNode

        //check if DataType is defined in the same EPackage
        if (eAttr.eType.eContainer() == eAttr.eContainingClass.eContainer()) {
            deferredRefs[attrNode] = listOf(DeferredRelationship("eAttributeType", eAttr.eAttributeType))
        }
    }

    private fun load(eRef: EReference) {
        val node = loadContainment(eREFERENCE_REF, "EReference", eRef)
        deferredRefs[node] = LinkedList<DeferredRelationship>().apply {
            add(DeferredRelationship("eReferenceType", eRef.eReferenceType))
            eRef.eOpposite?.let { add(DeferredRelationship("eOpposite", it)) }
        }
    }

    private fun getProps(eObj: EObject): HashMap<String, Value> {
        val res = hashMapOf<String, Value>()
        for (eAttr in eObj.eClass().eAllAttributes) {
            val value = Values.value(eObj.eGet(eAttr, true))
            if (!value.isNull) res[eAttr.name] = value
        }
        return res
    }
}