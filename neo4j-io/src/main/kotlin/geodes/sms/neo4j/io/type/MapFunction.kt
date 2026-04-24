package geodes.sms.neo4j.io.type

import org.neo4j.driver.Value
import org.neo4j.driver.Values
import org.neo4j.driver.internal.value.*
import java.math.BigDecimal
import java.math.BigInteger
import java.time.ZonedDateTime

abstract class MapFunction<T: Any> {
    //internal abstract val mapFunc: (Value) -> T
    internal abstract fun apply(value: Value): T
    //internal abstract fun apply(value: T): Value
}

object AsString : MapFunction<String>() {
    //override val mapFunc: (Value) -> String = { it.asString() }
    override fun apply(value: Value): String {
        return value.asString()
    }
}

object AsInt : MapFunction<Int>() {
    override fun apply(value: Value) = value.asInt()
    //override fun apply(value: Int) = IntegerValue(value.toLong())
}

object AsLong : MapFunction<Long>() {
    override fun apply(value: Value) = value.asLong()
    //override fun apply(value: Long) = IntegerValue(value)
}

object AsShort : MapFunction<Short>() {
    override fun apply(value: Value) = value.asNumber().toShort()
    //override fun apply(value: Short) = IntegerValue(value.toLong())
}

object AsByte : MapFunction<Byte>() {
    override fun apply(value: Value) = value.asNumber().toByte()
    //override fun apply(value: Byte) = IntegerValue(value.toLong())
}

object AsByteArray : MapFunction<ByteArray>() {
    override fun apply(value: Value) = value.asByteArray()
    //override fun apply(value: ByteArray) = BytesValue(value)
}

object AsBoolean : MapFunction<Boolean>() {
    override fun apply(value: Value) = value.asBoolean()
    //override fun apply(value: Boolean) = BooleanValue.fromBoolean(value)
}

object AsChar : MapFunction<Char>() {
    override fun apply(value: Value) = value.asString()[0]
    //override fun apply(value: Char) = StringValue("$value")
}

object AsDouble : MapFunction<Double>() {
    override fun apply(value: Value) = value.asDouble()
    //override fun apply(value: Double) = FloatValue(value)
}

object AsFloat : MapFunction<Float>() {
    override fun apply(value: Value) = value.asFloat()
    //override fun apply(value: Float) = FloatValue(value.toDouble())
}

object AsBigDecimal: MapFunction<BigDecimal>() {
    override fun apply(value: Value) = BigDecimal(value.asString())
    //override fun apply(value: BigDecimal) = StringValue(value.toString())
}

object AsBigInteger: MapFunction<BigInteger>() {
    override fun apply(value: Value) = BigInteger(value.asString())
    //override fun apply(value: BigInteger) = StringValue(value.toString())
}

object AsZonedDateTime: MapFunction<ZonedDateTime>() {
    override fun apply(value: Value) = value.asZonedDateTime()
    //override fun apply(value: ZonedDateTime) = DateTimeValue(value)
}

object AsObject: MapFunction<Any>() {
    override fun apply(value: Value) = value.asObject()
}

class AsList<T: Any>(val f: MapFunction<T>): MapFunction<List<T>>() {
    override fun apply(value: Value) = value.asList { f.apply(it) }
    //override fun apply(value: List<T>) = Values.value(value)
}

//class OfEnum<T: Enum<T>>(val e: T) : MapFunction<T>() {
//    inline fun <reified T> r(name: String) {
//        enumValueOf<T>(name)
//    }
//
//    override fun apply(v: Value): T {
//        v.asString()
//        return e
//    }
//}