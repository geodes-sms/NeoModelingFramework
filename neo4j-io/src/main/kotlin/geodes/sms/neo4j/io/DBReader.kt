package geodes.sms.neo4j.io

import org.neo4j.driver.Driver

class DBReader(private val driver: Driver) {

    fun findNodeByID(id: Long) {
        driver.session().readTransaction {

        }
    }


}