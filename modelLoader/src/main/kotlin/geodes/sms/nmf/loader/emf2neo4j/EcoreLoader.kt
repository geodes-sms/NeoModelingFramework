package geodes.sms.nmf.loader.emf2neo4j

import geodes.sms.nmf.neo4j.Values
import geodes.sms.nmf.neo4j.io.IGraph
import geodes.sms.nmf.neo4j.io.INode
import org.eclipse.emf.ecore.*


class EcoreLoader(private val writer: IGraph) {

    private val map = hashMapOf<EObject, INode>()

    fun load(ePackage: EPackage) {
        val ePackNode = writer.createNode("EPackage", getProps(ePackage))

        ePackage.eClassifiers.forEach { eObj ->
            val node = connect(ePackNode, eObj, "eClassifiers")
            when (eObj) {
                is EClass -> {
                    eObj.eAttributes.forEach { load(node, it) }
                    eObj.eReferences.forEach { load(node, it) }
                    eObj.eSuperTypes.forEach { connect(node, it, "eSuperTypes") }
                }
                is EEnum -> {
                    eObj.eLiterals.forEach { connect(node, it, "eLiterals") }
                }
            }
        }

        ePackage.eAllContents().asSequence().filterIsInstance<EPackage>().forEach {
            connect(startNode = map.getOrDefault(it.eSuperPackage, ePackNode),
                endEObj = it,
                refType = "eSubpackages")
        }

        writer.save()
        map.clear()
    }

    private fun getProps(eObj: EObject) = eObj.eClass().eAllAttributes
        .asSequence()
        //.filter { eObj.eIsSet(it) }
        .associateBy({ it.name }, { Values.value(eObj.eGet(it, true)) })

    private fun load(eClassNode: INode, eAttr: EAttribute) {
        val attrNode = writer.createPath(eClassNode, "eAttributes", "EAttribute", getProps(eAttr))
        attrNode.setProperty("eType", eAttr.eType.name)
        if (eAttr.eType.eContainer() == eAttr.eContainingClass.eContainer()) {
            connect(attrNode, eAttr.eType, "eAttributeType")
        }
    }

    private fun load(eClassNode: INode, eRef: EReference) {
        val refNode = writer.createPath(eClassNode, "eReferences", "EReference", getProps(eRef))
        connect(refNode, eRef.eType, "eReferenceType")
    }

    private fun connect(startNode: INode, endEObj: EObject, refType: String): INode {
        val endNode = map[endEObj]
        return if (endNode != null) {
            writer.createRelation(start = startNode, refType = refType, end = endNode)
            endNode
        } else {
            val node = writer.createPath(
                start = startNode,
                refType = refType,
                endLabel = endEObj.eClass().name,
                props = getProps(endEObj)
            )
            map[endEObj] = node
            node
        }
    }
}