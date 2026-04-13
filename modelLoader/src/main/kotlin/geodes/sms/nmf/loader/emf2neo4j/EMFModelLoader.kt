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
            val visited = IdentityHashMap<EObject, Entity>(1024)
            val refCache = HashMap<EClass, List<EReference>>()
            val attrCache = HashMap<EClass, List<EAttribute>>()
            var totalEdges = 0

            // create all nodes using a stack (avoid recursion entirely)
            val stack = ArrayDeque<EObject>(1024)
            stack.addLast(root)

            while (stack.isNotEmpty()) {
                val obj = stack.removeLast()
                if (visited.containsKey(obj)) continue

                val eClass = obj.eClass()
                val attrs = attrCache.getOrPut(eClass) { eClass.eAllAttributes.toList() }
                val refs = refCache.getOrPut(eClass) { eClass.eAllReferences.toList() }

                val props = mutableMapOf<String, Value>()
                attrs.forEach { attr ->
                    val value = obj.eGet(attr)
                    if (value != null) props[attr.name] = StringValue(value.toString())
                }
                val label = obj.eClass().name
                visited[obj] = writer.createNode(label, props)

                refs.forEach { ref ->
                    when (val value = obj.eGet(ref)) {
                        is EObject -> if (!visited.containsKey(value)) stack.addLast(value)
                        is Collection<*> -> value.forEach { target ->
                            if (target is EObject && !visited.containsKey(target)) stack.addLast(target)
                        }
                    }
                }
            }

            writer.commitNodes()

            // create all edges
            visited.entries.forEach { (obj, entity) ->
                val refs = refCache.getOrPut(obj.eClass()) { obj.eClass().eAllReferences.toList() }
                refs.forEach { ref ->
                    when (val value = obj.eGet(ref)) {
                        is EObject -> visited[value]?.let {
                            writer.createRef(ref.name, entity, it, ref.isContainment)
                            totalEdges++
                        }
                        is Collection<*> -> value.forEach { target ->
                            if (target is EObject) visited[target]?.let {
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

