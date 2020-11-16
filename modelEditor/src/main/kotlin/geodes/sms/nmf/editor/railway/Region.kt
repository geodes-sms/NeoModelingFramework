package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface Region : RailwayElement {

	fun loadSensors(limit: Int = 100): List<Sensor>
	fun addSensors(): Sensor
	fun removeSensors(v: Sensor)
	fun addSegmentElements(): Segment
	fun addSwitchElements(): Switch
	fun loadElements(limit: Int = 100): List<TrackElement>
	
	fun removeElements(v: TrackElement)
}