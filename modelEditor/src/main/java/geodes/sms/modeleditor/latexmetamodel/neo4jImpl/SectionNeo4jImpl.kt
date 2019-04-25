package  geodes.sms.modeleditor.latexmetamodel.neo4jImpl

import geodes.sms.modeleditor.latexmetamodel.*
import geodes.sms.neo4jecore.Neo4jEObject
import geodes.sms.neo4jecore.Neo4jEObjectImpl
import org.neo4j.driver.v1.Session
import org.neo4j.driver.v1.Values

class SectionNeo4jImpl(override val dbSession: Session, override val id: Int, override val label: String) :Section
, NamedTextContainer by NamedTextContainerNeo4jImpl(dbSession, id, label)
{

                override fun setTest_opposite(endNode: Document) : Boolean {
                    return try {
                        val res = dbSession.writeTransaction {it.run("MATCH (c:$label) WHERE ID(c)={startNodeID}" +
                            " MATCH (e:${endNode.label}) WHERE ID(e)={endNodeID}" +
                            " OPTIONAL MATCH (c)-[r:test_opposite {containment: {containment}}]->(:${endNode.label})" +
                            " OPTIONAL MATCH (e)<-[{containment: true}]-(endNodeContainer)" +
                            " WITH c, e, r, COUNT(endNodeContainer) = 0 AS predicate" +
                            " FOREACH (ignoreMe IN CASE WHEN predicate THEN [1] ELSE [] END |" +
                            "   DELETE r   MERGE (c)-[:test_opposite {containment:{containment}}]->(e))" +
                            " RETURN predicate AS isAdded",
                            Values.parameters("startNodeID", this.id, "endNodeID", endNode.id, "containment", false))}
                        res.single()["isAdded"].asBoolean()
                    } catch (e : Exception) {
                        e.printStackTrace()
                        false
                    }
                 }
                 
                override fun addSubsection(endNode: SubSection) : Boolean {
                    return try {
                        val res = dbSession.writeTransaction {it.run("MATCH (c:$label) WHERE ID(c) = {startNodeID}" +
                            " MATCH (e:${endNode.label}) WHERE ID(e) = {endNodeID}" +
                            " OPTIONAL MATCH (e)<-[{containment: true}]-(endNodeContainer)" +
                            " WITH c, e, COUNT(endNodeContainer) = 0 AS predicate" +
                            " FOREACH (ignoreMe IN CASE WHEN predicate THEN [1] ELSE [] END |" +
                            "   CREATE (c)-[:subsection {containment:{containment}}]->(e))" +
                            " RETURN predicate AS isAdded",
                            Values.parameters("startNodeID", this.id, "endNodeID", endNode.id, "containment", true))}
                        res.single()["isAdded"].asBoolean()
                    } catch (e : Exception) {
                        e.printStackTrace()
                        false
                    }
                }
                
}