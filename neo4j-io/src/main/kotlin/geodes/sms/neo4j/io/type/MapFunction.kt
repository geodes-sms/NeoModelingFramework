package geodes.sms.neo4j.io.type

import org.neo4j.driver.Value
import java.math.BigDecimal
import java.math.BigInteger
import java.time.ZonedDateTime

abstract class MapFunction<T: Any> {
    //internal abstract val mapFunc: (Value) -> T
    internal abstract fun apply(v: Value): T
}

object AsString : MapFunction<String>() {
    //override val mapFunc: (Value) -> String = { it.asString() }
    override fun apply(v: Value): String {
        return v.asString()
    }
}

object AsInt : MapFunction<Int>() {
    override fun apply(v: Value) = v.asInt()
}

object AsLong : MapFunction<Long>() {
    override fun apply(v: Value) = v.asLong()
}

object AsShort : MapFunction<Short>() {
    override fun apply(v: Value) = v.asNumber().toShort()
}

object AsByte : MapFunction<Byte>() {
    override fun apply(v: Value) = v.asNumber().toByte()
}

object AsByteArray : MapFunction<ByteArray>() {
    override fun apply(v: Value) = v.asByteArray()
}

object AsBoolean : MapFunction<Boolean>() {
    override fun apply(v: Value) = v.asBoolean()
}

object AsChar : MapFunction<Char>() {
    override fun apply(v: Value) = v.asString()[0]
}

object AsDouble : MapFunction<Double>() {
    override fun apply(v: Value) = v.asDouble()
}

object AsFloat : MapFunction<Float>() {
    override fun apply(v: Value) = v.asFloat()
}

object AsBigDecimal: MapFunction<BigDecimal>() {
    override fun apply(v: Value): BigDecimal {
        return BigDecimal(v.asString())
    }
}

object AsBigInteger: MapFunction<BigInteger>() {
    override fun apply(v: Value): BigInteger {
        return BigInteger(v.asString())
    }
}

object AsZonedDateTime: MapFunction<ZonedDateTime>() {
    override fun apply(v: Value) = v.asZonedDateTime()
}

object AsObject: MapFunction<Any>() {
    override fun apply(v: Value) = v.asObject()
}

class AsList<T: Any>(val f: MapFunction<T>): MapFunction<List<T>>() {
    override fun apply(v: Value) = v.asList { f.apply(it) }
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