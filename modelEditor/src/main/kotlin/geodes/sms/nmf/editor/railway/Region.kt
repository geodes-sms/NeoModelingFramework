package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface Region : RailwayElement {
	fun removeSensors(v: Sensor)
	fun loadSensors(limit: Int = 100): List<Sensor>
	fun addSensors(): Sensor
	fun removeElements(v: TrackElement)
	fun loadElements(limit: Int = 100): List<TrackElement>
	fun addElements(type: TrackElementType): TrackElement
}