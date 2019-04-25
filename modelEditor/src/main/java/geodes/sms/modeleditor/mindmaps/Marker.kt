package geodes.sms.modeleditor.mindmaps
import geodes.sms.neo4jecore.Neo4jEObject

interface Marker : Neo4jEObject {

            fun setName (attrValue: String) : Boolean
            fun getName () : String?
            
}