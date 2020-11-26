package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface Switch : TrackElement {
	fun setCurrentPosition(v: Position?)
	fun getCurrentPosition(): Position?
	fun setPositions(v: SwitchPosition)
	fun unsetPositions(v: SwitchPosition)
	fun loadPositions(limit: Int = 100): List<SwitchPosition>
}