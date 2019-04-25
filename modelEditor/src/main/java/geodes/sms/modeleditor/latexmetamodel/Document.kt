package geodes.sms.modeleditor.latexmetamodel
import geodes.sms.neo4jecore.Neo4jEObject

interface Document : Neo4jEObject {

            fun setTitle (attrValue: String) : Boolean
            fun getTitle () : String?
            
            fun setName (attrValue: String) : Boolean
            fun getName () : String?
            
            fun setAuthor (attrValue: String) : Boolean
            fun getAuthor () : String?
            
            fun setBibliography(endNode: Bibliography) : Boolean
            
            fun addSection(endNode: Section) : Boolean
            
            fun setAbstract(endNode: Abstract) : Boolean
            
}