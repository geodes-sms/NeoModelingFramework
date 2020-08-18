package geodes.sms.nmf.editor.graph.apitest

import geodes.sms.nmf.editor.graph.neo4jImpl.ModelManagerImpl
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Vertex1000 {
    private val dbUri = "bolt://localhost:7687"
    private val username = "neo4j"
    private val password = "admin"
    private val manager = ModelManagerImpl(dbUri, username, password)

    @BeforeEach
    fun beforeEach() {
        manager.clearCache()
        manager.clearDB()
    }

    @Test fun test0() {
        val g = manager.createGraph()
        g.setName("TEST 0")
        manager.saveChanges()
    }

    @AfterAll
    fun close() {
        //manager.clearDB()
        manager.close()
    }
}