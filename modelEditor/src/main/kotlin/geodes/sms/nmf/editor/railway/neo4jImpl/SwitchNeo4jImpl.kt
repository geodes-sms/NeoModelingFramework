package geodes.sms.nmf.editor.railway.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.railway.*

class SwitchNeo4jImpl(nc: INodeController) : Switch, TrackElementNeo4jImpl(nc) {
	override fun setCurrentPosition(v: Position?) {
		if (v == null) removeProperty("currentPosition")
		else putProperty("currentPosition", v)
	}

	override fun getCurrentPosition(): Position? {
		val res = getProperty("currentPosition", AsString)
		return if (res != null) enumValueOf<Position>(res) else null
	}

	override fun setPositions(v: SwitchPosition) {
		createOutRef("positions", v)
	}

	override fun unsetPositions(v: SwitchPosition) {
		removeOutRef("positions", v)
	}

	override fun getPositions(limit: Int): List<SwitchPosition> {
		return loadOutConnectedNodes("positions", null, limit) {
			SwitchPositionNeo4jImpl(it)
		}
	}
}