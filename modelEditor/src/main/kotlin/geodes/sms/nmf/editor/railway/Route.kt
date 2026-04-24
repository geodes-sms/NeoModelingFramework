package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface Route : RailwayElement {
	fun setActive(v: Boolean?)
	fun getActive(): Boolean?
	fun unsetFollows(v: SwitchPosition)
	fun getFollows(limit: Int = 100): List<SwitchPosition>
	fun addFollows(): SwitchPosition
	fun setRequires(v: Sensor)
	fun unsetRequires(v: Sensor)
	fun getRequires(limit: Int = 100): List<Sensor>
	fun setEntry(v: Semaphore)
	fun unsetEntry(v: Semaphore)
	fun getEntry(): Semaphore?
	fun setExit(v: Semaphore)
	fun unsetExit(v: Semaphore)
	fun getExit(): Semaphore?
}