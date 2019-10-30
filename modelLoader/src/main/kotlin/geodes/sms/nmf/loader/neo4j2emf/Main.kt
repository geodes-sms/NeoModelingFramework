package geodes.sms.nmf.loader.neo4j2emf


fun main(args: Array<String>) {
    val basePath = "/home/vitali/Desktop/my flash/model.loader/model"
    //val modelPath = "$basePath/Ecore.ecore"
    //val modelPath = "$basePath/Latex.ecore"
    val modelPath = "$basePath/G.ecore"

    val dbUri = "bolt://localhost:7687"
    val username = "neo4j"
    val password = "admin"
    val modelRootID: Long = 132
    val modelReader =
        Neo4jModelReader(dbUri, username, password, modelRootID)

    val metaEPackage =
        ModelLoader.loadMetamodelFromFile(modelPath)
    val loader = ModelLoader(metaEPackage, modelReader)
    loader.load("savedG.xmi")

    modelReader.close()

    /*
    val eFactory = metaEPackage.eFactoryInstance

    val docEClass = metaEPackage.getEClassifier("Document") as EClass
    val secEClass = metaEPackage.getEClassifier("Section") as EClass
    val subsecEClass = metaEPackage.getEClassifier("SubSection") as EClass

    val secFeature = docEClass.getEStructuralFeature("section")
    val subsecFeature = secEClass.getEStructuralFeature("subsection")

    val doc = eFactory.create(docEClass)
    val sec = eFactory.create(secEClass)
    val subSec = eFactory.create(subsecEClass)

    loader.connectEObjects(doc, secFeature, sec)

    /*
    (doc.eGet(secFeature) as EList<EObject>).add(sec)
    (sec.eGet(subsecFeature) as EList<EObject>).add(subSec)
    */

    val resourceSet = ResourceSetImpl()
    val resource = resourceSet.createResource(URI.createURI("./myTest.ecore"))
    resource.contents.add(doc)
    resource.save(System.out, null)
*/
}