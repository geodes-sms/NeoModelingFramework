package geodes.sms.modeleditor.mindmaps
import geodes.sms.neo4jecore.Neo4jEObject

interface MainTopic : Topic {

            fun addSubTopics(endNode: SubTopic) : Boolean
            
}