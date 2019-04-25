package geodes.sms.modeleditor.latexmetamodel
import geodes.sms.neo4jecore.Neo4jEObject

interface NamedTextContainer : TextContainer {

            fun setName (attrValue: String) : Boolean
            fun getName () : String?
            
}