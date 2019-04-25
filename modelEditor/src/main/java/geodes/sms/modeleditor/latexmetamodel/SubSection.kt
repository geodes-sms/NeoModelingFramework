package geodes.sms.modeleditor.latexmetamodel
import geodes.sms.neo4jecore.Neo4jEObject

interface SubSection : NamedTextContainer {

            fun addSubsubsection(endNode: SubSubSection) : Boolean
            
}