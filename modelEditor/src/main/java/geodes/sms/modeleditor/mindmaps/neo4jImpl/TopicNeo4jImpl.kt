package  geodes.sms.modeleditor.mindmaps.neo4jImpl

import geodes.sms.modeleditor.mindmaps.*
import geodes.sms.neo4jecore.Neo4jEObject
import geodes.sms.neo4jecore.Neo4jEObjectImpl
import org.neo4j.driver.v1.Session
import org.neo4j.driver.v1.Values

class TopicNeo4jImpl(override val dbSession: Session, override val id: Int, override val label: String) :Topic

, Neo4jEObject by Neo4jEObjectImpl(dbSession, id, label) {

        override fun setName(attrValue: String) : Boolean {
            return  try {
                dbSession.writeTransaction {it.run("MATCH (c:$label) WHERE ID(c)={nodeID}  SET c.name = {value}",
                Values.parameters("nodeID", this.id, "value", attrValue))}
                true
            } catch (e : Exception) {
                e.printStackTrace()
                false
            }
        }

        override fun getName() : String? {
            //return String
            return  try {
                val res = dbSession.readTransaction {it.run("MATCH (c:$label) WHERE ID(c) = {nodeID} RETURN c.name AS p",
                    Values.parameters("nodeID", this.id))}
                res.single()["p"].asString()
            } catch (e : Exception) {
                e.printStackTrace()
                null
            }
        }
        
                override fun addMarker(endNode: Marker) : Boolean {
                    return try {
                        val res = dbSession.writeTransaction { it.run("MATCH (c:$label) WHERE ID(c) = {startNodeID}" +
                            " MATCH (e:${endNode.label}) WHERE ID(e) = {endNodeID}" +
	  						" OPTIONAL MATCH (c)-[r:marker {containment:true}]->(:${endNode.label})" +
                            " OPTIONAL MATCH (e)<-[{containment: true}]-(endNodeContainer)" +
                            " WITH c, e, COUNT(r) < 5 AND COUNT(endNodeContainer) = 0 AS predicate" +
                            " FOREACH (ignoreMe IN CASE WHEN predicate THEN [1] ELSE [] END | " +
                            "  CREATE (c)-[r:marker{containment:{containment}}]->(e))" +
                            " RETURN predicate AS isAdded",
                            Values.parameters("startNodeID", this.id, "endNodeID", endNode.id, "containment", true))}
                        res.single()["isAdded"].asBoolean()
                    } catch (e : Exception) {
                        e.printStackTrace()
                        false
                    }
                }
                
}