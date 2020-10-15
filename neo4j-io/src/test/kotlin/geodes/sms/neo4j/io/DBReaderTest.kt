package geodes.sms.neo4j.io

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase

internal class DBReaderTest {
    private val reader = DBReader(GraphDatabase.driver(DBAccess.dbUri, AuthTokens.basic(DBAccess.username, DBAccess.password)))
    @Test fun findConnectedNodesWithOutputsCount() {

    }
}