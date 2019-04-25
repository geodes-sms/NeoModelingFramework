package geodes.sms.modeleditor.latexmetamodel
import geodes.sms.neo4jecore.Neo4jEObject

interface SubSubSection : NamedTextContainer {

            fun addParagraph(endNode: Paragraph) : Boolean
            
}