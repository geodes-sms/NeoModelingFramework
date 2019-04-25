package geodes.sms.modeleditor.latexmetamodel
import geodes.sms.neo4jecore.Neo4jEObject

interface Section : NamedTextContainer {

            fun setTest_opposite(endNode: Document) : Boolean
            
            fun addSubsection(endNode: SubSection) : Boolean
            
}