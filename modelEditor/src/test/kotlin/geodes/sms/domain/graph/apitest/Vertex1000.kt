package geodes.sms.domain.graph.apitest

import geodes.sms.domain.graph.neo4jImpl.ModelManagerImpl
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
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

    @AfterAll
    fun close() {
        //manager.clearDB()
        manager.close()
    }
}