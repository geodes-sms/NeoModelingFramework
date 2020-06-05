package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.controllers.IRelationshipController
import geodes.sms.neo4j.io.entity.INodeEntity
import geodes.sms.neo4j.io.entity.IRelationshipEntity
import org.neo4j.driver.*

/** Graph proxy */
class GraphManager(dbUri: String, username: String, password: String) : AutoCloseable {
    private val driver = GraphDatabase.driver(dbUri, AuthTokens.basic(username, password))

    private val creator = BufferedCreator()
    private val updater = BufferedUpdater()
    private val remover = BufferedRemover()
    private val reader = DBReader(driver)

    private val mapper = Mapper(creator, updater, remover, reader)
    //val cache = Graph()

    fun saveChanges() {
        val session = driver.session()
        mapper.saveChanges(session)
        session.close()
    }

    fun clearChanges() {}

    fun clearCache() {}

    fun clearDB() {
        driver.session().writeTransaction { tx ->
            //tx.run()
        }
    }

    fun createNode(label: String) : INodeController {
        return mapper.createNode(label)
    }

    fun createRelationship(rType: String, start: INodeEntity, end: INodeEntity): IRelationshipController {
        TODO()
    }

//    fun createPathSegment() {
//
//    }

    fun loadNode(id: Long, label: String) : INodeController {
       return mapper.loadNode(id, label)
    }

    //also unloads all out/in refs
    fun unload(nc: INodeEntity) {
        TODO()
    }

    fun remove(nc: INodeEntity) {

    }

    fun remove(nc: IRelationshipEntity) {

    }

    override fun close() {
        //cache.clearCache()
        driver.close()
    }


//    // EntityControllerFactory
//    inner class EntityFactory {
//        private var n = 0   // innerID creator
//        fun createNode(label: String /*, normalMap*/) : INodeController {
//            val props = hashMapOf<String, Value>()   //create from kotlin map
//            val innerID = creator.createNode(label, props)
//            val nc = NodeController.createForNewNode(entityFactory,, innerID, props)
//            //val nc = NodeController.createForNewNode(entityFactory, innerID, props)
//            mapper.createNode(innerID, nc)
//            return nc
//        }
//
//        fun createRelationship() : IRelationshipController {
//            TODO()
//        }
//
//        fun createChildNode() : INodeController {
//            TODO()
//        }
//    }
//
//    inner class EntityLoader {
//        fun loadFromDB(id: Long) {
//            reader.findNodeByID(id)
//        }
//    }
//
//    inner class EntityUpdater {
//        fun updateNode(innerID: Int) {}
//        fun updateRelationship(innerID: Int) {}
//    }
//
//    inner class EntityRemover {
//        //fun removeNodeFromCache(innerID: Int) {}    // = detach !!
//        fun removeNodeFromDB(id: Long) {}
//    }
}