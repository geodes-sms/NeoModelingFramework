package geodes.sms.nmf.editor.attributes.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.attributes.*

class RootNeo4jImpl(nc: INodeController) : Root, INodeController by nc {
	override fun setBigDecimal(v: java.math.BigDecimal?) {
		if (v == null) removeProperty("bigDecimal")
		else putProperty("bigDecimal", v)
	}

	override fun getBigDecimal(): java.math.BigDecimal? {
		return getProperty("bigDecimal", AsBigDecimal)
	}

	override fun setBigInteger(v: java.math.BigInteger?) {
		if (v == null) removeProperty("bigInteger")
		else putProperty("bigInteger", v)
	}

	override fun getBigInteger(): java.math.BigInteger? {
		return getProperty("bigInteger", AsBigInteger)
	}

	override fun setBoolPrimitive(v: Boolean?) {
		if (v == null) removeProperty("boolPrimitive")
		else putProperty("boolPrimitive", v)
	}

	override fun getBoolPrimitive(): Boolean? {
		return getProperty("boolPrimitive", AsBoolean)
	}

	override fun setBoolObj(v: Boolean?) {
		if (v == null) removeProperty("boolObj")
		else putProperty("boolObj", v)
	}

	override fun getBoolObj(): Boolean? {
		return getProperty("boolObj", AsBoolean)
	}

	override fun setBytePrimitive(v: Byte?) {
		if (v == null) removeProperty("bytePrimitive")
		else putProperty("bytePrimitive", v)
	}

	override fun getBytePrimitive(): Byte? {
		return getProperty("bytePrimitive", AsByte)
	}

	override fun setByteObj(v: Byte?) {
		if (v == null) removeProperty("byteObj")
		else putProperty("byteObj", v)
	}

	override fun getByteObj(): Byte? {
		return getProperty("byteObj", AsByte)
	}

	override fun setByteArray(v: ByteArray?) {
		if (v == null) removeProperty("byteArray")
		else putProperty("byteArray", v)
	}

	override fun getByteArray(): ByteArray? {
		return getProperty("byteArray", AsByteArray)
	}

	override fun setByteArrayDefVal(v: ByteArray?) {
		if (v == null) removeProperty("byteArrayDefVal")
		else putProperty("byteArrayDefVal", v)
	}

	override fun getByteArrayDefVal(): ByteArray? {
		return getProperty("byteArrayDefVal", AsByteArray)
	}

	override fun setCharPrimitive(v: Char?) {
		if (v == null) removeProperty("charPrimitive")
		else putProperty("charPrimitive", v)
	}

	override fun getCharPrimitive(): Char? {
		return getProperty("charPrimitive", AsChar)
	}

	override fun setCharObj(v: Char?) {
		if (v == null) removeProperty("charObj")
		else putProperty("charObj", v)
	}

	override fun getCharObj(): Char? {
		return getProperty("charObj", AsChar)
	}

	override fun setCharObjDefVal(v: Char?) {
		if (v == null) removeProperty("charObjDefVal")
		else putProperty("charObjDefVal", v)
	}

	override fun getCharObjDefVal(): Char? {
		return getProperty("charObjDefVal", AsChar)
	}

	override fun setDoublePrimitive(v: Double?) {
		if (v == null) removeProperty("doublePrimitive")
		else putProperty("doublePrimitive", v)
	}

	override fun getDoublePrimitive(): Double? {
		return getProperty("doublePrimitive", AsDouble)
	}

	override fun setDoubleObj(v: Double?) {
		if (v == null) removeProperty("doubleObj")
		else putProperty("doubleObj", v)
	}

	override fun getDoubleObj(): Double? {
		return getProperty("doubleObj", AsDouble)
	}

	override fun setFloatPrimitive(v: Float?) {
		if (v == null) removeProperty("floatPrimitive")
		else putProperty("floatPrimitive", v)
	}

	override fun getFloatPrimitive(): Float? {
		return getProperty("floatPrimitive", AsFloat)
	}

