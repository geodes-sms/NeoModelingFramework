package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface RailwayContainer : INodeEntity {

	fun loadRoutes(limit: Int = 100): List<Route>
	fun addRoutes(): Route
	fun removeRoutes(v: Route)

	fun loadRegions(limit: Int = 100): List<Region>
	fun addRegions(): Region
	fun removeRegions(v: Region)
}