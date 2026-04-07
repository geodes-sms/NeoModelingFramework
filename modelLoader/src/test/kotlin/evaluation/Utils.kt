package evaluation

import org.junit.jupiter.api.Test
import java.io.File

class Utils {

    fun fixXmiHeadersBySubfolder(baseFolder: File, headerMap: Map<String, String>) {
        require(baseFolder.exists() && baseFolder.isDirectory) {
            "Base folder not found: ${baseFolder.absolutePath}"
        }

        // Iterate over all subfolders
        baseFolder.listFiles { f -> f.isDirectory }?.forEach { subfolder ->
            val header = headerMap[subfolder.name]
            if (header == null) {
                println("No header mapping for subfolder '${subfolder.name}', skipping")
                return@forEach
            }

            // Find all XMI files in the subfolder
            val xmiFiles = subfolder.listFiles { f ->
                f.isFile && f.extension.equals("xmi", ignoreCase = true)
            } ?: emptyArray()

            for (xmiFile in xmiFiles) {
                // Process file line by line
                val tempFile = File(xmiFile.absolutePath + ".tmp")
                xmiFile.bufferedReader().use { reader ->
                    tempFile.bufferedWriter().use { writer ->
                        var firstLineReplaced = false
                        reader.forEachLine { line ->
                            if (!firstLineReplaced && line.trimStart().startsWith("<xmi:XMI")) {
                                writer.write(header)
                                writer.newLine()
                                firstLineReplaced = true
                            } else {
                                writer.write(line)
                                writer.newLine()
                            }
                        }
                    }
                }
                // Replace original file with fixed file
                if (!xmiFile.delete() || !tempFile.renameTo(xmiFile)) {
                    println("Failed to replace original file: ${xmiFile.absolutePath}")
                } else {
                    println("Fixed header for: ${xmiFile.absolutePath}")
                }
            }
        }
    }

    @Test
    fun runFix() {
        val baseFolder = File("../Evaluation/LinTra")

        val headerMap = mapOf(
            "java" to """<xmi:XMI xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:javaMM="http://www.eclipse.org/MoDisco/Java/0.2.incubation/java" xsi:schemaLocation="http://www.eclipse.org/MoDisco/Java/0.2.incubation/java ./java.ecore">""",
            "DBLP" to """<xmi:XMI xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:DBLP="http://www.example.org/DBLP" xsi:schemaLocation="http://www.example.org/DBLP ./DBLP.ecore">""",
            "movies" to """<xmi:XMI xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:movies="http://movies/1.0" xsi:schemaLocation="http://movies/1.0 ./movies.ecore">"""
        )

        fixXmiHeadersBySubfolder(baseFolder, headerMap)
    }
}