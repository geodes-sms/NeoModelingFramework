package geodes.sms.modelloader.emf2neo4j

import org.neo4j.driver.v1.Values
import org.neo4j.driver.v1.Driver
import kotlin.properties.Delegates


class Neo4jBufferedWriter(private val driver: Driver) : AutoCloseable {

    // todo : add rootNode to buffer in constructor. This node must have metaModel package id (or at least name)
    // 1 Neo4jBufferedWriter per 1 model (per 1 graph)

    /** Must be >= 2 to be able contain at least CREATE and MATCH queries (without return) */
    private val bufferCapacity = 150
    private var aliasPostfix : Long = 0
    private var bufferPosition : Int by Delegates.observable(0) { _, _, newValue ->
        if (newValue == bufferCapacity)
            flush()
    }

    private val queryBuffer = sortedSetOf<Query>() //compareBy {it.queryOrder}

    /** String xmlUri -> Node node */
    private val nodeMetadata = hashMapOf<String, Node>()

    /** String alias -> String xmlUri */
    private val aliasBuffer = hashMapOf<String, String>()  //size = bufferCapacity

    /** String propsAlias -> Map<String, Any> props of current emf node*/
    private val properties = hashMapOf<String, Any>()


    fun writeNode(nodeUri: String, label: String, props: Map<String, Any>, usageCount: Int) {

        val node = BufferedNode(alias = "n_$aliasPostfix", usageCount = usageCount)
        nodeMetadata[nodeUri] = node
        aliasBuffer[node.alias] = nodeUri
        queryBuffer.add(node.create(propsAlias = "props_$aliasPostfix", label = label))
        properties["props_${aliasPostfix++}"] = props

        bufferPosition += 1 //call flush in observer
    }

    fun writeOutputRef(startNodeUri: String, endNodeUri: String, refName: String, isContainment: Boolean) {
        val startNode = nodeMetadata[startNodeUri]
        startNode?.let {
            //find endNode and pass to second let block
            nodeMetadata[endNodeUri]
        }?.let { endNode ->
            queryBuffer.addAll(startNode.createOutputRef(endNode, refName, isContainment))
            if (--startNode.usageCount == 0) nodeMetadata.remove(startNodeUri)
            if (--endNode.usageCount   == 0) nodeMetadata.remove(endNodeUri)
            bufferPosition += 1
        }
    }

    private fun flush() {
        //append return statement to get IDs associated with alias
        queryBuffer.add(QueryReturn(aliasBuffer.keys))

        /*
        println()
        println("all query:")
        println(queryBuffer.joinToString( separator = "\n"))*/
        //println("nodeMetadata size: ${nodeMetadata.size}")
        //nodeMetadata.forEach { (uri, node) -> println(node.alias + " " + node.usageCount) }

        driver.session().writeTransaction {
            val res = it.run(queryBuffer.joinToString( separator = "\n"), properties)
            val map = res.single().get("nodeIDs").asMap(Values.ofLong()) //String alias -> Long id
            map.forEach { (alias, id) ->
                val xmlUri = aliasBuffer.getOrDefault(alias, "")
                nodeMetadata.computeIfPresent(xmlUri) { _, oldNode: Node ->
                    Neo4jNode(alias, id, oldNode.usageCount)
                }
                //nodeMetadata[uri] = Neo4jNode(alias, id)
            }
        }
        bufferPosition = 0
        queryBuffer.clear()
        aliasBuffer.clear()
        properties.clear()
    }

    override fun close() {
        flush()
    }
}