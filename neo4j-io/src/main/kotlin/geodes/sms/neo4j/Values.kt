package geodes.sms.neo4j

import org.neo4j.driver.internal.value.*
import org.neo4j.driver.Value
import org.neo4j.driver.Values
import java.time.ZoneId
import java.time.ZonedDateTime

/** Additional types, missing in standard driver implementation */
object Values {

    fun value(input: Any?): Value = when (input) {
//        is String -> StringValue(input)
//        is Int -> IntegerValue(input.toLong())
//        is Long -> IntegerValue(input)
//        is Short -> IntegerValue(input.toLong())
//        is Boolean -> BooleanValue.fromBoolean(input)
//        is Byte -> IntegerValue(input.toLong())
//        is ByteArray -> BytesValue(input)
//        is Char -> StringValue(input.toString())
//        is Double -> FloatValue(input)
//        is Float ->  FloatValue(input.toDouble())
        is Enum<*> -> StringValue(input.name)
        is java.util.Date -> DateTimeValue(ZonedDateTime.ofInstant(input.toInstant(), ZoneId.systemDefault()))
        is java.math.BigDecimal -> StringValue(input.toString())
        is java.math.BigInteger -> StringValue(input.toString())
        //is Map.Entry<*, *> -> {}
        is List<*> -> value(input)    //EList
        //is EEnum = EClass = EDataType  //not in model instance; is not a direct attr type
        null -> NullValue.NULL  //not persistable value
        else -> Values.value(input)
    }

    fun value(input: java.math.BigDecimal) = StringValue(input.toString())
    fun value(input: java.math.BigInteger) = StringValue(input.toString())
    fun value(input: Enum<*>) = StringValue(input.name)
    fun value(input: java.util.Date) = DateTimeValue(ZonedDateTime.ofInstant(input.toInstant(), ZoneId.systemDefault()))
    fun value(input: List<*>) = ListValue(*Array<Value?>(input.size) { i -> value(input[i]) })

    fun asEnum() {

    }
}

