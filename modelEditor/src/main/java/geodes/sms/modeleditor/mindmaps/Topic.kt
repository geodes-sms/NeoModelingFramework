package geodes.sms.modeleditor.mindmaps
import geodes.sms.neo4jecore.Neo4jEObject

interface Topic : Neo4jEObject {

            fun setName (attrValue: String) : Boolean
            fun getName () : String?
            
            fun addMarker(endNode: Marker) : Boolean
            
}