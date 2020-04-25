package geodes.sms.neo4j.io

import org.neo4j.driver.*


class DBContext(dbUri: String, username: String, password: String) : AutoCloseable {
    private val driver = GraphDatabase.driver(dbUri, AuthTokens.basic(username, password))

    val creator = BufferedCreator()
    val updater = BufferedUpdater()
    val remover = BufferedRemover()
    val reader = DBReader(driver)
    val mapper = Mapper(creator, updater, remover, reader)

    fun saveChanges() {
        val session = driver.session()
        mapper.saveChanges(session)
        session.close()
    }

    fun clearChanges() {}

    fun clearDB() {
        driver.session().writeTransaction { tx ->
            //tx.run()
        }
    }

    override fun close() {
        driver.close()
    }
}