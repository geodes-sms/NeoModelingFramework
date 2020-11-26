package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface RailwayContainer : INodeEntity {
	fun removeRoutes(v: Route)
	fun loadRoutes(limit: Int = 100): List<Route>
	fun addRoutes(): Route
	fun removeRegions(v: Region)
	fun loadRegions(limit: Int = 100): List<Region>
	fun addRegions(): Region
}