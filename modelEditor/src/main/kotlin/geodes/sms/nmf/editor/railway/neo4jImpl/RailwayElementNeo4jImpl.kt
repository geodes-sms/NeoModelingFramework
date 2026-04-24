package geodes.sms.nmf.editor.railway.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.railway.*

abstract class RailwayElementNeo4jImpl(nc: INodeController) : RailwayElement, INodeController by nc {
	override fun setId(v: Int?) {
		if (v == null) removeProperty("id")
		else putProperty("id", v)
	}

	override fun getId(): Int? {
		return getProperty("id", AsInt)
	}
}