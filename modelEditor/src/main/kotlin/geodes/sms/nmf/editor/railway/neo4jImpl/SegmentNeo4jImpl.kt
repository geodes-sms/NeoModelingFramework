package geodes.sms.nmf.editor.railway.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.railway.*

class SegmentNeo4jImpl(nc: INodeController) : Segment, TrackElementNeo4jImpl(nc) {
	override fun setLength(v: Int?) {
		if (v == null) removeProperty("length")
		else putProperty("length", v)
	}

	override fun getLength(): Int? {
		return getProperty("length", AsInt)
	}

	override fun addSemaphores(): Semaphore {
		return SemaphoreNeo4jImpl(createChild("semaphores", "Semaphore"))
	}

	override fun removeSemaphores(v: Semaphore) {
		removeChild("semaphores", v)
	}

	override fun loadSemaphores(limit: Int): List<Semaphore> {
		return loadOutConnectedNodes("semaphores", null, limit) {
			SemaphoreNeo4jImpl(it)
		}
	}
}