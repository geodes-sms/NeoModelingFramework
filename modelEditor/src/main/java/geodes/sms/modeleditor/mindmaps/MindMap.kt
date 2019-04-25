package geodes.sms.modeleditor.mindmaps
import geodes.sms.neo4jecore.Neo4jEObject

interface MindMap : Neo4jEObject {

            fun setTitle (attrValue: String) : Boolean
            fun getTitle () : String?
            
            fun setCentralTopic(endNode: CentralTopic) : Boolean
            
            fun addMarkers(endNode: Marker) : Boolean
            
}