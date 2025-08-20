package evaluation

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.*
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.impl.*
import org.eclipse.emf.ecore.xmi.impl.*
import java.io.File
import org.junit.jupiter.api.Test

class AnalyzeModels{
    @Test
    fun runEcoreAnalysis() {
        exportEcoreMetricsToCSV("../Evaluation/models", "../Evaluation/results/RQ1/ecore/models.csv")
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

    fun countClassifiers(pkg: EPackage): Triple<Int, Int, Pair<Int, Int>> {
        var numClasses = 0
        var numAttributes = 0
        var numReferences = 0
        var numContainments = 0

        // Count classifiers in this package
        for (classifier in pkg.eClassifiers) {
            if (classifier is EClass) {
                numClasses++
                numAttributes += classifier.eAttributes.size
                numReferences += classifier.eReferences.count { !it.isContainment }
                numContainments += classifier.eReferences.count { it.isContainment }
            }
        }

        // Recurse into subpackages
        for (subPkg in pkg.eSubpackages) {
            val (subClasses, subAttributes, subRefsAndConts) = countClassifiers(subPkg)
            numClasses += subClasses
            numAttributes += subAttributes
            numReferences += subRefsAndConts.first
            numContainments += subRefsAndConts.second
        }

        return Triple(numClasses, numAttributes, numReferences to numContainments)
    }

    val packages = loadEcoreModel(filePath)
    var totalClasses = 0
    var totalAttributes = 0
    var totalReferences = 0
    var totalContainments = 0

    for (pkg in packages) {
        val (cls, attr, refsAndConts) = countClassifiers(pkg)
        totalClasses += cls
        totalAttributes += attr
        totalReferences += refsAndConts.first
        totalContainments += refsAndConts.second
    }

    val modelName = filePath.substringAfterLast(File.separator).removeSuffix(".ecore")
    val loc = File(filePath).useLines { it.count() }

    return EcoreMetrics(modelName, totalClasses, totalAttributes, totalReferences, totalContainments, loc)
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
