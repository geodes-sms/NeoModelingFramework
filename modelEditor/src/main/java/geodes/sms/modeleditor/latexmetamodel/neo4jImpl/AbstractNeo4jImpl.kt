package  geodes.sms.modeleditor.latexmetamodel.neo4jImpl

import geodes.sms.modeleditor.latexmetamodel.*
import geodes.sms.neo4jecore.Neo4jEObject
import geodes.sms.neo4jecore.Neo4jEObjectImpl
import org.neo4j.driver.v1.Session
import org.neo4j.driver.v1.Values

class AbstractNeo4jImpl(override val dbSession: Session, override val id: Int, override val label: String) :Abstract
, TextContainer by TextContainerNeo4jImpl(dbSession, id, label)
{

}