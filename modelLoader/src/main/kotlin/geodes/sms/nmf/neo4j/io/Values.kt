package geodes.sms.nmf.neo4j.io

import org.eclipse.emf.ecore.EEnumLiteral
import org.neo4j.driver.internal.value.*
import org.neo4j.driver.Value
import java.time.ZoneId
import java.time.ZonedDateTime


object Values {
    fun value(input: Any?): Value = when (input) {
        null -> NullValue.NULL
        is String ->  StringValue(input)
        is Int ->   IntegerValue(input.toLong())
        is Long ->  IntegerValue(input)
        is Short -> IntegerValue(input.toLong())
        is Boolean -> BooleanValue.fromBoolean(input)
        is Byte ->  IntegerValue(input.toLong())
        is ByteArray -> BytesValue(input)
        is Char ->    StringValue(input.toString())
        is Double ->  FloatValue(input)
        is Float ->   FloatValue(input.toDouble())
        is EEnumLiteral -> StringValue(input.literal)
        is java.util.Date -> DateTimeValue(ZonedDateTime.ofInstant(input.toInstant(), ZoneId.systemDefault()))
        is java.math.BigDecimal -> StringValue(input.toString())
        is java.math.BigInteger -> StringValue(input.toString())
        //is Map.Entry<*, *> -> {}
        is List<*> -> {     //EList
            val iterator = input.iterator()
            ListValue(*Array(input.size) { value(iterator.next()) })
        }
        is Array<*> -> ListValue(*Array(input.size) { i -> value(input[i]) })
        //is EEnum = EClass = EDataType  //not in model instance; is not a direct attr type
        else -> NullValue.NULL //not persistable value
        //else -> Values.value(input)
    }
}
