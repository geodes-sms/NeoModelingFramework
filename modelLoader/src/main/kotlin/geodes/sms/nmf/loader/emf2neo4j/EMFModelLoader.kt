package geodes.sms.nmf.loader.emf2neo4j

import geodes.sms.nmf.neo4j.io.Entity
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EReference
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.emf.ecore.util.EcoreUtil
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.StringValue
import java.io.File
import java.util.IdentityHashMap


interface EmfModelLoader {


    companion object {
        fun load(modelPath: String, writer: GraphBatchWriter): Pair<Int, Int> {
            Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.apply {
                put("ecore", EcoreResourceFactoryImpl())
                put("*", XMIResourceFactoryImpl())
            }

            val resource = ResourceSetImpl()
                .getResource(URI.createFileURI(File(modelPath).absolutePath), true)

            // Separate EPackage roots from plain EObject roots
            val ePackageRoots = resource.contents.filterIsInstance<EPackage>()
            val eObjectRoots  = resource.contents.filterIsInstance<EObject>()
                .filterNot { it is EPackage }

            var totalNodes = 0
            var totalRefs  = 0

            // EPackage roots are self-contained process independently as before
            for (pkg in ePackageRoots) {
                val (n, r) = EcoreLoader(writer).load(pkg)
                totalNodes += n
                totalRefs  += r
            }

            // EObject roots share a single nodes map so cross-root refs resolve
            if (eObjectRoots.isNotEmpty()) {
                val sharedNodes = hashMapOf<EObject, Entity>()
                val loader = ReflectiveBatchLoader(writer, sharedNodes)

                // Phase 1: all nodes first
                for (root in eObjectRoots) {
                    totalNodes += loader.loadNodes(root)
                }

                // Phase 2: all refs (cross-root refs now resolve correctly)
                totalRefs += loader.loadRefs()
            }

            return totalNodes to totalRefs
        }
    }

}

