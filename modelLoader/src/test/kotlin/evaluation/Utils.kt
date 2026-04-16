package evaluation

import org.junit.jupiter.api.Test
import java.io.File

class Utils {

    fun fixXmiHeadersBySubfolder(baseFolder: File) {
        require(baseFolder.exists() && baseFolder.isDirectory) {
            "Base folder not found: ${baseFolder.absolutePath}"
        }

        baseFolder.listFiles { f -> f.isDirectory }?.forEach { subfolder ->
            val mm = subfolder.name

            val xmiFiles = subfolder.walkTopDown()
                .filter { it.isFile && it.extension.equals("xmi", ignoreCase = true) }
                .toList()

            for (xmiFile in xmiFiles) {
                val metamodelFile = File(baseFolder, "metamodels/$mm.ecore")
                val metamodelLowerCase = mm.replaceFirstChar { it.lowercase() }

                val header = """
            <xmi:XMI xmi:version="2.0"
                     xmlns:xmi="http://www.omg.org/XMI"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xmlns:$metamodelLowerCase="http://www.example.org/$mm"
                     xsi:schemaLocation="http://www.example.org/$mm ../../metamodels/${metamodelFile.name}">
                     """.trimIndent().replace("\n", " ")
                val lines = xmiFile.readLines().toMutableList()

                val newLines = mutableListOf<String>()

                var xmlDeclHandled = false
                var xmiHeaderInserted = false
                var firstModelTagHandled = false

                for ((index, line) in lines.withIndex()) {
                    val trimmed = line.trim()

                    // 1. XML declaration
                    if (!xmlDeclHandled && trimmed.startsWith("<?xml")) {
                        newLines.add(line)
                        xmlDeclHandled = true
                        continue
                    }

                    // 2. Existing <xmi:XMI> → replace
                    if (!xmiHeaderInserted && trimmed.startsWith("<xmi:XMI")) {
                        newLines.add(header)
                        xmiHeaderInserted = true
                        continue
                    }

                    // 3. First non-header tag → insert header if missing
                    if (!xmiHeaderInserted && trimmed.startsWith("<") && !trimmed.startsWith("<?")) {
                        newLines.add(header)
                        xmiHeaderInserted = true
                    }

                    // 4. Clean first model element tag
                    if (xmiHeaderInserted && !firstModelTagHandled &&
                        trimmed.startsWith("<") &&
                        !trimmed.startsWith("<xmi:XMI") &&
                        !trimmed.startsWith("<?")
                    ) {
                        val cleaned = cleanModelTag(trimmed)
                        newLines.add(cleaned)
                        firstModelTagHandled = true
                        continue
                    }

                    newLines.add(line)
                }

                // 5. Ensure closing tag
                if (newLines.none { it.contains("</xmi:XMI>") }) {
                    newLines.add("</xmi:XMI>")
                }

                xmiFile.writeText(newLines.joinToString("\n"))
                println("Fixed: ${xmiFile.absolutePath}")
            }
        }
    }

    /**
     * Removes duplicated attributes like:
     * - xmi:version
     * - xmlns:*
     * - xsi:*
     */
    private fun cleanModelTag(tag: String): String {
        return tag
            .replace(Regex("""\s+xmi:[^=]+="[^"]*""""), "")
            .replace(Regex("""\s+xmlns:[^=]+="[^"]*""""), "")
            .replace(Regex("""\s+xmlns="[^"]*""""), "")
            .replace(Regex("""\s+xsi:[^=]+="[^"]*""""), "")
    }

    @Test
    fun runFix() {
        val baseFolder = File("../Evaluation/dataset/models")
        fixXmiHeadersBySubfolder(baseFolder)
    }
}