package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface TrackElement : RailwayElement {
	fun setMonitoredBy(v: Sensor)
	fun unsetMonitoredBy(v: Sensor)
	fun loadMonitoredBy(limit: Int = 100): List<Sensor>
	fun setConnectsTo(v: TrackElement)
	fun unsetConnectsTo(v: TrackElement)
	fun loadConnectsTo(limit: Int = 100): List<TrackElement>
}