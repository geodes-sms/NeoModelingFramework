package geodes.sms.nmf.emfextension.codelabs

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EContentsEList
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import java.io.File


fun main(args: Array<String>) {
    val modelPath = File(args[0]).absolutePath
    println("Processing model: $modelPath")

    Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.apply {
        put("geodes.sms.geodes.sms.domain.ecore", EcoreResourceFactoryImpl())
        put("xmi", XMIResourceFactoryImpl())
    }

    val resourceSet = ResourceSetImpl()
    val resource = resourceSet.getResource(URI.createFileURI(modelPath), true)

    val nodeCount = getNodeCount(resource)
    val (containmentRefCount, crossRefCount) = getRefCount(
        resource
    )

    println("Node count: $nodeCount")
    println("Total refCount: ${containmentRefCount + crossRefCount}")
    println("  ContainmentRef count: $containmentRefCount")
    println("  CrossRef count:  $crossRefCount")
}


fun getNodeCount(resource: Resource): Int{
    var nodeCount = 0
    for (eObj in resource.allContents)
        nodeCount++
    return nodeCount
}

fun getRefCount(resource: Resource) : Pair<Int, Int> {
    var contentsRef = 0
    var crossRef = 0

    for (eObj in resource.allContents) {
        val crossIterator = eObj.eCrossReferences().iterator() as EContentsEList.FeatureIterator
        for (r in crossIterator)
            crossRef++

        val contentIterator = eObj.eContents().iterator() as EContentsEList.FeatureIterator
        for (r in contentIterator)
            contentsRef++
    }
    return contentsRef to crossRef
}