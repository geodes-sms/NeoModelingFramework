package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.DBAccess
import geodes.sms.neo4j.io.GraphManager
import geodes.sms.neo4j.io.type.AsInt
import geodes.sms.neo4j.io.type.AsList
import geodes.sms.neo4j.io.type.AsString
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PropertyAccessorTest {
    private val manager = GraphManager(DBAccess.dbUri, DBAccess.username, DBAccess.password)

    @AfterEach fun clearCache() {
        manager.clearCache()
    }

    @Test fun intPropertyTest() {
        val node = manager.createNode("Node")
        val property = 77
        node.putProperty("p", property)
        manager.saveChanges()
        node.unload()

        val nodeLoaded = manager.loadNode(node._id, node.label)
        val propLoaded = nodeLoaded.getProperty("p", AsInt)

        Assertions.assertEquals(property, propLoaded)
    }

    @Test fun listPropertyTest() {
        val node = manager.createNode("Node")
        val property = listOf("qq", "ww")
        node.putProperty("list", property)
        manager.saveChanges()
        node.unload()

        val nodeLoaded = manager.loadNode(node._id, node.label)
        val propLoaded = nodeLoaded.getProperty("list", AsList(AsString))

        Assertions.assertEquals(property, propLoaded)
    }

    @AfterAll
    fun close() {
        manager.close()
    }
}