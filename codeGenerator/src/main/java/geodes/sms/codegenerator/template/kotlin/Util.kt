package geodes.sms.codegenerator.template.kotlin

object Util {

    val type = mapOf(
        "boolean" to "Boolean",
        "byte" to "Byte",
        "byte[]" to "ByteArray",
        "short" to "Short",
        "char" to "Char",
        "int" to "Int",
        "long" to "Long",
        "float" to "Float",
        "double" to "Double",
        "java.util.Map" to "Map<String, Any>",
        "org.eclipse.emf.common.util.EList" to "List<Any>",

        "java.lang.Character" to "Char",
        "java.lang.String" to "String",
        "java.lang.Double" to "Double",
        "java.lang.Float" to "Float",
        "java.lang.Integer" to "Int",
        "java.lang.Object" to "Any",
        "java.lang.Long" to "Long",
        "java.lang.Short" to "Short",
        "java.lang.Boolean" to "Boolean",
        "java.lang.Byte" to "Byte"
    )

    /** for upperBound == 1*/
    val returnSingleType = mapOf(
        "Boolean"   to "res.single()[\"p\"].asBoolean()",
        "Byte"      to "res.single()[\"p\"].asNumber() as Byte",
        "ByteArray" to "res.single()[\"p\"].asByteArray()",
        "Short"     to "res.single()[\"p\"].asNumber() as Short",
        "Char"      to "res.single()[\"p\"].asString()[0]",
        "Int"       to "res.single()[\"p\"].asInt()",
        "Long"      to "res.single()[\"p\"].asLong()",
        "Float"     to "res.single()[\"p\"].asFloat()",
        "Double"    to "res.single()[\"p\"].asDouble()",

        "String"    to "res.single()[\"p\"].asString()",
        "Any"       to "res.single()[\"p\"].asObject()",
        "Map<String, Any>" to "res.single()[\"p\"].asMap()",
        "List<Any>" to "res.single()[\"p\"].asList()",
        "java.math.BigInteger" to "java.math.BigInteger(res.single()[\"p\"].asString())",
        "java.math.BigDecimal" to "java.math.BigDecimal(res.single()[\"p\"].asString())",
        "java.util.Date" to "java.util.Date.from(res.single()[\"p\"].asZonedDateTime().toInstant())"
    )

    /** for upperBound > 1 */
    val returnListType = mapOf(
        "Boolean"   to "res.single()[\"p\"].asList(Values.ofBoolean())",
        "Byte"      to "res.single()[\"p\"].asList { it.asNumber() as Byte }",
        "ByteArray" to "res.single()[\"p\"].asList { it.asByteArray() }",
        "Short"     to "res.single()[\"p\"].asList { it.asNumber() as Short }",
        "Char"      to "res.single()[\"p\"].asList { it.asString()[0] }",
        "Int"       to "res.single()[\"p\"].asList(Values.ofInteger())",
        "Long"      to "res.single()[\"p\"].asList(Values.ofLong())",
        "Float"     to "res.single()[\"p\"].asList(Values.ofFloat())",
        "Double"    to "res.single()[\"p\"].asList(Values.ofDouble())",

        "String"    to "res.single()[\"p\"].asList(Values.ofString())",
        "Any"       to "res.single()[\"p\"].asList(Values.ofObject())",
        //"Map<String, Any>" to "res.single()[\"p\"].asList(Values.ofMap())",
        //"List<Any>" to "res.single()[\"p\"].asList()",
        "java.math.BigInteger" to "res.single()[\"p\"].asList { java.math.BigInteger(it.asString()) }",
        "java.math.BigDecimal" to "res.single()[\"p\"].asList { java.math.BigDecimal(it.asString()) }",
        "java.util.Date" to "res.single()[\"p\"].asList { java.util.Date.from(it.asZonedDateTime().toInstant()) }"
    )

    /*
    fun attrSetStatement(eType: String, attrName: String) = when (eType) {

        "java.math.BigInteger" -> " SET c.$attrName = {value}\", Values.parameters(\"value\", attrValue.toString())"
        "java.math.BigDecimal" -> " SET c.$attrName = {value}\", Values.parameters(\"value\", attrValue.toString())"
        "java.util.Date" -> " SET c.$attrName = {value}\", Values.parameters(\"value\", java.time.ZonedDateTime" +
                ".ofInstant(attrValue.toInstant(), java.time.ZoneId.systemDefault()))"
        "java.util.Map" -> " MERGE (c)-[:attr {name:'$attrName'}]->(map) SET map = {value}\"," +
                                            " Values.parameters(\"value\", attrValue)"
        else -> " SET c.$attrName = {value}\", Values.parameters(\"value\", attrValue)"
    }*/
}