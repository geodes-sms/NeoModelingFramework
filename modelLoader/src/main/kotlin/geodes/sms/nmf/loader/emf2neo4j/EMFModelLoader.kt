package geodes.sms.nmf.loader.emf2neo4j

import geodes.sms.nmf.neo4j.io.Entity
import geodes.sms.nmf.neo4j.io.GraphBatchWriter
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.emf.ecore.util.EcoreUtil
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.StringValue
import java.io.File


interface EmfModelLoader {
    companion object {
        fun load(modelPath: String, writer: GraphBatchWriter): Pair<Int, Int> {
            Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.apply {
                put("ecore", EcoreResourceFactoryImpl())
                put("*", XMIResourceFactoryImpl())
            }

            val resourceSet = ResourceSetImpl()
            val resource: Resource = resourceSet.getResource(
                URI.createFileURI(File(modelPath).absolutePath),
                true
            )

            // important: resolve all references in the resource (IDs, proxies)
            EcoreUtil.resolveAll(resource)

            val root = resource.contents[0]

            return when (root) {
                is EPackage -> EcoreLoader(writer).load(root)   // load metamodel itself
                is EObject -> {
                    // traverse all top-level elements and their references
                    traverseAndLoad(root, writer)
                }
                else -> throw Exception("Unknown element type $root in the model $modelPath. SKIPPING the model")
            }
        }

        private fun traverseAndLoad(root: EObject, writer: GraphBatchWriter): Pair<Int, Int> {
            val visited = mutableMapOf<EObject, Entity>() // Map EObject -> Neo4j node
            var totalNodes = 0
            var totalEdges = 0

            fun traverse(obj: EObject) {
                if (visited.containsKey(obj)) return

                // Create node for this EObject
                val label = obj.eClass().name
                val props = mutableMapOf<String, Value>()
                // Copy attributes as props
                obj.eClass().eAllAttributes.forEach { attr ->
                    val value = obj.eGet(attr)
                    if (value != null) props[attr.name] = StringValue(value.toString())
                }

                val entity = writer.createNode(label, props)
                visited[obj] = entity
                totalNodes++

                // First traverse contained elements to create their nodes
                obj.eContents().forEach { traverse(it) }

                // Now create edges for all EReferences
                obj.eClass().eAllReferences.forEach { ref ->
                    val value = obj.eGet(ref)
                    when (value) {
                        is EObject -> {
                            traverse(value) // ensure target node exists
                            writer.createRef(ref.name, entity, visited[value]!!, ref.isContainment)
                            totalEdges++
                        }
                        is Collection<*> -> {
                            value.filterIsInstance<EObject>().forEach { target ->
                                traverse(target)
                                writer.createRef(ref.name, entity, visited[target]!!, ref.isContainment)
                                totalEdges++
                            }
                        }
                    }
                }
            }

            traverse(root)

            // Commit everything
            writer.commitNodes()
            writer.commitRefs()

            return totalNodes to totalEdges
        }
    }
}