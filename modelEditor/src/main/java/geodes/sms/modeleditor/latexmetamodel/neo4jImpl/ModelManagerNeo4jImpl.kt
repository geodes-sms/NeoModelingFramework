package  geodes.sms.modeleditor.latexmetamodel.neo4jImpl

import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Values
import geodes.sms.modeleditor.latexmetamodel.*

class ModelManagerNeo4jImpl(uri: String, username: String, password: String): ModelManager, AutoCloseable {

    private val driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password))
    private val session = driver.session()

    override fun close() {
        session.close()
        driver.close()
    }

            override fun createBibliography() : Bibliography {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:Bibliography) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return BibliographyNeo4jImpl(session, id, "Bibliography")
            }

            override fun getBibliographyByID(id: Int) : Bibliography? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:Bibliography) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) BibliographyNeo4jImpl(session, id, "Bibliography") else null
                }
            }
        
            override fun createSection() : Section {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:Section) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return SectionNeo4jImpl(session, id, "Section")
            }

            override fun getSectionByID(id: Int) : Section? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:Section) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) SectionNeo4jImpl(session, id, "Section") else null
                }
            }
        
            override fun createDocument() : Document {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:document) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return DocumentNeo4jImpl(session, id, "document")
            }

            override fun getDocumentByID(id: Int) : Document? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:document) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) DocumentNeo4jImpl(session, id, "document") else null
                }
            }
        
            override fun createAbstract() : Abstract {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:Abstract) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return AbstractNeo4jImpl(session, id, "Abstract")
            }

            override fun getAbstractByID(id: Int) : Abstract? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:Abstract) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) AbstractNeo4jImpl(session, id, "Abstract") else null
                }
            }
        
            override fun createSubSection() : SubSection {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:SubSection) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return SubSectionNeo4jImpl(session, id, "SubSection")
            }

            override fun getSubSectionByID(id: Int) : SubSection? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:SubSection) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) SubSectionNeo4jImpl(session, id, "SubSection") else null
                }
            }
        
            override fun createParagraph() : Paragraph {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:Paragraph) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return ParagraphNeo4jImpl(session, id, "Paragraph")
            }

            override fun getParagraphByID(id: Int) : Paragraph? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:Paragraph) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) ParagraphNeo4jImpl(session, id, "Paragraph") else null
                }
            }
        
            override fun createSubSubSection() : SubSubSection {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:SubSubSection) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return SubSubSectionNeo4jImpl(session, id, "SubSubSection")
            }

            override fun getSubSubSectionByID(id: Int) : SubSubSection? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:SubSubSection) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) SubSubSectionNeo4jImpl(session, id, "SubSubSection") else null
                }
            }
        }