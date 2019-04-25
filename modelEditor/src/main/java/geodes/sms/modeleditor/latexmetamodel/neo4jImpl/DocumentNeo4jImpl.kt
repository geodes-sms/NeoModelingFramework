package  geodes.sms.modeleditor.latexmetamodel.neo4jImpl

import geodes.sms.modeleditor.latexmetamodel.*
import geodes.sms.neo4jecore.Neo4jEObject
import geodes.sms.neo4jecore.Neo4jEObjectImpl
import org.neo4j.driver.v1.Session
import org.neo4j.driver.v1.Values

class DocumentNeo4jImpl(override val dbSession: Session, override val id: Int, override val label: String) :Document

, Neo4jEObject by Neo4jEObjectImpl(dbSession, id, label) {

        override fun setTitle(attrValue: String) : Boolean {
            return  try {
                dbSession.writeTransaction {it.run("MATCH (c:$label) WHERE ID(c)={nodeID}  SET c.title = {value}",
                Values.parameters("nodeID", this.id, "value", attrValue))}
                true
            } catch (e : Exception) {
                e.printStackTrace()
                false
            }
        }

        override fun getTitle() : String? {
            //return String
            return  try {
                val res = dbSession.readTransaction {it.run("MATCH (c:$label) WHERE ID(c) = {nodeID} RETURN c.title AS p",
                    Values.parameters("nodeID", this.id))}
                res.single()["p"].asString()
            } catch (e : Exception) {
                e.printStackTrace()
                null
            }
        }
        
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
        
        override fun setAuthor(attrValue: String) : Boolean {
            return  try {
                dbSession.writeTransaction {it.run("MATCH (c:$label) WHERE ID(c)={nodeID}  SET c.author = {value}",
                Values.parameters("nodeID", this.id, "value", attrValue))}
                true
            } catch (e : Exception) {
                e.printStackTrace()
                false
            }
        }

        override fun getAuthor() : String? {
            //return String
            return  try {
                val res = dbSession.readTransaction {it.run("MATCH (c:$label) WHERE ID(c) = {nodeID} RETURN c.author AS p",
                    Values.parameters("nodeID", this.id))}
                res.single()["p"].asString()
            } catch (e : Exception) {
                e.printStackTrace()
                null
            }
        }
        
                override fun setBibliography(endNode: Bibliography) : Boolean {
                    return try {
                        val res = dbSession.writeTransaction {it.run("MATCH (c:$label) WHERE ID(c)={startNodeID}" +
                            " MATCH (e:${endNode.label}) WHERE ID(e)={endNodeID}" +
                            " OPTIONAL MATCH (c)-[r:bibliography {containment: {containment}}]->(:${endNode.label})" +
                            " OPTIONAL MATCH (e)<-[{containment: true}]-(endNodeContainer)" +
                            " WITH c, e, r, COUNT(endNodeContainer) = 0 AS predicate" +
                            " FOREACH (ignoreMe IN CASE WHEN predicate THEN [1] ELSE [] END |" +
                            "   DELETE r   MERGE (c)-[:bibliography {containment:{containment}}]->(e))" +
                            " RETURN predicate AS isAdded",
                            Values.parameters("startNodeID", this.id, "endNodeID", endNode.id, "containment", true))}
                        res.single()["isAdded"].asBoolean()
                    } catch (e : Exception) {
                        e.printStackTrace()
                        false
                    }
                 }
                 
                override fun addSection(endNode: Section) : Boolean {
                    return try {
                        val res = dbSession.writeTransaction {it.run("MATCH (c:$label) WHERE ID(c) = {startNodeID}" +
                            " MATCH (e:${endNode.label}) WHERE ID(e) = {endNodeID}" +
                            " OPTIONAL MATCH (e)<-[{containment: true}]-(endNodeContainer)" +
                            " WITH c, e, COUNT(endNodeContainer) = 0 AS predicate" +
                            " FOREACH (ignoreMe IN CASE WHEN predicate THEN [1] ELSE [] END |" +
                            "   CREATE (c)-[:section {containment:{containment}}]->(e))" +
                            " RETURN predicate AS isAdded",
                            Values.parameters("startNodeID", this.id, "endNodeID", endNode.id, "containment", true))}
                        res.single()["isAdded"].asBoolean()
                    } catch (e : Exception) {
                        e.printStackTrace()
                        false
                    }
                }
                
                override fun setAbstract(endNode: Abstract) : Boolean {
                    return try {
                        val res = dbSession.writeTransaction {it.run("MATCH (c:$label) WHERE ID(c)={startNodeID}" +
                            " MATCH (e:${endNode.label}) WHERE ID(e)={endNodeID}" +
                            " OPTIONAL MATCH (c)-[r:abstract {containment: {containment}}]->(:${endNode.label})" +
                            " OPTIONAL MATCH (e)<-[{containment: true}]-(endNodeContainer)" +
                            " WITH c, e, r, COUNT(endNodeContainer) = 0 AS predicate" +
                            " FOREACH (ignoreMe IN CASE WHEN predicate THEN [1] ELSE [] END |" +
                            "   DELETE r   MERGE (c)-[:abstract {containment:{containment}}]->(e))" +
                            " RETURN predicate AS isAdded",
                            Values.parameters("startNodeID", this.id, "endNodeID", endNode.id, "containment", true))}
                        res.single()["isAdded"].asBoolean()
                    } catch (e : Exception) {
                        e.printStackTrace()
                        false
                    }
                 }
                 
}