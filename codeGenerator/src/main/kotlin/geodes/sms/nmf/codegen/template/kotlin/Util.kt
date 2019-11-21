package geodes.sms.nmf.codegen.template.kotlin

import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EEnum

object Util {

    val type = mapOf(
        "EBigDecimal"       to "java.math.BigDecimal",
        "EBigInteger"       to "java.math.BigInteger",
        "EBoolean"          to "Boolean",
        "EBooleanObject"    to "Boolean",
        "EByte"             to "Byte",
        "EByteObject"       to "Byte",
        "EByteArray"        to "ByteArray",
        "EChar"             to "Char",
        "ECharacterObject"  to "Char",
        "EDate"             to "java.time.ZonedDateTime",   //"java.util.Date",
        "EDouble"           to "Double",
        "EDoubleObject"     to "Double",
        "EEList"            to "List<Any>",
        "EFloat"            to "Float",
        "EFloatObject"      to "Float",
        "EInt"              to "Int",
        "EIntegerObject"    to "Int",
        "EJavaObject"       to "Any",
        "EJavaClass"        to "Any",
        "ELong"             to "Long",
        "ELongObject"       to "Long",
        //"EMap"              to "MutableMap<String, Any>",
        "EShort"            to "Short",
        "EShortObject"      to "Short",
        "EString"           to "String"
    )

    fun getAttrType(eAttr: EAttribute): String {
        val eType = eAttr.eAttributeType
        val kotlinType = type[eType.name]
        return when {
            kotlinType != null -> kotlinType
            eType is EEnum -> eType.name
            else -> "Any"
        }
    }

    /** Default values for collections initializing */
    val defaultValue = mapOf(
        "java.math.BigDecimal" to "java.math.BigDecimal(\"0\")",
        "java.math.BigInteger" to "java.math.BigInteger(\"0\")",
        "Boolean" to "false",
        "Byte"    to "0",
        "ByteArray" to "ByteArray(10)",
        "Char"   to "'\\u0000'",
        "Double" to "0.0",
        "String" to "\"\"",
        "Float" to "0.0F",
        "Int"   to "0",
        "Long"  to "0L",
        "Short" to "0",
        "java.util.Date" to "java.util.Date()",
        "MutableMap<String, Any>" to "hashMapOf()",
        "List<Any>" to "mutableListOf()",
        "Any" to "Unit"
    )

    /** Default values for single property initializing */
    fun defaultValue(value: Any) = when(value) {
        is Boolean -> "$value"
        is Byte    -> "$value"
        is Char   -> "'$value'"
        is Double -> "$value"
        is String -> "$value"
        is Float -> "${value}F"
        is Int   -> "$value"
        is Long  -> "${value}L"
        is Short -> "$value"
        is ByteArray -> "\"${String(value)}\".toByteArray()"
        is java.util.Date -> "java.util.Date(${value.time}L)"   //ZonedDateTime.ofInstant(input.toInstant(), ZoneId.systemDefault())
        is java.math.BigDecimal -> "java.math.BigDecimal(\"$value\")"
        is java.math.BigInteger -> "java.math.BigInteger(\"$value\")"

        is Map<*, *> -> "hashMapOf()"
        is List<*> -> "mutableListOf()"
        else -> "Unit"  //is Any
    }
}