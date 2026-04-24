package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface Region : RailwayElement {
	fun unsetSensors(v: Sensor)
	fun getSensors(limit: Int = 100): List<Sensor>
	fun addSensors(): Sensor
	fun unsetElements(v: TrackElement)
	fun getElements(limit: Int = 100): List<TrackElement>
	fun addElements(type: TrackElementType): TrackElement
}