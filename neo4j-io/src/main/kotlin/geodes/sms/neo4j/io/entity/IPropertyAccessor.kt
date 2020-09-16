package geodes.sms.neo4j.io.entity

import geodes.sms.neo4j.io.type.MapFunction
import org.neo4j.driver.Value
import java.math.BigDecimal
import java.math.BigInteger
import java.time.ZonedDateTime

interface IPropertyAccessor {
    fun <T: Any> getProperty(name: String, mapFunc: MapFunction<T>): T?

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
    fun <T> putProperty(name: String, value: List<T>?)

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