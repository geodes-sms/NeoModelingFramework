package geodes.sms.nmf.editor.railway.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.railway.*

class SemaphoreNeo4jImpl(nc: INodeController) : Semaphore, RailwayElementNeo4jImpl(nc) {
	override fun setSignal(v: Signal?) {
		if (v == null) removeProperty("signal")
		else putProperty("signal", v)
	}

	override fun getSignal(): Signal? {
		val res = getProperty("signal", AsString)
		return if (res != null) enumValueOf<Signal>(res) else null
	}
}