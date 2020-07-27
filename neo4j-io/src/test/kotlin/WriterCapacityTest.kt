import geodes.sms.neo4j.io.BufferedCreator
import geodes.sms.neo4j.io.BufferedRemover
import geodes.sms.neo4j.io.DBReader
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.internal.value.IntegerValue


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WriterCapacityTest {

    private val dbUri = "bolt://localhost:7687"
    private val username = "neo4j"
    private val password = "admin"
    private val driver = GraphDatabase.driver(dbUri, AuthTokens.basic(username, password))

    @Test fun commitNodes() {
        println("Commit nodes fun...")
        val creator = BufferedCreator()

        val size = 1000000
        for (i in 0L until size) {
            creator.createNode("A", mapOf("a" to IntegerValue(i)))
        }

        val session = driver.session()
        creator.commitNodes(session) {
            //it.forEach { println(it) }
        }
        session.close()
    }

    @Test fun commitRelationships() {
        println("Commit relationship fun...")
        val creator = BufferedCreator()

        for (i in 0..1000000L) {
            creator.createRelationship("r", i, i)
        }

        val session = driver.session()
        creator.commitRelationshipsNoIDs(session)
        session.close()
    }

    @Test fun removeRelationships() {
        val remover = BufferedRemover()

        for (i in 0..1000000L) {
            remover.removeRelationship(i, "r", i)
        }

        val session = driver.session()
        remover.commitRelationshipsRemoveByHost(session)
        session.close()
    }

    @Test fun readTest() {
        val reader = DBReader(driver)

        reader.findConnectedNodesWithOutputsCount(1000023, "b", "B") { res ->
            res.forEach {
                println(it.id)
                println(it.outRefCount)
            }
        }
    }

    @AfterAll fun close() {
        driver.close()
        //println("Driver closed")
    }
}