	override fun setFloatObj(v: Float?) {
		if (v == null) removeProperty("floatObj")
		else putProperty("floatObj", v)
	}

	override fun getFloatObj(): Float? {
		return getProperty("floatObj", AsFloat)
	}

	override fun setIntPrimitive(v: Int?) {
		if (v == null) removeProperty("intPrimitive")
		else putProperty("intPrimitive", v)
	}

	override fun getIntPrimitive(): Int? {
		return getProperty("intPrimitive", AsInt)
	}

	override fun setIntObj(v: Int?) {
		if (v == null) removeProperty("intObj")
		else putProperty("intObj", v)
	}

	override fun getIntObj(): Int? {
		return getProperty("intObj", AsInt)
	}

	override fun setLongPrimitive(v: Long?) {
		if (v == null) removeProperty("longPrimitive")
		else putProperty("longPrimitive", v)
	}

	override fun getLongPrimitive(): Long? {
		return getProperty("longPrimitive", AsLong)
	}

	override fun setLongObj(v: Long?) {
		if (v == null) removeProperty("longObj")
		else putProperty("longObj", v)
	}

	override fun getLongObj(): Long? {
		return getProperty("longObj", AsLong)
	}

	override fun setShortPrimitive(v: Short?) {
		if (v == null) removeProperty("shortPrimitive")
		else putProperty("shortPrimitive", v)
	}

	override fun getShortPrimitive(): Short? {
		return getProperty("shortPrimitive", AsShort)
	}

	override fun setShortObj(v: Short?) {
		if (v == null) removeProperty("shortObj")
		else putProperty("shortObj", v)
	}

	override fun getShortObj(): Short? {
		return getProperty("shortObj", AsShort)
	}

	override fun setEList(v: List<Any>?) {
		if (v == null) removeProperty("eList")
		else putProperty("eList", v)
	}

	override fun getEList(): List<Any>? {
		return getProperty("eList", AsList(AsObject))
	}

	override fun setDate(v: java.time.ZonedDateTime?) {
		if (v == null) removeProperty("date")
		else putProperty("date", v)
	}

	override fun getDate(): java.time.ZonedDateTime? {
		return getProperty("date", AsZonedDateTime)
	}

	override fun setDateDefVal(v: java.time.ZonedDateTime?) {
		if (v == null) removeProperty("dateDefVal")
		else putProperty("dateDefVal", v)
	}

	override fun getDateDefVal(): java.time.ZonedDateTime? {
		return getProperty("dateDefVal", AsZonedDateTime)
	}

	override fun setStringObj(v: String?) {
		if (v == null) removeProperty("stringObj")
		else putProperty("stringObj", v)
	}

	override fun getStringObj(): String? {
		return getProperty("stringObj", AsString)
	}

	override fun setListString(v: List<String>?) {
		when {
			v == null || v.isEmpty() -> removeProperty("listString")
			v.size in 2..5 -> putProperty("listString", v)
			else -> throw Exception("bound limits: list size must be in 2..5")
		}
	}

	override fun getListString(): List<String>? {
		return getProperty("listString", AsList(AsString))
	}


	override fun setListInt(v: List<Int>?) {
		if (v == null || v.isEmpty()) removeProperty("listInt")
		else putProperty("listInt", v)
	}

	override fun getListInt(): List<Int>? {
		return getProperty("listInt", AsList(AsInt))
	}


	override fun setListShort(v: List<Short>?) {
		when {
			v == null || v.isEmpty() -> removeProperty("listShort")
			v.size in 0..5 -> putProperty("listShort", v)
			else -> throw Exception("bound limits: list size must be in 0..5")
		}
	}

	override fun getListShort(): List<Short>? {
		return getProperty("listShort", AsList(AsShort))
	}


	override fun setMap(v: Any?) {
		TODO("map as parameter is not supported yet")
//		if (v == null) removeProperty("map")
//		else putProperty("map", v)
	}

	override fun getMap(): Any? {
		return getProperty("map", AsObject)
	}
}