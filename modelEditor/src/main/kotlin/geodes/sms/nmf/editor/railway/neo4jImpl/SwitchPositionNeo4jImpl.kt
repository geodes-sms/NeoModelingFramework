package geodes.sms.nmf.editor.railway.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.railway.*

class SwitchPositionNeo4jImpl(nc: INodeController) : SwitchPosition, RailwayElementNeo4jImpl(nc) {
	override fun setPosition(v: Position?) {
		if (v == null) removeProperty("position")
		else putProperty("position", v)
	}

	override fun getPosition(): Position? {
		val res = getProperty("position", AsString)
		return if (res != null) enumValueOf<Position>(res) else null
	}

	override fun setRoute(v: Route) {
		createOutRef("route", v, 1)
	}

	override fun unsetRoute(v: Route) {
		removeOutRef("route", v)
	}

	override fun getRoute(): Route? {
		val data = loadOutConnectedNodes("route", null, 1) {
			RouteNeo4jImpl(it)
		}
		return if (data.isEmpty()) null else data[0]
	}

	override fun setTarget(v: Switch) {
		createOutRef("target", v, 1)
	}

	override fun unsetTarget(v: Switch) {
		removeOutRef("target", v)
	}

	override fun getTarget(): Switch? {
		val data = loadOutConnectedNodes("target", null, 1) {
			SwitchNeo4jImpl(it)
		}
		return if (data.isEmpty()) null else data[0]
	}
}