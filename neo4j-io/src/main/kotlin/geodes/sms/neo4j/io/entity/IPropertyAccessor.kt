package geodes.sms.neo4j.io.entity

import java.math.BigDecimal
import java.math.BigInteger
import java.time.ZonedDateTime

interface IPropertyAccessor {
    fun getPropertyAsString(name: String): String?
    fun getPropertyAsInt(name: String): Int?
    fun getPropertyAsLong(name: String): Long?
    fun getPropertyAsShort(name: String): Short?
    fun getPropertyAsBoolean(name: String): Boolean?
    fun getPropertyAsByte(name: String): Byte?
    fun getPropertyAsByteArray(name: String): ByteArray?
    fun getPropertyAsChar(name: String): Char?
    fun getPropertyAsDouble(name: String): Double?
    fun getPropertyAsFloat(name: String): Float?
    fun <T: Enum<T>> getPropertyAsEnum(name: String): T?
    fun getPropertyAsDate(name: String): ZonedDateTime?
    fun getPropertyAsBigDecimal(name: String): BigDecimal?
    fun getPropertyAsBigInteger(name: String): BigInteger?
    fun getPropertyAsAny(name: String): Any?

    fun putProperty(name: String, value: String?)
    fun putProperty(name: String, value: Int?)
    fun putProperty(name: String, value: Long?)
    fun putProperty(name: String, value: Short?)
    fun putProperty(name: String, value: Boolean?)
    fun putProperty(name: String, value: Byte?)
    fun putProperty(name: String, value: ByteArray?)
    fun putProperty(name: String, value: Char?)
    fun putProperty(name: String, value: Double?)
    fun putProperty(name: String, value: Float?)
    fun <T: Enum<T>> putProperty(name: String, value: Enum<T>?)
    fun putProperty(name: String, value: ZonedDateTime?)
    fun putProperty(name: String, value: BigDecimal?)
    fun putProperty(name: String, value: BigInteger?)

    fun putUniqueProperty(name: String, value: String)
    fun putUniqueProperty(name: String, value: Int)
    fun putUniqueProperty(name: String, value: Long)
    fun putUniqueProperty(name: String, value: Short)
    fun putUniqueProperty(name: String, value: Boolean)
    fun putUniqueProperty(name: String, value: Byte)
    fun putUniqueProperty(name: String, value: ByteArray)
    fun putUniqueProperty(name: String, value: Char)
    fun putUniqueProperty(name: String, value: Double)
    fun putUniqueProperty(name: String, value: Float)
    fun <T: Enum<T>> putUniqueProperty(name: String, value: Enum<T>)
    fun putUniqueProperty(name: String, value: ZonedDateTime)
    fun putUniqueProperty(name: String, value: BigDecimal)
    fun putUniqueProperty(name: String, value: BigInteger)

    fun removeProperty(name: String)
}