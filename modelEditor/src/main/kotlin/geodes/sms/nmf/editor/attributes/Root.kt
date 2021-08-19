package geodes.sms.nmf.editor.attributes

import geodes.sms.neo4j.io.entity.INodeEntity

interface Root : INodeEntity {
	fun setBigDecimal(v: java.math.BigDecimal?)
	fun getBigDecimal(): java.math.BigDecimal?
	fun setBigInteger(v: java.math.BigInteger?)
	fun getBigInteger(): java.math.BigInteger?
	fun setBoolPrimitive(v: Boolean?)
	fun getBoolPrimitive(): Boolean?
	fun setBoolObj(v: Boolean?)
	fun getBoolObj(): Boolean?
	fun setBytePrimitive(v: Byte?)
	fun getBytePrimitive(): Byte?
	fun setByteObj(v: Byte?)
	fun getByteObj(): Byte?
	fun setByteArray(v: ByteArray?)
	fun getByteArray(): ByteArray?
	fun setByteArrayDefVal(v: ByteArray?)
	fun getByteArrayDefVal(): ByteArray?
	fun setCharPrimitive(v: Char?)
	fun getCharPrimitive(): Char?
	fun setCharObj(v: Char?)
	fun getCharObj(): Char?
	fun setCharObjDefVal(v: Char?)
	fun getCharObjDefVal(): Char?
	fun setDoublePrimitive(v: Double?)
	fun getDoublePrimitive(): Double?
	fun setDoubleObj(v: Double?)
	fun getDoubleObj(): Double?
	fun setFloatPrimitive(v: Float?)
	fun getFloatPrimitive(): Float?
	fun setFloatObj(v: Float?)
	fun getFloatObj(): Float?
	fun setIntPrimitive(v: Int?)
	fun getIntPrimitive(): Int?
	fun setIntObj(v: Int?)
	fun getIntObj(): Int?
	fun setLongPrimitive(v: Long?)
	fun getLongPrimitive(): Long?
	fun setLongObj(v: Long?)
	fun getLongObj(): Long?
	fun setShortPrimitive(v: Short?)
	fun getShortPrimitive(): Short?
	fun setShortObj(v: Short?)
	fun getShortObj(): Short?
	fun setEList(v: List<Any>?)
	fun getEList(): List<Any>?
	fun setDate(v: java.time.ZonedDateTime?)
	fun getDate(): java.time.ZonedDateTime?
	fun setDateDefVal(v: java.time.ZonedDateTime?)
	fun getDateDefVal(): java.time.ZonedDateTime?
	fun setStringObj(v: String?)
	fun getStringObj(): String?
	fun setListString(v: List<String>?)
	fun getListString(): List<String>?
	fun setListInt(v: List<Int>?)
	fun getListInt(): List<Int>?
	fun setListShort(v: List<Short>?)
	fun getListShort(): List<Short>?
	fun setMap(v: Any?)
	fun getMap(): Any?
}