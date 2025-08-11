package evaluation

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.*
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.impl.*
import org.eclipse.emf.ecore.xmi.impl.*
import java.io.File
import org.junit.jupiter.api.Test
//import org.eclipse.gmt.modisco.java.cdo.meta.JavaPackage

class AnalyzeModels{
    @Test
    fun runEcoreAnalysis() {
        exportEcoreMetricsToCSV("../ECMFA-2026-Evaluation/models", "../ECMFA-2026-Evaluation/results/RQ1/ecore/models.csv")
    }
}
data class EcoreMetrics(
    val modelName: String,
    val numClasses: Int,
    val numAttributes: Int,
    val numReferences: Int,
    val numContainments: Int,
    val loc: Int
)

fun loadEcoreModel(filePath: String): Collection<EPackage> {
    val resourceSet = ResourceSetImpl()
    resourceSet.resourceFactoryRegistry.extensionToFactoryMap["ecore"] = EcoreResourceFactoryImpl()
    val fileUri = URI.createFileURI(File(filePath).absolutePath)
    val resource = resourceSet.getResource(fileUri, true)
    resource.load(null)
    return resource.contents.filterIsInstance<EPackage>()
}

fun analyzeEcore(filePath: String): EcoreMetrics {
    val packages = loadEcoreModel(filePath)
    var numClasses = 0
    var numAttributes = 0
    var numReferences = 0
    var numContainments = 0

    for (pkg in packages) {
        for (classifier in pkg.eClassifiers) {
            if (classifier is EClass) {
                numClasses++
                numAttributes += classifier.eAttributes.size
                numReferences += classifier.eReferences.count { !it.isContainment }
                numContainments += classifier.eReferences.count { it.isContainment }
            }
        }
    }

    val modelName = filePath.substringAfterLast(File.separator).removeSuffix(".ecore")
    val loc = File(filePath).useLines { it.count() }

    return EcoreMetrics(modelName, numClasses, numAttributes, numReferences, numContainments, loc)
}


fun exportEcoreMetricsToCSV(directoryPath: String, outputPath: String) {
    val modelsDir = File(directoryPath)
    val ecoreFiles = modelsDir.walk()
        .filter { it.isFile && it.extension == "ecore" }
        .map { it.path }
        .toList()

    val outputFile = File(outputPath)
    outputFile.writeText("model,LOC,classes,attributes,refs,containments\n")
    for (model in ecoreFiles) {
        val metrics = analyzeEcore(model)
        outputFile.appendText("${metrics.modelName},${metrics.loc},${metrics.numClasses},${metrics.numAttributes},${metrics.numReferences},${metrics.numContainments}\n")

    }

}
//
//@Test
//fun runXMIAnalysis() {
//    exportXmiMetricsToCSV("../ECMFA-2026-Evaluation/NeoEMF-benchmark-models", "../ECMFA-2026-Evaluation/results/RQ1/xmi/models.csv")
//}
//fun loadXMIModel(filePath: String): Collection<EPackage> {
//    val resourceSet = ResourceSetImpl()
//    resourceSet.resourceFactoryRegistry.extensionToFactoryMap["xmi"] = XMIResourceFactoryImpl()
//    val fileUri = URI.createFileURI(File(filePath).absolutePath)
//    val resource = resourceSet.getResource(fileUri, true)
//    resource.load(null)
//    return resource.contents.filterIsInstance<EPackage>()
//}
//
//data class XmiMetrics(
//    val modelName: String,
//    val classDeclaration: Int,
//    val fieldDeclaration: Int,
//    val methodDeclaration: Int
//)
//fun exportXmiMetricsToCSV(directoryPath: String, outputPath: String) {
//    val modelsDir = File(directoryPath)
//    val xmiFiles = modelsDir.walk()
//        .filter { it.isFile && it.extension == "xmi" }
//        .map { it.path }
//        .toList()
//
//    val outputFile = File(outputPath)
//    outputFile.writeText("model,classDeclaration,fieldDeclaration,methodDeclaration\n") // header
//
//    for (model in xmiFiles) {
//        val metrics = analyzeJavaModel(model)
//        outputFile.appendText("${metrics.modelName},${metrics.classDeclaration},${metrics.fieldDeclaration},${metrics.methodDeclaration}\n")
//    }
//
//}
//fun analyzeJavaModel(xmiFilePath: String): XmiMetrics {
//    val resourceSet = ResourceSetImpl()
//    EPackage.Registry.INSTANCE.put(JavaPackage.eNS_URI, JavaPackage.eINSTANCE);
//    // Register XMI factory for .xmi files
//    resourceSet.resourceFactoryRegistry.extensionToFactoryMap["xmi"] = XMIResourceFactoryImpl()
//
//    // Load the MoDisco Java metamodel
//    //val metamodelUri = URI.createFileURI(metamodelPath)
//    //val metamodelResource = resourceSet.getResource(metamodelUri, true)
//    // val ePackages = metamodelResource.contents.filterIsInstance<org.eclipse.emf.ecore.EPackage>()
//
//    //
//
//    // Load the XMI model (your sample Java model)
//    val modelUri = URI.createFileURI(xmiFilePath)
//    val modelResource = resourceSet.getResource(modelUri, true)
//
//    var numClassDeclarations = 0
//    var numFieldDeclarations = 0
//    var numMethodDeclarations = 0
//
//    fun traverse(eObject: EObject) {
//        // Count specific MoDisco Java types by checking their eClass name or namespace
//        val eClassName = eObject.eClass().name
//
//        when (eClassName) {
//            "ClassDeclaration" -> numClassDeclarations++
//            "FieldDeclaration" -> numFieldDeclarations++
//            "MethodDeclaration" -> numMethodDeclarations++
//        }
//
//        eObject.eContents().forEach { traverse(it) }
//    }
//
//    modelResource.contents.forEach { traverse(it) }
//
//    val modelName = File(xmiFilePath).nameWithoutExtension
//    return XmiMetrics(modelName, numClassDeclarations, numFieldDeclarations, numMethodDeclarations)
//}

