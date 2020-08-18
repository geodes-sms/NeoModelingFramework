package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.entity.IPropertyAccessor
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.*
import java.math.BigDecimal
import java.math.BigInteger
import java.time.ZonedDateTime


//common for node and relationship
internal abstract class PropertyAccessor(
    protected val propsDiff: HashMap<String, Value>
) : IPropertyAccessor {
    protected val props: HashMap<String, Any> = hashMapOf()

    //different for node and relationship
    protected abstract fun readPropertyFromDB(name: String): Value
    protected abstract fun putProperty(name: String, value: Value)
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
        override fun putUniqueProperty(name: String, value: String) {
            if (isPropertyUnique(name, value, StringValue(value)))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun putUniqueProperty(name: String, value: Int) {
            if (isPropertyUnique(name, value, IntegerValue(value.toLong())))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun putUniqueProperty(name: String, value: Long) {
            if (isPropertyUnique(name, value, IntegerValue(value)))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun putUniqueProperty(name: String, value: Short) {
            if (isPropertyUnique(name, value, IntegerValue(value.toLong())))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun putUniqueProperty(name: String, value: Boolean) {
            if (isPropertyUnique(name, value, BooleanValue.fromBoolean(value)))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun putUniqueProperty(name: String, value: Byte) {
            if (isPropertyUnique(name, value, IntegerValue(value.toLong())))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun putUniqueProperty(name: String, value: ByteArray) {
            if (isPropertyUnique(name, value, BytesValue(value)))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun putUniqueProperty(name: String, value: Char) {
            if (isPropertyUnique(name, value, StringValue("$value")))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun putUniqueProperty(name: String, value: Double) {
            if (isPropertyUnique(name, value, FloatValue(value)))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun putUniqueProperty(name: String, value: Float) {
            if (isPropertyUnique(name, value, FloatValue(value.toDouble())))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun <T : Enum<T>> putUniqueProperty(name: String, value: Enum<T>) {
            if (isPropertyUnique(name, value, StringValue(value.name)))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun putUniqueProperty(name: String, value: ZonedDateTime) {
            if (isPropertyUnique(name, value, DateTimeValue(value)))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun putUniqueProperty(name: String, value: BigDecimal) {
            if (isPropertyUnique(name, value, StringValue(value.toString())))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }

        override fun putUniqueProperty(name: String, value: BigInteger) {
            if (isPropertyUnique(name, value, StringValue(value.toString())))
                putProperty(name, value)
            else throw Exception("Property already exists in DB")
        }
    }


    //works ony with cache
    protected open inner class NewPropertyAccessor : ActivePropertyAccessor() {
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

        override fun getPropertyAsAny(name: String): Any? {
            return props[name]
        }

        override fun removeProperty(name: String) {
            propsDiff[name] = NullValue.NULL
            props.remove(name)
        }
    }


    protected open inner class PersistedPropertyAccessor : ActivePropertyAccessor() {
        override fun putProperty(name: String, value: String?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, StringValue(value))
            }
        }

        override fun putProperty(name: String, value: Int?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, IntegerValue(value.toLong()))
            }
        }

        override fun putProperty(name: String, value: Long?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, IntegerValue(value))
            }
        }

        override fun putProperty(name: String, value: Short?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, IntegerValue(value.toLong()))
            }
        }

        override fun putProperty(name: String, value: Boolean?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, BooleanValue.fromBoolean(value))
            }
        }

        override fun putProperty(name: String, value: Byte?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, IntegerValue(value.toLong()))
            }
        }

        override fun putProperty(name: String, value: ByteArray?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, BytesValue(value))
            }
        }

        override fun putProperty(name: String, value: Char?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, StringValue("$value"))
            }
        }

        override fun putProperty(name: String, value: Double?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, FloatValue(value))
            }
        }

        override fun putProperty(name: String, value: Float?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, FloatValue(value.toDouble()))
            }
        }

        override fun <T : Enum<T>> putProperty(name: String, value: Enum<T>?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, StringValue(value.name))
            }
        }

        override fun putProperty(name: String, value: ZonedDateTime?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, DateTimeValue(value))
            }
        }

        override fun putProperty(name: String, value: BigDecimal?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, StringValue(value.toString()))
            }
        }

        override fun putProperty(name: String, value: BigInteger?) {
            if (value == null) removeProperty(name)
            else {
                props[name] = value
                putProperty(name, StringValue(value.toString()))
            }
        }

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
            return if (res != null) res as? BigDecimal else read(name) {
                BigDecimal(readPropertyFromDB(name).asString())
            }
        }

        override fun getPropertyAsBigInteger(name: String): BigInteger? {
            val res = props[name]
            return if (res != null) res as? BigInteger else read(name) {
                BigInteger(readPropertyFromDB(name).asString())
            }
        }

        override fun getPropertyAsAny(name: String): Any? {
            return props.getOrPut(name) { readPropertyFromDB(name).asObject() }
        }

        override fun removeProperty(name: String) {
            putProperty(name, NullValue.NULL)
            props.remove(name)
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

//    protected object RemovedPropertyAccessor : NotActivePropertyAccessor(
//        "Entity was removed. Cannot perform operation on removed entity"
//    )
//
//    protected object DetachedPropertyAccessor : NotActivePropertyAccessor(
//        "Entity was detached. Cannot perform operation on detached (unloaded) entity"
//    )
}