package geodes.sms.modeleditor.latexmetamodel
import geodes.sms.neo4jecore.Neo4jEObject

interface Bibliography : NamedTextContainer {

            fun setStyle (attrValue: String) : Boolean
            fun getStyle () : String?
            
}