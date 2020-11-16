package geodes.sms.nmf.editor.railway.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.railway.*

class SensorNeo4jImpl(nc: INodeController) : Sensor, RailwayElementNeo4jImpl(nc) {
    override fun loadMonitors(limit: Int): List<TrackElement> {
        return loadOutConnectedNodes("monitors", null, limit, "") {
            when (it.label) {
				"Segment" -> SegmentNeo4jImpl(it)
				"Switch" -> SwitchNeo4jImpl(it)
                else -> throw Exception("Cannot cast InodeController")
            }
        }
    }

    override fun setMonitors(v: TrackElement) {
        createOutRef("monitors", v)
    }

    override fun unsetMonitors(v: TrackElement) {
        removeOutRef("monitors", v)
    }
}