package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.entity.IPropertyAccessor
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
    protected val props: HashMap<String, Any> = hashMapOf()

    //different for node and relationship
    protected abstract fun readPropertyFromDB(name: String): Value
    protected abstract fun updateEntity()
    protected abstract fun isPropertyUnique(name: String, value: Any, dbValue: Value): Boolean
    protected abstract val state: IPropertyAccessor

    override fun getPropertyAsString(name: String): String? = state.getPropertyAsString(name)
    override fun getPropertyAsInt(name: String): Int? = state.getPropertyAsInt(name)
    override fun getPropertyAsLong(name: String): Long? = state.getPropertyAsLong(name)
    override fun getPropertyAsShort(name: String): Short? = state.getPropertyAsShort(name)
    override fun getPropertyAsBoolean(name: String): Boolean? = state.getPropertyAsBoolean(name)
    override fun getPropertyAsByte(name: String): Byte? = state.getPropertyAsByte(name)
    override fun getPropertyAsByteArray(name: String): ByteArray? = state.getPropertyAsByteArray(name)
    override fun getPropertyAsChar(name: String): Char? = state.getPropertyAsChar(name)
    override fun getPropertyAsDouble(name: String): Double? = state.getPropertyAsDouble(name)
    override fun getPropertyAsFloat(name: String): Float? = state.getPropertyAsFloat(name)
    override fun <T : Enum<T>> getPropertyAsEnum(name: String): T? =  state.getPropertyAsEnum<T>(name)
    override fun getPropertyAsDate(name: String): ZonedDateTime? = state.getPropertyAsDate(name)
    override fun getPropertyAsBigDecimal(name: String): BigDecimal? = state.getPropertyAsBigDecimal(name)
    override fun getPropertyAsBigInteger(name: String): BigInteger? = state.getPropertyAsBigInteger(name)
    override fun <T> getPropertyAsListOf(name: String): List<T>? = state.getPropertyAsListOf(name)
    override fun getPropertyAsAny(name: String): Any? = state.getPropertyAsAny(name)
    override fun putProperty(name: String, value: String?) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Int?) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Long?) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Short?) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Boolean?) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Byte?) = state.putProperty(name, value)
    override fun putProperty(name: String, value: ByteArray?) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Char?) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Double?) = state.putProperty(name, value)
    override fun putProperty(name: String, value: Float?) = state.putProperty(name, value)
    override fun <T : Enum<T>> putProperty(name: String, value: Enum<T>?) = state.putProperty(name, value)
    override fun putProperty(name: String, value: ZonedDateTime?) = state.putProperty(name, value)
    override fun putProperty(name: String, value: BigDecimal?) = state.putProperty(name, value)
    override fun putProperty(name: String, value: BigInteger?) = state.putProperty(name, value)
    override fun <T> putProperty(name: String, value: List<T>?) = state.putProperty(name, value)
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
            else throw Exception("Property already exists in DB")
        }

        override fun putProperty(name: String, value: String?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = StringValue(value)
            }
        }

        override fun putProperty(name: String, value: Int?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = IntegerValue(value.toLong())
            }
        }

        override fun putProperty(name: String, value: Long?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = IntegerValue(value)
            }
        }

        override fun putProperty(name: String, value: Short?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = IntegerValue(value.toLong())
            }
        }

        override fun putProperty(name: String, value: Boolean?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = BooleanValue.fromBoolean(value)
            }
        }

        override fun putProperty(name: String, value: Byte?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = IntegerValue(value.toLong())
            }
        }

        override fun putProperty(name: String, value: ByteArray?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = BytesValue(value)
            }
        }

        override fun putProperty(name: String, value: Char?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = StringValue("$value")
            }
        }

        override fun putProperty(name: String, value: Double?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = FloatValue(value)
            }
        }

        override fun putProperty(name: String, value: Float?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = FloatValue(value.toDouble())
            }
        }

        override fun <T : Enum<T>> putProperty(name: String, value: Enum<T>?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = StringValue(value.name)
            }
        }

        override fun putProperty(name: String, value: ZonedDateTime?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = DateTimeValue(value)
            }
        }

        override fun putProperty(name: String, value: BigDecimal?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = StringValue(value.toString())
            }
        }

        override fun putProperty(name: String, value: BigInteger?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = StringValue(value.toString())
            }
        }

        override fun <T> putProperty(name: String, value: List<T>?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                propsDiff[name] = Values.value(value)
            }
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
        override fun getPropertyAsString(name: String): String? {
            return props[name] as? String
        }

        override fun getPropertyAsInt(name: String): Int? {
            return props[name] as? Int
        }

        override fun getPropertyAsLong(name: String): Long? {
            return props[name] as? Long
        }

        override fun getPropertyAsShort(name: String): Short? {
            return props[name] as? Short
        }

        override fun getPropertyAsBoolean(name: String): Boolean? {
            return props[name] as? Boolean
        }

        override fun getPropertyAsByte(name: String): Byte? {
            return props[name] as? Byte
        }

        override fun getPropertyAsByteArray(name: String): ByteArray? {
            return props[name] as? ByteArray
        }

        override fun getPropertyAsChar(name: String): Char? {
            return props[name] as? Char
        }

        override fun getPropertyAsDouble(name: String): Double? {
            return props[name] as? Double
        }

        override fun getPropertyAsFloat(name: String): Float? {
            return props[name] as? Float
        }

        override fun <T : Enum<T>> getPropertyAsEnum(name: String): T? {
            return props[name] as? T
        }

        override fun getPropertyAsDate(name: String): ZonedDateTime? {
            return props[name] as? ZonedDateTime
        }

        override fun getPropertyAsBigDecimal(name: String): BigDecimal? {
            return props[name] as? BigDecimal
        }

        override fun getPropertyAsBigInteger(name: String): BigInteger? {
            return props[name] as? BigInteger
        }

        override fun <T> getPropertyAsListOf(name: String): List<T>? {
            return props[name] as? List<T>
        }

        override fun getPropertyAsAny(name: String): Any? {
            return props[name]
        }
    }

    protected open inner class PersistedPropertyAccessor: ModifiedPropertyAccessor() {
        override fun putProperty(name: String, value: String?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Int?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Long?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Short?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Boolean?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Byte?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: ByteArray?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Char?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Double?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: Float?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun <T : Enum<T>> putProperty(name: String, value: Enum<T>?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: ZonedDateTime?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: BigDecimal?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun putProperty(name: String, value: BigInteger?) {
            updateEntity()
            super.putProperty(name, value)
        }

        override fun <T> putProperty(name: String, value: List<T>?) {
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

    protected open inner class ModifiedPropertyAccessor : ActivePropertyAccessor() {
        private inline fun <T: Any> read(name: String, mapFunction: (Value) -> T): T? {
            val res = readPropertyFromDB(name)
            return if (res.isNull) null else {
                val value = mapFunction(res)
                props[name] = value
                value
            }
        }

        override fun getPropertyAsString(name: String): String? {
            val res = props[name]
            return if (res != null) res as? String else read(name) { it.asString() }
        }

        override fun getPropertyAsInt(name: String): Int? {
            val res = props[name]
            return if (res != null) res as? Int else read(name) { it.asInt() }
        }

        override fun getPropertyAsLong(name: String): Long? {
            val res = props[name]
            return if (res != null) res as? Long else read(name) { it.asLong() }
        }

        override fun getPropertyAsShort(name: String): Short? {
            val res = props[name]
            return if (res != null) res as? Short else read(name) { it.asNumber().toShort() }
        }

        override fun getPropertyAsBoolean(name: String): Boolean? {
            val res = props[name]
            return if (res != null) res as? Boolean else read(name) { it.asBoolean() }
        }

        override fun getPropertyAsByte(name: String): Byte? {
            val res = props[name]
            return if (res != null) res as? Byte else read(name) { it.asNumber().toByte() }
        }

        override fun getPropertyAsByteArray(name: String): ByteArray? {
            val res = props[name]
            return if (res != null) res as? ByteArray else read(name) { it.asByteArray() }
        }

        override fun getPropertyAsChar(name: String): Char? {
            val res = props[name]
            return if (res != null) res as? Char else read(name) { it.asString()[0] }
        }

        override fun getPropertyAsDouble(name: String): Double? {
            val res = props[name]
            return if (res != null) res as? Double else read(name) { it.asDouble() }
        }

        override fun getPropertyAsFloat(name: String): Float? {
            val res = props[name]
            return if (res != null) res as? Float else read(name) { it.asFloat() }
        }

        override fun <T : Enum<T>> getPropertyAsEnum(name: String): T? {
            TODO()
//            return props.getOrPut(name) {
//                val res = readPropertyFromDB(name).asString()
//                enumValueOf<T>(res)
//            } as? T
        }

        override fun getPropertyAsDate(name: String): ZonedDateTime? {
            val res = props[name]
            return if (res != null) res as? ZonedDateTime else read(name) { it.asZonedDateTime() }
        }

        override fun getPropertyAsBigDecimal(name: String): BigDecimal? {
            val res = props[name]
            return if (res != null) res as? BigDecimal else read(name) { BigDecimal(it.asString()) }
        }

        override fun getPropertyAsBigInteger(name: String): BigInteger? {
            val res = props[name]
            return if (res != null) res as? BigInteger else read(name) { BigInteger(it.asString()) }
        }

        override fun <T> getPropertyAsListOf(name: String): List<T>? {
            val res = props[name]
            return if (res != null) res as? List<T> else read(name) {
                it.asList { v -> v.asObject() as T }
            }
        }

        override fun getPropertyAsAny(name: String): Any? {
            return props.getOrPut(name) { readPropertyFromDB(name).asObject() }
        }
    }

    protected abstract class NotActivePropertyAccessor(private val msg: String) : IPropertyAccessor {
        override fun getPropertyAsString(name: String) = throw Exception(msg)
        override fun getPropertyAsInt(name: String) = throw Exception(msg)
        override fun getPropertyAsLong(name: String) = throw Exception(msg)
        override fun getPropertyAsShort(name: String) = throw Exception(msg)
        override fun getPropertyAsBoolean(name: String) = throw Exception(msg)
        override fun getPropertyAsByte(name: String) = throw Exception(msg)
        override fun getPropertyAsByteArray(name: String) = throw Exception(msg)
        override fun getPropertyAsChar(name: String) = throw Exception(msg)
        override fun getPropertyAsDouble(name: String) = throw Exception(msg)
        override fun getPropertyAsFloat(name: String) = throw Exception(msg)
        override fun <T : Enum<T>> getPropertyAsEnum(name: String) = throw Exception(msg)
        override fun getPropertyAsDate(name: String) = throw Exception(msg)
        override fun getPropertyAsBigDecimal(name: String) = throw Exception(msg)
        override fun getPropertyAsBigInteger(name: String) = throw Exception(msg)
        override fun <T> getPropertyAsListOf(name: String) = throw Exception(msg)
        override fun getPropertyAsAny(name: String) = throw Exception(msg)
        override fun putProperty(name: String, value: String?) = throw Exception(msg)
        override fun putProperty(name: String, value: Int?) = throw Exception(msg)
        override fun putProperty(name: String, value: Long?) = throw Exception(msg)
        override fun putProperty(name: String, value: Short?) = throw Exception(msg)
        override fun putProperty(name: String, value: Boolean?) = throw Exception(msg)
        override fun putProperty(name: String, value: Byte?) = throw Exception(msg)
        override fun putProperty(name: String, value: ByteArray?) = throw Exception(msg)
        override fun putProperty(name: String, value: Char?) = throw Exception(msg)
        override fun putProperty(name: String, value: Double?) = throw Exception(msg)
        override fun putProperty(name: String, value: Float?) = throw Exception(msg)
        override fun <T : Enum<T>> putProperty(name: String, value: Enum<T>?) = throw Exception(msg)
        override fun putProperty(name: String, value: ZonedDateTime?) = throw Exception(msg)
        override fun putProperty(name: String, value: BigDecimal?) = throw Exception(msg)
        override fun putProperty(name: String, value: BigInteger?) = throw Exception(msg)
        override fun <T> putProperty(name: String, value: List<T>?) = throw Exception(msg)
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