package geodes.sms.modeleditor.mindmaps
import geodes.sms.neo4jecore.Neo4jEObject

interface CentralTopic : Topic {

            fun addMainTopics(endNode: MainTopic) : Boolean
            
}