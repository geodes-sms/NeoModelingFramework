package geodes.sms.domain.graph

import geodes.sms.domain.graph.neo4jImpl.ModelManagerImpl
import org.junit.jupiter.api.*


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BasicTest {
    private val dbUri = "bolt://localhost:7687"
    private val username = "neo4j"
    private val password = "admin"
    private val manager = ModelManagerImpl(dbUri, username, password)

    @Test fun initGraph() {
        val graph = manager.createGraph()
        val v1 = graph.addCompositeVertex()
        val v2 = graph.addCompositeVertex()

        v1.addSubVertex()
        v1.addSubVertex()
        v1.addSubVertex()

        val sv = v2.addSubVertex()
        val cv1 = v2.addCompositeVertex()
        v2.addCompositeVertex()
        v2.addCompositeVertex()

        val cv4 = cv1.addCompositeVertex()
        val cv5 = cv1.addCompositeVertex()

        cv4.addCompositeVertex()
        cv4.addCompositeVertex()
        cv5.addCompositeVertex().addSubVertex().addEdge(cv4)

        sv.addEdge(sv)
        sv.addEdge(sv)
        //sv.addEdge(sv)
        //cv4

        manager.saveChanges()
    }

    @Test fun rmContainmentTest() {
        val graph = manager.createGraph()
        val cv1 = graph.addCompositeVertex()
        val cv2 = graph.addCompositeVertex()
        val cv3 = graph.addCompositeVertex()

        val cv5 = cv1.addCompositeVertex().addCompositeVertex()
        cv5.addCompositeVertex().addSubVertex()
        cv5.addSubVertex()
        cv5.addSubVertex()
        cv5.addSubVertex()

        val cv6 = cv2.addCompositeVertex().addCompositeVertex()
        cv6.addCompositeVertex()
        cv6.addSubVertex()
        cv6.addSubVertex()
        manager.saveChanges()

        graph.removeVertex(cv5)
        graph.removeVertex(cv2)
        manager.saveChanges()
    }

    @Test fun rmChildFromBufferTest() {
        val graph = manager.createGraph()
        val cv1 = graph.addCompositeVertex()
        val cv2 = graph.addCompositeVertex()
        val cv3 = graph.addCompositeVertex()
        val cv4 = graph.addCompositeVertex()

        graph.removeVertex(cv1)
        graph.removeVertex(cv2)
        manager.saveChanges()
    }

    @Test fun rmCrossRefTest() {
        val graph = manager.createGraph()
        val cv1 = graph.addCompositeVertex()
        val cv2 = graph.addCompositeVertex().addCompositeVertex()

        val v1 = graph.addVertex()
        val v2 = graph.addVertex()

        v1.addEdge(v2)
        v1.addEdge(v2)
        v1.addEdge(v2)
        v1.addEdge(v2)
        v1.addEdge(v2)

        cv2.addEdge(cv1)
        cv2.addEdge(cv1)
        manager.saveChanges()

        v1.removeEdge(v2)
        v1.removeEdge(v2)
        v1.removeEdge(v2)
        cv2.removeEdge(cv1)
        manager.saveChanges()
    }

    @Test fun rmCrossRefFromBufferTest() {
        val graph = manager.createGraph()
        val v1 = graph.addVertex()
        val v2 = graph.addVertex()

        v1.addEdge(v2)
        v1.addEdge(v2)
        v1.addEdge(v2)
        v1.addEdge(v2)
        v1.addEdge(v2)

        v1.removeEdge(v2)
        v1.removeEdge(v2)
        v1.removeEdge(v2)
        manager.saveChanges()
    }

    @Test fun rmContainmentFromBuffer() {
        val graph = manager.createGraph()
        val cv1 = graph.addCompositeVertex()
        val cv2 = graph.addCompositeVertex()
                graph.addCompositeVertex()

        cv1.setCapacity(10)
        val c1 = cv1.addCompositeVertex()
        val c2 = cv1.addCompositeVertex()
        val c3 = cv1.addCompositeVertex()
        c1.addSubVertex()
        c1.addSubVertex()
        c1.setCapacity(222)
        cv2.addEdge(cv2)
        manager.saveChanges()

        graph.removeVertex(cv1)
        manager.saveChanges()

        //val exceptionMsg = assertThrows<Exception> { c1.addSubVertex() }
        //Assertions.assertEquals(exceptionMsg, "Node '$c1' was removed. Cannot perform operation on removed node")
        assertThrows<Exception> { c1.addSubVertex() }
    }

    @Test fun lowerBoundTest() {
        val graph = manager.createGraph()
        val v1 = graph.addVertex()
        val v2 = graph.addVertex()
        v1.addEdge(v2)
        v1.addEdge(v2)
        v1.addEdge(v2)
        v1.addEdge(v2)
        manager.saveChanges()

        v1.removeEdge(v2)
        v1.removeEdge(v2)
        v1.removeEdge(v2)
        val exception = assertThrows<Exception> { v1.removeEdge(v2) }

        manager.saveChanges()
        manager.clearCache()
        Assertions.assertEquals(exception.message,
            "Upper bound '1' exceeded for relationship 'edge'")
    }

    @Test fun readTest() {
        val graph = manager.createGraph()
        val cv1 = graph.addCompositeVertex()
        val cv2 = graph.addCompositeVertex()
                  graph.addCompositeVertex()
        cv1.setCapacity(88)

        graph.addVertex()
        graph.addVertex()
        graph.addVertex()
        graph.addVertex()

        cv1.addSubVertex()
        cv1.addSubVertex()
        cv2.addSubVertex()
        manager.saveChanges()
        manager.clearCache()

        val grLoaded = manager.loadGraphByID(graph._id)
        val res = grLoaded.getCompositeVertices()
        res.forEach {
            println(""+ it._id + "  " + it.getCapacity())
        }
    }

    @Test fun uniquePropertyTest() {
        val graph = manager.createGraph()
        val cv1 = graph.addCompositeVertex()
        val cv2 = graph.addCompositeVertex()
        cv1.setId(10)

        manager.saveChanges()
        cv2.setId(10)
    }

    @AfterAll
    fun close() {
        manager.close()
    }
}