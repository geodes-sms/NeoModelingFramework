package  geodes.sms.modeleditor.latexmetamodel.neo4jImpl

import geodes.sms.modeleditor.latexmetamodel.*
import geodes.sms.neo4jecore.Neo4jEObject
import geodes.sms.neo4jecore.Neo4jEObjectImpl
import org.neo4j.driver.v1.Session
import org.neo4j.driver.v1.Values

class SubSectionNeo4jImpl(override val dbSession: Session, override val id: Int, override val label: String) :SubSection
, NamedTextContainer by NamedTextContainerNeo4jImpl(dbSession, id, label)
{

                override fun addSubsubsection(endNode: SubSubSection) : Boolean {
                    return try {
                        val res = dbSession.writeTransaction {it.run("MATCH (c:$label) WHERE ID(c) = {startNodeID}" +
                            " MATCH (e:${endNode.label}) WHERE ID(e) = {endNodeID}" +
                            " OPTIONAL MATCH (e)<-[{containment: true}]-(endNodeContainer)" +
                            " WITH c, e, COUNT(endNodeContainer) = 0 AS predicate" +
                            " FOREACH (ignoreMe IN CASE WHEN predicate THEN [1] ELSE [] END |" +
                            "   CREATE (c)-[:subsubsection {containment:{containment}}]->(e))" +
                            " RETURN predicate AS isAdded",
                            Values.parameters("startNodeID", this.id, "endNodeID", endNode.id, "containment", true))}
                        res.single()["isAdded"].asBoolean()
                    } catch (e : Exception) {
                        e.printStackTrace()
                        false
                    }
                }
                
}