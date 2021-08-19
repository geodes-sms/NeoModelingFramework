package geodes.sms.nmf.editor.railway.neo4jImpl

import geodes.sms.neo4j.io.controllers.INodeController
import geodes.sms.neo4j.io.type.*
import geodes.sms.nmf.editor.railway.*

class RailwayContainerNeo4jImpl(nc: INodeController) : RailwayContainer, INodeController by nc {
	override fun addRoutes(): Route {
		return RouteNeo4jImpl(createChild("routes", "Route"))
	}

	override fun unsetRoutes(v: Route) {
		removeChild("routes", v)
	}

	override fun getRoutes(limit: Int): List<Route> {
		return loadOutConnectedNodes("routes", null, limit) {
			RouteNeo4jImpl(it)
		}
	}

	override fun addRegions(): Region {
		return RegionNeo4jImpl(createChild("regions", "Region"))
	}

	override fun unsetRegions(v: Region) {
		removeChild("regions", v)
	}

	override fun getRegions(limit: Int): List<Region> {
		return loadOutConnectedNodes("regions", null, limit) {
			RegionNeo4jImpl(it)
		}
	}
}