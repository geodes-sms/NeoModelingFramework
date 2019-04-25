package geodes.sms.neo4jecore

import org.neo4j.driver.v1.Session
import org.neo4j.driver.v1.Values


open class Neo4jEObjectImpl(override val dbSession: Session, override val id: Int, override val label: String) : Neo4jEObject {

    override fun remove(): Boolean {
        return try {
            dbSession.writeTransaction {it.run("MATCH (n:$label) WHERE ID(n)={nodeID}" +
                    " OPTIONAL MATCH (n)-[* {containment:true}]->(m)" +
                    " DETACH DELETE n, m",
                Values.parameters("nodeID", this.id))}
            true
        } catch (e : Exception) {
            e.printStackTrace()
            false
        }
    }
}