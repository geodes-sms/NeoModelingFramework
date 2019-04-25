package geodes.sms.modeleditor.mindmaps
import geodes.sms.neo4jecore.Neo4jEObject

interface SubTopic : Topic {

            fun addSubsubTopics(endNode: SubTopic) : Boolean
            
            fun setCrossRef(endNode: SubTopic) : Boolean
            
}