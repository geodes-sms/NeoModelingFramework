package geodes.sms.nmf.editor.graph

import geodes.sms.nmf.editor.graph.neo4jImpl.ModelManagerImpl
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BasicTest {
    private val dbUri = DBCredentials.dbUri
    private val username = DBCredentials.username
    private val password = DBCredentials.password
    private val manager = ModelManagerImpl(dbUri, username, password)

    @Test fun initGraph() {
        val graph = manager.createGraph()
        val v1 = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex
        val v2 = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex

        v1.addSub_vertices(VertexType.Vertex)
        v1.addSub_vertices(VertexType.Vertex)
        v1.addSub_vertices(VertexType.Vertex)

//        val sv = v2.addSub_vertices()
//        val cv1 = v2.addCompositeVertex()
//        v2.addCompositeVertex()
//        v2.addCompositeVertex()
//
//        val cv4 = cv1.addCompositeVertex()
//        val cv5 = cv1.addCompositeVertex()
//
//        cv4.addCompositeVertex()
//        cv4.addCompositeVertex()
//        cv5.addCompositeVertex().addSubVertex().addEdge(cv4)

        //sv.addEdge(sv)
        //cv4

        manager.saveChanges()
    }

    @Test fun test1() {
        val graph = manager.createGraph()
        val v1 = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex
        val v2 = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex
        val v3 = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex
        manager.saveChanges()

        v1.setEdge(v2)
        v1.setEdge(v2)
        v1.setEdge(v1)
        v3.addSub_vertices(VertexType.Vertex)
        manager.saveChanges()
    }

//    @Test fun rmContainmentTest() {
//        val graph = manager.createGraph()
//        val cv1 = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex
//        val cv2 = graph.addVertices(VertexType.CompositeVertex) as CompositeVertex
//        graph.addVertices(VertexType.CompositeVertex)
//
//        val cv5 = cv1.addCompositeVertex().addCompositeVertex()
//        cv5.addCompositeVertex().addSubVertex()
//        cv5.addSubVertex()
//        cv5.addSubVertex()
//        cv5.addSubVertex()
//
//        val cv6 = cv2.addCompositeVertex().addCompositeVertex()
//        cv6.addCompositeVertex()
//        cv6.addSubVertex()
//        cv6.addSubVertex()
//        manager.saveChanges()
//
//        graph.removeVertex(cv5)
//        graph.removeVertex(cv2)
//        manager.saveChanges()
//    }

//    @Test fun rmChildFromBufferTest() {
//        val graph = manager.createGraph()
//        val cv1 = graph.addCompositeVertex()
//        val cv2 = graph.addCompositeVertex()
//        graph.addCompositeVertex()
//        graph.addCompositeVertex()
//
//        graph.removeVertex(cv1)
//        graph.removeVertex(cv2)
//        manager.saveChanges()
//    }
//
//    @Test fun rmCrossRefTest() {
//        val graph = manager.createGraph()
//        val cv1 = graph.addCompositeVertex()
//        val cv2 = graph.addCompositeVertex().addCompositeVertex()
//
//        val v1 = graph.addVertex()
//        val v2 = graph.addVertex()
//
//        v1.addEdge(v2)
//        v1.addEdge(v2)
//        v1.addEdge(v2)
//        v1.addEdge(v2)
//        v1.addEdge(v2)
//
//        cv2.addEdge(cv1)
//        cv2.addEdge(cv1)
//        manager.saveChanges()
//
//        v1.unsetEdge(v2)
//        v1.unsetEdge(v2)
//        v1.unsetEdge(v2)
//        cv2.unsetEdge(cv1)
//        manager.saveChanges()
//    }
//
//    @Test fun rmCrossRefFromBufferTest() {
//        val graph = manager.createGraph()
//        val v1 = graph.addVertex()
//        val v2 = graph.addVertex()
//
//        v1.addEdge(v2)
//        v1.addEdge(v2)
//        v1.addEdge(v2)
//        v1.addEdge(v2)
//        v1.addEdge(v2)
//
//        v1.unsetEdge(v2)
//        v1.unsetEdge(v2)
//        v1.unsetEdge(v2)
//        manager.saveChanges()
//    }
//
//    @Test fun rmContainmentFromBuffer() {
//        val graph = manager.createGraph()
//        val cv1 = graph.addCompositeVertex()
//        val cv2 = graph.addCompositeVertex()
//                graph.addCompositeVertex()
//
//        cv1.setCapacity(10)
//        val c1 = cv1.addCompositeVertex()
//        cv1.addCompositeVertex()
//        cv1.addCompositeVertex()
//        c1.addSubVertex()
//        c1.addSubVertex()
//        c1.setCapacity(222)
//        cv2.addEdge(cv2)
//        manager.saveChanges()
//
//        graph.removeVertex(cv1)
//        manager.saveChanges()
//
//        //val exceptionMsg = assertThrows<Exception> { c1.addSubVertex() }
//        //Assertions.assertEquals(exceptionMsg, "Node '$c1' was removed. Cannot perform operation on removed node")
//        assertThrows<Exception> { c1.addSubVertex() }
//    }
//
//    @Test fun lowerBoundTest() {
//        val graph = manager.createGraph()
//        val v1 = graph.addVertex()
//        val v2 = graph.addVertex()
//        v1.addEdge(v2)
//        v1.addEdge(v2)
//        v1.addEdge(v2)
//        v1.addEdge(v2)
//        manager.saveChanges()
//
//        v1.unsetEdge(v2)
//        v1.unsetEdge(v2)
//        v1.unsetEdge(v2)
//        val exception = assertThrows<Exception> { v1.unsetEdge(v2) }
//
//        manager.saveChanges()
//        manager.clearCache()
//        Assertions.assertEquals(exception.message,
//            "Upper bound '1' exceeded for relationship 'edge'")
//    }
//
//    @Test fun readTest() {
//        val graph = manager.createGraph()
//        val cv1 = graph.addCompositeVertex()
//        val cv2 = graph.addCompositeVertex()
//                  graph.addCompositeVertex()
//        cv1.setCapacity(88)
//
//        graph.addVertex()
//        graph.addVertex()
//        graph.addVertex()
//        graph.addVertex()
//
//        cv1.addSubVertex()
//        cv1.addSubVertex()
//        cv2.addSubVertex()
//        manager.saveChanges()
//        manager.clearCache()
//
//        val grLoaded = manager.loadGraphByID(graph._id)
//        val res = grLoaded.getCompositeVertices()
//        res.forEach {
//            println(""+ it._id + "  " + it.getCapacity())
//        }
//    }
//
//    @Test fun loadPropertyTest() {
//        val graph = manager.createGraph()
//        val cv = graph.addCompositeVertex()
//        cv.setIsInitial(false)
//        manager.saveChanges()
//        manager.clearCache()
//
//        val cvLoaded = manager.loadCompositeVertexByID(cv._id)
//        Assertions.assertEquals(false, cvLoaded.getIsInitial())
//    }
//
//    @Test fun uniquePropertyTest() {
//        val graph = manager.createGraph()
//        val cv1 = graph.addCompositeVertex()
//        val cv2 = graph.addCompositeVertex()
//        cv1.setId(10)
//
//        manager.saveChanges()
//        assertThrows<Exception> { cv2.setId(10) }
//    }

    @AfterAll
    fun close() {
        manager.close()
    }
}