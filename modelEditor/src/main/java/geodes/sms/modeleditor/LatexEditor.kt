package geodes.sms.modeleditor

//import geodes.sms.modeleditor.latexmetamodel.neo4jImpl.ModelManagerNeo4jImpl


object LatexEditor {

    @JvmStatic
    fun main(args: Array<String>) {

        /*
        val dbUri = "bolt://localhost:7687"
        val username = "neo4j"
        val password = "admin"
        val manager = ModelManagerNeo4jImpl(dbUri, username, password)

        /*
        val doc = manager.createDocument()
        val abstract1 = manager.createAbstract()
        val abstract2 = manager.createAbstract()
        val bibliography = manager.createBibliography()
        //val sec1 = manager.createSection()
        //val sec2 = manager.createSection()

        doc.setName("doc1")
        abstract1.setText("abstract1")
        abstract2.setText("abstract2")
        bibliography.setText("bibliography")
        bibliography.setStyle("some style")

        doc.setAbstract(abstract1)
        doc.setBibliography(bibliography)*/

        val doc = manager.getDocumentByID(1737)
        val sec1 = manager.createSection()
        val sec2 = manager.createSection()

        doc?.addSection(sec1)
        doc?.addSection(sec2)
        sec1.setName("sec1")

        manager.close()
        */
    }
}