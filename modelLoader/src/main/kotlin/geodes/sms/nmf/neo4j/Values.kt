package geodes.sms.nmf.neo4j

import org.eclipse.emf.ecore.EEnumLiteral
import org.neo4j.driver.internal.value.*
import org.neo4j.driver.Value
import java.time.ZoneId
import java.time.ZonedDateTime

object Values {

    fun value(input: Any?): Value = when (input) {
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
        is List<*> -> value(input)    //EList
        //is EEnum = EClass = EDataType  //not in model instance; is not a direct attr type
        else -> NullValue.NULL //not persistable value
    }

    fun value(input: String) = StringValue(input)
    fun value(input: Char) = StringValue(input.toString())
    fun value(input: Boolean): BooleanValue = BooleanValue.fromBoolean(input)
    fun value(input: ByteArray) = BytesValue(input)

    fun value(input: Long) = IntegerValue(input)
    fun value(input: Int) = IntegerValue(input.toLong())
    fun value(input: Short) = IntegerValue(input.toLong())
    fun value(input: Byte) = IntegerValue(input.toLong())

    fun value(input: Double) = FloatValue(input)
    fun value(input: Float)  = FloatValue(input.toDouble())

    fun value(input: java.math.BigDecimal) = StringValue(input.toString())
    fun value(input: java.math.BigInteger) = StringValue(input.toString())
    fun value(input: EEnumLiteral) = StringValue(input.literal)
    fun value(input: java.util.Date) = DateTimeValue(ZonedDateTime.ofInstant(input.toInstant(), ZoneId.systemDefault()))
    fun value(input: List<*>) = ListValue(*Array<Value?>(input.size) { i -> value(input[i]) })
}

