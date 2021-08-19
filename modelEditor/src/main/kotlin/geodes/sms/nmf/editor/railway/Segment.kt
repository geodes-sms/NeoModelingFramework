package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface Segment : TrackElement {
	fun setLength(v: Int?)
	fun getLength(): Int?
	fun unsetSemaphores(v: Semaphore)
	fun getSemaphores(limit: Int = 100): List<Semaphore>
	fun addSemaphores(): Semaphore
}