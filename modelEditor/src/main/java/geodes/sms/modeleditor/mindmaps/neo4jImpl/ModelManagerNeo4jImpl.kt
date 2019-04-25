package  geodes.sms.modeleditor.mindmaps.neo4jImpl

import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Values
import geodes.sms.modeleditor.mindmaps.*

class ModelManagerNeo4jImpl(uri: String, username: String, password: String): ModelManager, AutoCloseable {

    private val driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password))
    private val session = driver.session()

    override fun close() {
        session.close()
        driver.close()
    }

            override fun createSubTopic() : SubTopic {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:SubTopic) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return SubTopicNeo4jImpl(session, id, "SubTopic")
            }

            override fun getSubTopicByID(id: Int) : SubTopic? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:SubTopic) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) SubTopicNeo4jImpl(session, id, "SubTopic") else null
                }
            }
        
            override fun createMainTopic() : MainTopic {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:MainTopic) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return MainTopicNeo4jImpl(session, id, "MainTopic")
            }

            override fun getMainTopicByID(id: Int) : MainTopic? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:MainTopic) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) MainTopicNeo4jImpl(session, id, "MainTopic") else null
                }
            }
        
            override fun createCentralTopic() : CentralTopic {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:CentralTopic) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return CentralTopicNeo4jImpl(session, id, "CentralTopic")
            }

            override fun getCentralTopicByID(id: Int) : CentralTopic? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:CentralTopic) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) CentralTopicNeo4jImpl(session, id, "CentralTopic") else null
                }
            }
        
            override fun createMarker() : Marker {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:Marker) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return MarkerNeo4jImpl(session, id, "Marker")
            }

            override fun getMarkerByID(id: Int) : Marker? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:Marker) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) MarkerNeo4jImpl(session, id, "Marker") else null
                }
            }
        
            override fun createMindMap() : MindMap {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:MindMap) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return MindMapNeo4jImpl(session, id, "MindMap")
            }

            override fun getMindMapByID(id: Int) : MindMap? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:MindMap) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) MindMapNeo4jImpl(session, id, "MindMap") else null
                }
            }
        }