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
            val visited = mutableMapOf<EObject, Entity>()
            var totalEdges = 0

            // Pass 1: create all nodes using a stack (avoid recursion entirely)
            val stack = ArrayDeque<EObject>()
            stack.addLast(root)

            while (stack.isNotEmpty()) {
                val obj = stack.removeLast()
                if (visited.containsKey(obj)) continue

                val label = obj.eClass().name
                val props = mutableMapOf<String, Value>()
                obj.eClass().eAllAttributes.forEach { attr ->
                    val value = obj.eGet(attr)
                    if (value != null) props[attr.name] = StringValue(value.toString())
                }

                visited[obj] = writer.createNode(label, props)

                // push all reachable objects onto the stack
                obj.eClass().eAllReferences.forEach { ref ->
                    when (val value = obj.eGet(ref)) {
                        is EObject -> if (!visited.containsKey(value)) stack.addLast(value)
                        is Collection<*> -> value.filterIsInstance<EObject>()
                            .filter { !visited.containsKey(it) }
                            .forEach { stack.addLast(it) }
                    }
                }
            }

            writer.commitNodes()

            // Pass 2: create all edges, no recursion needed
            visited.keys.forEach { obj ->
                val entity = visited[obj]!!
                obj.eClass().eAllReferences.forEach { ref ->
                    when (val value = obj.eGet(ref)) {
                        is EObject -> {
                            visited[value]?.let { target ->
                                writer.createRef(ref.name, entity, target, ref.isContainment)
                                totalEdges++
                            }
                        }
                        is Collection<*> -> value.filterIsInstance<EObject>().forEach { target ->
                            visited[target]?.let {
                                writer.createRef(ref.name, entity, it, ref.isContainment)
                                totalEdges++
                            }
                        }
                    }
                }
            }

            writer.commitRefs()

            return visited.size to totalEdges
        }
    }
}