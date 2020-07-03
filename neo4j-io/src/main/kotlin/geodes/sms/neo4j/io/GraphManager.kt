package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.entity.RefBounds
import org.neo4j.driver.*

/** Graph proxy */
class GraphManager(dbUri: String, username: String, password: String) : AutoCloseable {
    private val driver = GraphDatabase.driver(dbUri, AuthTokens.basic(username, password))

    private val creator = BufferedCreator()
    private val updater = BufferedUpdater(driver)
    private val remover = BufferedRemover()
    private val reader = DBReader(driver)
    private val mapper = Mapper(creator, updater, remover, reader)

    fun saveChanges() {
        val session = driver.session()
        mapper.saveChanges(session)
        session.close()
    }

    fun clearChanges() {}

    fun clearCache() { mapper.clearCache() }

    fun clearDB() {
        val session = driver.session()
        remover.removeAll(session)
        session.close()
    }

    fun createNode(label: String, outRefBounds: Map<String, RefBounds> = emptyMap()): INodeController {
        return mapper.createNode(label, outRefBounds)
    }

    fun loadNode(id: Long, label: String, outRefBounds: Map<String, RefBounds> = emptyMap()): INodeController {
        return mapper.loadNodeNode(id, label, outRefBounds)
    }

    //also unloads all out/in refs
    fun unload(node: geodes.sms.neo4j.io.entity.INodeEntity) {
        if (node is INodeController) node.unload()
        else throw Exception("object $node must be instance of INodeController")
    }

    override fun close() {
        mapper.clearCache()
        driver.close()
    }
}