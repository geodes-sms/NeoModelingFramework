package evaluation

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.*
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.junit.jupiter.api.Test
import java.io.File

/*/
To run this file, follow the steps on the Readme.md
 */
// Test file to evaluate RQ1
class RQ1Eval {
    @Test
    fun runEcoreAnalysis() {
        exportEcoreMetricsToCSV(
            "../Evaluation/metamodels",
            "../Evaluation/results/RQ1/metamodels.csv"
        )
    }
}

//  Data class for metrics 
data class EcoreMetrics(
    val modelName: String,
    val numClasses: Int,
    val numAttributes: Int,
    val numReferences: Int,
    val numContainments: Int,
    val loc: Int,
    val maxDIT: Int,
    val maxHagg: Int
)

//  Load Ecore model 
fun loadEcoreModel(filePath: String): Collection<EPackage> {
    val resourceSet = ResourceSetImpl()
    resourceSet.resourceFactoryRegistry.extensionToFactoryMap["ecore"] = EcoreResourceFactoryImpl()
    val fileUri = URI.createFileURI(File(filePath).absolutePath)
    val resource = resourceSet.getResource(fileUri, true)
    resource.load(null)
    return resource.contents.filterIsInstance<EPackage>()
}

//  DIT (Generalisation / Inheritance) 
fun computeDIT(
    eClass: EClass,
    visited: MutableSet<EClass> = mutableSetOf(),
    memo: MutableMap<EClass, Int> = mutableMapOf()
): Int {
    memo[eClass]?.let { return it }
    if (!visited.add(eClass)) return 0 // cycle detected

    val depth = if (eClass.eSuperTypes.isEmpty()) 1
    else 1 + eClass.eSuperTypes.maxOf { computeDIT(it, visited, memo) }

    visited.remove(eClass)
    memo[eClass] = depth
    return depth
}

fun computeMaxDIT(allClasses: Collection<EClass>): Int {
    val memo = mutableMapOf<EClass, Int>()
    return allClasses.maxOfOrNull { computeDIT(it, mutableSetOf(), memo) } ?: 0
}

//  HAgg (Aggregation / Containment) 
fun buildAggregationMap(allClasses: Collection<EClass>): Map<EClass, List<EClass>> {
    val map = mutableMapOf<EClass, MutableList<EClass>>()
    for (cls in allClasses) {
        for (ref in cls.eReferences.filter { it.isContainment }) {
            val target = ref.eType as? EClass ?: continue
            map.computeIfAbsent(cls) { mutableListOf() }.add(target)
        }
    }
    return map
}

fun computeHaggDepth(
    eClass: EClass,
    aggMap: Map<EClass, List<EClass>>,
    visited: MutableSet<EClass> = mutableSetOf(),
    memo: MutableMap<EClass, Int> = mutableMapOf()
): Int {
    memo[eClass]?.let { return it }
    if (!visited.add(eClass)) return 0 // cycle detected

    val children = aggMap[eClass] ?: emptyList()
    val depth = if (children.isEmpty()) 1 else 1 + children.maxOf { computeHaggDepth(it, aggMap, visited, memo) }

    visited.remove(eClass)
    memo[eClass] = depth
    return depth
}

fun computeMaxHagg(allClasses: Collection<EClass>): Int {
    val aggMap = buildAggregationMap(allClasses)
    val memo = mutableMapOf<EClass, Int>()
    return allClasses.maxOfOrNull { computeHaggDepth(it, aggMap, mutableSetOf(), memo) } ?: 0
}

//  Main analysis 
fun analyzeEcore(filePath: String): EcoreMetrics {
    val packages = loadEcoreModel(filePath)

    val allClasses = mutableListOf<EClass>()
    var totalAttributes = 0
    var totalReferences = 0
    var totalContainments = 0

    fun collectClasses(pkg: EPackage) {
        for (classifier in pkg.eClassifiers) {
            if (classifier is EClass) {
                allClasses.add(classifier)
                totalAttributes += classifier.eAttributes.size
                totalReferences += classifier.eReferences.count { !it.isContainment }
                totalContainments += classifier.eReferences.count { it.isContainment }
            }
        }
        pkg.eSubpackages.forEach { collectClasses(it) }
    }

    for (pkg in packages) {
        collectClasses(pkg)
    }

    val maxDIT = computeMaxDIT(allClasses)
    val maxHagg = computeMaxHagg(allClasses)

    val modelName = filePath.substringAfterLast(File.separator).removeSuffix(".ecore")
    val loc = File(filePath).useLines { it.count() }

    return EcoreMetrics(
        modelName,
        allClasses.size,
        totalAttributes,
        totalReferences,
        totalContainments,
        loc,
        maxDIT,
        maxHagg
    )
}

//  CSV Export 
fun exportEcoreMetricsToCSV(directoryPath: String, outputPath: String) {
    val metamodelsDir = File(directoryPath)
    val ecoreFiles = metamodelsDir.walk()
        .filter { it.isFile && it.extension == "ecore" }
        .map { it.path }
        .toList()

    val outputFile = File(outputPath)
    outputFile.writeText("model,LOC,classes,attributes,refs,containments,maxDIT,maxHagg\n")
    for (model in ecoreFiles) {
        val metrics = analyzeEcore(model)
        outputFile.appendText(
            "${metrics.modelName},${metrics.loc},${metrics.numClasses}," +
                    "${metrics.numAttributes},${metrics.numReferences},${metrics.numContainments}," +
                    "${metrics.maxDIT},${metrics.maxHagg}\n"
        )
    }
}
