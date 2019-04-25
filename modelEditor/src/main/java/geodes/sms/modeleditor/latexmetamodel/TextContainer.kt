package geodes.sms.modeleditor.latexmetamodel
import geodes.sms.neo4jecore.Neo4jEObject

interface TextContainer : Neo4jEObject {

            fun setText (attrValue: String) : Boolean
            fun getText () : String?
            
}