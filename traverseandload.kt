OLD
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

new
private fun traverseAndLoad(root: EObject, writer: GraphBatchWriter): Pair<Int, Int> {
            val visited = IdentityHashMap<EObject, Entity>(1024)
            val refCache = HashMap<EClass, List<EReference>>()
            val attrCache = HashMap<EClass, List<EAttribute>>()
            var totalEdges = 0

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