package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.GraphManager
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PropertyAccessorTest {
    private val dbUri = "bolt://localhost:7687"
    private val username = "neo4j"
    private val password = "admin"
    private val manager = GraphManager(dbUri, username, password)

    @AfterEach fun clearCache() {
        manager.clearCache()
    }

    @Test fun listPropertyTest() {
        val node = manager.createNode("Node")
        val property = listOf("qq", "ww")
        node.putProperty("list", property)
        manager.saveChanges()
        node.unload()

        val nodeLoaded = manager.loadNode(node._id, node.label)
        val propLoaded = nodeLoaded.getPropertyAsListOf<String>("list")

        Assertions.assertEquals(property, propLoaded)
    }

    @AfterAll
    fun close() {
        manager.close()
    }
}