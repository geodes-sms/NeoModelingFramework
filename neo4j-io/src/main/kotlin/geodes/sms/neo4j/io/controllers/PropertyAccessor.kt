package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.entity.IPropertyAccessor
import geodes.sms.neo4j.io.type.MapFunction
import org.neo4j.driver.Value
import org.neo4j.driver.Values
import org.neo4j.driver.internal.value.*
import java.math.BigDecimal
import java.math.BigInteger
import java.time.ZonedDateTime

//common for node and relationship entities
internal abstract class PropertyAccessor(
    protected val propsDiff: HashMap<String, Value>
) : IPropertyAccessor {
    protected val props: HashMap<String, Any> = hashMapOf() //for primitives props only !!!

    //different for node and relationship
    protected abstract fun readPropertyFromDB(name: String): Value
    protected abstract fun updateEntity()
    protected abstract fun isPropertyUnique(name: String, value: Any, dbValue: Value): Boolean
    protected abstract val state: IPropertyAccessor

    override fun <T : Any> getProperty(name: String, mapFunc: MapFunction<T>) = state.getProperty(name, mapFunc)
    override fun putProperty(name: String, value: String) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Int) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Long) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Short) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Boolean) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Byte) = state.putProperty(name, value)
    override fun putProperty(name: String, value: ByteArray) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Char) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Double) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Float) = state.putProperty(name, value)
    override fun <T : Enum<T>> putProperty(name: String, value: Enum<T>) = state.putProperty(name, value)
    override fun putProperty(name: String, value: ZonedDateTime) = state.putProperty(name, value)
    override fun putProperty(name: String, value: BigDecimal) = state.putProperty(name, value)
    override fun putProperty(name: String, value: BigInteger) = state.putProperty(name, value)
    override fun <T> putProperty(name: String, value: List<T>) = state.putProperty(name, value)
    override fun putUniqueProperty(name: String, value: String) = state.putUniqueProperty(name, value)
    override fun putUniqueProperty(name: String, value: Int) = state.putUniqueProperty(name, value)
    override fun putUniqueProperty(name: String, value: Long) = state.putUniqueProperty(name, value)
    override fun putUniqueProperty(name: String, value: Short) = state.putUniqueProperty(name, value)
    override fun putUniqueProperty(name: String, value: Boolean) = state.putUniqueProperty(name, value)
    override fun putUniqueProperty(name: String, value: Byte) = state.putUniqueProperty(name, value)
    override fun putUniqueProperty(name: String, value: ByteArray) = state.putUniqueProperty(name, value)
    override fun putUniqueProperty(name: String, value: Char) = state.putUniqueProperty(name, value)
    override fun putUniqueProperty(name: String, value: Double) = state.putUniqueProperty(name, value)
    override fun putUniqueProperty(name: String, value: Float) = state.putUniqueProperty(name, value)
    override fun <T : Enum<T>> putUniqueProperty(name: String, value: Enum<T>) = state.putUniqueProperty(name, value)
    override fun putUniqueProperty(name: String, value: ZonedDateTime) = state.putUniqueProperty(name, value)
    override fun putUniqueProperty(name: String, value: BigDecimal) = state.putUniqueProperty(name, value)
    override fun putUniqueProperty(name: String, value: BigInteger) = state.putUniqueProperty(name, value)
    override fun removeProperty(name: String) = state.removeProperty(name)

    protected abstract inner class ActivePropertyAccessor : IPropertyAccessor {
        private fun putUnique(name: String, value: Any, neo4jValue: Value) {
            if (isPropertyUnique(name, value, neo4jValue)) {
                props[name] = value
                propsDiff[name] = neo4jValue
            }
            else throw Exception("Property must be unique. Property with '$name' : $value already exists in DB")
        }

        override fun putProperty(name: String, value: String) {
            props[name] = value
            propsDiff[name] = StringValue(value)
        }

        override fun putProperty(name: String, value: Int) {
            props[name] = value
            propsDiff[name] = IntegerValue(value.toLong())
        }

        override fun putProperty(name: String, value: Long) {
            props[name] = value
            propsDiff[name] = IntegerValue(value)
        }

        override fun putProperty(name: String, value: Short) {
            props[name] = value
            propsDiff[name] = IntegerValue(value.toLong())
        }

        override fun putProperty(name: String, value: Boolean) {
            props[name] = value
            propsDiff[name] = BooleanValue.fromBoolean(value)
        }

        override fun putProperty(name: String, value: Byte) {
            props[name] = value
            propsDiff[name] = IntegerValue(value.toLong())
        }

        override fun putProperty(name: String, value: ByteArray) {
            props[name] = value
            propsDiff[name] = BytesValue(value)
        }

        override fun putProperty(name: String, value: Char) {
            props[name] = value
            propsDiff[name] = StringValue("$value")
        }

        override fun putProperty(name: String, value: Double) {
            props[name] = value
            propsDiff[name] = FloatValue(value)
        }

        override fun putProperty(name: String, value: Float) {
            props[name] = value
            propsDiff[name] = FloatValue(value.toDouble())
        }

        override fun <T : Enum<T>> putProperty(name: String, value: Enum<T>) {
            props[name] = value
            propsDiff[name] = StringValue(value.name)
        }

        override fun putProperty(name: String, value: ZonedDateTime) {
            props[name] = value
            propsDiff[name] = DateTimeValue(value)
        }

        override fun putProperty(name: String, value: BigDecimal) {
            props[name] = value
            propsDiff[name] = StringValue(value.toString())
        }

        override fun putProperty(name: String, value: BigInteger) {
            props[name] = value
            propsDiff[name] = StringValue(value.toString())
        }

        override fun <T> putProperty(name: String, value: List<T>) {
            props[name] = value
            propsDiff[name] = Values.value(value)
        }

        override fun putUniqueProperty(name: String, value: String) {
            putUnique(name, value, StringValue(value))
        }

        override fun putUniqueProperty(name: String, value: Int) {
            putUnique(name, value, IntegerValue(value.toLong()))
        }

        override fun putUniqueProperty(name: String, value: Long) {
            putUnique(name, value, IntegerValue(value))
        }

        override fun putUniqueProperty(name: String, value: Short) {
            putUnique(name, value, IntegerValue(value.toLong()))
        }

        override fun putUniqueProperty(name: String, value: Boolean) {
            putUnique(name, value, BooleanValue.fromBoolean(value))
        }

        override fun putUniqueProperty(name: String, value: Byte) {
            putUnique(name, value, IntegerValue(value.toLong()))
        }

        override fun putUniqueProperty(name: String, value: ByteArray) {
            putUnique(name, value, BytesValue(value))
        }

        override fun putUniqueProperty(name: String, value: Char) {
            putUnique(name, value, StringValue("$value"))
        }

        override fun putUniqueProperty(name: String, value: Double) {
            putUnique(name, value, FloatValue(value))
        }

        override fun putUniqueProperty(name: String, value: Float) {
            putUnique(name, value, FloatValue(value.toDouble()))
        }

        override fun <T : Enum<T>> putUniqueProperty(name: String, value: Enum<T>) {
            putUnique(name, value, StringValue(value.name))
        }

        override fun putUniqueProperty(name: String, value: ZonedDateTime) {
            putUnique(name, value, DateTimeValue(value))
        }

        override fun putUniqueProperty(name: String, value: BigDecimal) {
            putUnique(name, value, StringValue(value.toString()))
        }

        override fun putUniqueProperty(name: String, value: BigInteger) {
            putUnique(name, value, StringValue(value.toString()))
        }

        override fun removeProperty(name: String) {
            propsDiff[name] = NullValue.NULL
            props.remove(name)
        }
    }

    //works ony with cache
    protected open inner class NewPropertyAccessor : ActivePropertyAccessor() {
        override fun <T : Any> getProperty(name: String, mapFunc: MapFunction<T>): T? {
            return props[name] as? T
        }
    }

    protected open inner class PersistedPropertyAccessor: ActivePropertyAccessor() {
        override fun <T : Any> getProperty(name: String, mapFunc: MapFunction<T>): T? {
            val resCached = props[name]
            return if (resCached != null) resCached as? T else {
                val res = readPropertyFromDB(name)
                return if (res.isNull) null else {
                    val value = mapFunc.apply(res)
                    props[name] = value
                    value
                }
            }
        }

        override fun putProperty(name: String, value: String) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Int) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Long) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Short) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Boolean) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Byte) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: ByteArray) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Char) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Double) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Float) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun <T : Enum<T>> putProperty(name: String, value: Enum<T>) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: ZonedDateTime) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: BigDecimal) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: BigInteger) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun <T> putProperty(name: String, value: List<T>) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: String) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: Int) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: Long) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: Short) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: Boolean) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: Byte) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: ByteArray) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: Char) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: Double) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: Float) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun <T : Enum<T>> putUniqueProperty(name: String, value: Enum<T>) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: ZonedDateTime) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: BigDecimal) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun putUniqueProperty(name: String, value: BigInteger) {
            updateEntity()
            super.putUniqueProperty(name, value)
        }

        override fun removeProperty(name: String) {
            updateEntity()
            super.removeProperty(name)
        }
    }

    protected abstract class NotActivePropertyAccessor(private val msg: String) : IPropertyAccessor {
        override fun <T : Any> getProperty(name: String, mapFunc: MapFunction<T>) = throw Exception(msg)
        override fun putProperty(name: String, value: String) = throw Exception(msg)
        override fun putProperty(name: String, value: Int) = throw Exception(msg)
        override fun putProperty(name: String, value: Long) = throw Exception(msg)
        override fun putProperty(name: String, value: Short) = throw Exception(msg)
        override fun putProperty(name: String, value: Boolean) = throw Exception(msg)
        override fun putProperty(name: String, value: Byte) = throw Exception(msg)
        override fun putProperty(name: String, value: ByteArray) = throw Exception(msg)
        override fun putProperty(name: String, value: Char) = throw Exception(msg)
        override fun putProperty(name: String, value: Double) = throw Exception(msg)
        override fun putProperty(name: String, value: Float) = throw Exception(msg)
        override fun <T : Enum<T>> putProperty(name: String, value: Enum<T>) = throw Exception(msg)
        override fun putProperty(name: String, value: ZonedDateTime) = throw Exception(msg)
        override fun putProperty(name: String, value: BigDecimal) = throw Exception(msg)
        override fun putProperty(name: String, value: BigInteger) = throw Exception(msg)
        override fun <T> putProperty(name: String, value: List<T>) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: String) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: Int) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: Long) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: Short) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: Boolean) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: Byte) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: ByteArray) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: Char) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: Double) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: Float) = throw Exception(msg)
        override fun <T : Enum<T>> putUniqueProperty(name: String, value: Enum<T>) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: ZonedDateTime) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: BigDecimal) = throw Exception(msg)
        override fun putUniqueProperty(name: String, value: BigInteger) = throw Exception(msg)
        override fun removeProperty(name: String) = throw Exception(msg)
    }
}