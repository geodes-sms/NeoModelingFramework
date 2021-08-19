package geodes.sms.nmf.editor.railway.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.railway.*

class RouteNeo4jImpl(nc: INodeController) : Route, RailwayElementNeo4jImpl(nc) {
	override fun setActive(v: Boolean?) {
		if (v == null) removeProperty("active")
		else putProperty("active", v)
	}

	override fun getActive(): Boolean? {
		return getProperty("active", AsBoolean)
	}

	override fun addFollows(): SwitchPosition {
		return SwitchPositionNeo4jImpl(createChild("follows", "SwitchPosition"))
	}

	override fun unsetFollows(v: SwitchPosition) {
		removeChild("follows", v)
	}

	override fun getFollows(limit: Int): List<SwitchPosition> {
		return loadOutConnectedNodes("follows", null, limit) {
			SwitchPositionNeo4jImpl(it)
		}
	}

	override fun setRequires(v: Sensor) {
		createOutRef("requires", v)
	}

	override fun unsetRequires(v: Sensor) {
		removeOutRef("requires", v, 2)
	}

	override fun getRequires(limit: Int): List<Sensor> {
		return loadOutConnectedNodes("requires", null, limit) {
			SensorNeo4jImpl(it)
		}
	}

	override fun setEntry(v: Semaphore) {
		createOutRef("entry", v, 1)
	}

	override fun unsetEntry(v: Semaphore) {
		removeOutRef("entry", v)
	}

	override fun getEntry(): Semaphore? {
		val data = loadOutConnectedNodes("entry", null, 1) {
			SemaphoreNeo4jImpl(it)
		}
		return if (data.isEmpty()) null else data[0]
	}

	override fun setExit(v: Semaphore) {
		createOutRef("exit", v, 1)
	}

	override fun unsetExit(v: Semaphore) {
		removeOutRef("exit", v)
	}

	override fun getExit(): Semaphore? {
		val data = loadOutConnectedNodes("exit", null, 1) {
			SemaphoreNeo4jImpl(it)
		}
		return if (data.isEmpty()) null else data[0]
	}
}