package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface Route : RailwayElement {
	fun setActive(v: Boolean?)
	fun getActive(): Boolean?

	fun loadFollows(limit: Int = 100): List<SwitchPosition>
	fun addFollows(): SwitchPosition
	fun removeFollows(v: SwitchPosition)
	fun setRequires(v: Sensor)
	fun unsetRequires(v: Sensor)
		fun loadRequires(limit: Int = 100): List<Sensor>
	
	fun setEntry(v: Semaphore)
	fun unsetEntry(v: Semaphore)
		fun loadEntry(): Semaphore?
	
	fun setExit(v: Semaphore)
	fun unsetExit(v: Semaphore)
		fun loadExit(): Semaphore?
	
}