package geodes.sms.domain.graph

import geodes.sms.domain.graph.neo4jImpl.ModelManagerImpl
import org.junit.jupiter.api.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiComplexModelTest {
    private val dbUri = "bolt://localhost:7687"
    private val username = "neo4j"
    private val password = "admin"
    private val manager = ModelManagerImpl(dbUri, username, password)

    @BeforeEach
    fun clearDB() {
        manager.clearDB()
    }

    // ApiTestGenerator(1, 2, 1, 2, 3, 0).gen()
    @RepeatedTest(3)
    @Test fun test1() {
        val g1 = manager.createGraph()
        val v1 = g1.addVertex()
        v1.addEdge(v1)
        v1.setId(1)

        val cv2 = g1.addCompositeVertex()
        cv2.setId(2)
        cv2.setDefaultVertex(v1)

        val cv3 = g1.addCompositeVertex()
        cv3.setId(3)
        cv3.setDefaultVertex(cv2)

        manager.saveChanges()
        manager.clearCache()
    }

    @RepeatedTest(4)
    @Test fun test2() {
        val g1 = manager.createGraph()
        for (i in 1..30)
            g1.addVertex()

        manager.saveChanges()
        manager.clearCache()
    }

    @AfterAll
    fun close() {
        manager.clearDB()
        manager.close()
    }
}