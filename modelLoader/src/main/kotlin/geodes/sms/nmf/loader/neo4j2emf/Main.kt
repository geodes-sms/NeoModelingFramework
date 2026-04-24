//package geodes.sms.nmf.loader.neo4j2emf
//
//
//fun main(args: Array<String>) {
//    val basePath = "/home/vitali/Desktop/my flash/model.loader/model"
//    //val modelPath = "$basePath/Ecore.ecore"
//    //val modelPath = "$basePath/Latex.ecore"
//    val modelPath = "$basePath/G.ecore"
//
//    val dbUri = "bolt://localhost:7687"
//    val username = "neo4j"
//    val password = "admin"
//    val modelRootID: Long = 132
//    val modelReader = Neo4jModelReader(dbUri, username, password, modelRootID)
//
//    val metaEPackage = ModelLoader.loadMetamodelFromFile(modelPath)
//    val loader = ModelLoader(metaEPackage, modelReader)
//    loader.load("savedG.xmi")
//
//    modelReader.close()
//}