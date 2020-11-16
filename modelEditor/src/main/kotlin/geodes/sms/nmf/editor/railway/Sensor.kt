package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface Sensor : RailwayElement {
	fun setMonitors(v: TrackElement)
	fun unsetMonitors(v: TrackElement)
		fun loadMonitors(limit: Int = 100): List<TrackElement>
	
}