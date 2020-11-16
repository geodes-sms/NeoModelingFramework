package geodes.sms.nmf.editor.railway.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.railway.*

class RegionNeo4jImpl(nc: INodeController) : Region, RailwayElementNeo4jImpl(nc) {
    override fun addSensors(): Sensor {
        return SensorNeo4jImpl(createChild("sensors", "Sensor"))
    }

    override fun loadSensors(limit: Int): List<Sensor> {
        return loadOutConnectedNodes("sensors", null, limit, "") {
            SensorNeo4jImpl(it)
        }
    }

    override fun removeSensors(v: Sensor) {
        removeChild("sensors", v)
    }

    override fun addSegmentElements(): Segment {
        return SegmentNeo4jImpl(createChild("elements", "Segment"))
    }

    override fun addSwitchElements(): Switch {
        return SwitchNeo4jImpl(createChild("elements", "Switch"))
    }

    override fun loadElements(limit: Int): List<TrackElement> {
        return loadOutConnectedNodes("elements", null, limit, "") {
            when (it.label) {
				"Segment" -> SegmentNeo4jImpl(it)
				"Switch" -> SwitchNeo4jImpl(it)
                else -> throw Exception("Cannot cast InodeController")
            }
        }
    }

    override fun removeElements(v: TrackElement) {
        removeChild("elements", v)
    }
}