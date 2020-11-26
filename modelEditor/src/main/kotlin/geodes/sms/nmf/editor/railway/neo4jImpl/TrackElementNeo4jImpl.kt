package geodes.sms.nmf.editor.railway.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.railway.*

abstract class TrackElementNeo4jImpl(nc: INodeController) : TrackElement, RailwayElementNeo4jImpl(nc) {
	override fun setMonitoredBy(v: Sensor) {
		createOutRef("monitoredBy", v)
	}

	override fun unsetMonitoredBy(v: Sensor) {
		removeOutRef("monitoredBy", v)
	}

	override fun loadMonitoredBy(limit: Int): List<Sensor> {
		return loadOutConnectedNodes("monitoredBy", null, limit) {
			SensorNeo4jImpl(it)
		}
	}

	override fun setConnectsTo(v: TrackElement) {
		createOutRef("connectsTo", v)
	}

	override fun unsetConnectsTo(v: TrackElement) {
		removeOutRef("connectsTo", v)
	}

	override fun loadConnectsTo(limit: Int): List<TrackElement> {
		return loadOutConnectedNodes("connectsTo", null, limit) {
			when (it.label) {
				"Segment" -> SegmentNeo4jImpl(it)
				"Switch" -> SwitchNeo4jImpl(it)
				else -> throw Exception("Cannot cast to INodeController")
			}
		}
	}
}