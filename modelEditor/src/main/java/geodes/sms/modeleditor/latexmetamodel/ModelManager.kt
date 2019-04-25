package  geodes.sms.modeleditor.latexmetamodel

interface ModelManager {
            fun createBibliography() : Bibliography
            fun getBibliographyByID(id: Int) : Bibliography?
        
            fun createSection() : Section
            fun getSectionByID(id: Int) : Section?
        
            fun createDocument() : Document
            fun getDocumentByID(id: Int) : Document?
        
            fun createAbstract() : Abstract
            fun getAbstractByID(id: Int) : Abstract?
        
            fun createSubSection() : SubSection
            fun getSubSectionByID(id: Int) : SubSection?
        
            fun createParagraph() : Paragraph
            fun getParagraphByID(id: Int) : Paragraph?
        
            fun createSubSubSection() : SubSubSection
            fun getSubSubSectionByID(id: Int) : SubSubSection?
        }