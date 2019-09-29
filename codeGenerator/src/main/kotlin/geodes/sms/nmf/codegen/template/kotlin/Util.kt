package geodes.sms.nmf.codegen.template.kotlin

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
        "EDate"             to "java.util.Date",
        "EDouble"           to "Double",
        "EDoubleObject"     to "Double",
        "EEList"            to "List<Any>",
        "EFloat"            to "Float",
        "EFloatObject"      to "Float",
        "EInt"              to "Int",
        "EIntegerObject"    to "Int",
        "EJavaObject"       to "Any",
        "ELong"             to "Long",
        "ELongObject"       to "Long",
        "EMap"              to "HashMap<String, Any>",
        "EShort"            to "Short",
        "EShortObject"      to "Short",
        "EString"           to "String"
    )

    val defaultValue = mapOf(
        "java.math.BigDecimal" to "java.math.BigDecimal(\"0\")",
        "java.math.BigInteger" to "java.math.BigInteger(\"0\")",
        "Boolean" to "false",
        "Byte"    to "0",
        "Char"   to "'\\u0000'",
        "Double" to "0.0",
        "String" to "\"\"",
        "Float" to "0.0F",
        "Int"   to "0",
        "Long"  to "0L",
        "Short" to "0",
        "java.util.Date"   to "java.util.Date()",
        "HashMap<String, Any>" to "hashMapOf()",
        "List<Any>" to "mutableListOf()",
        "Any" to "Unit",
        "ByteArray" to "ByteArray(5)"
    )

    fun wrapEmfDefaultValue() {

    }
}