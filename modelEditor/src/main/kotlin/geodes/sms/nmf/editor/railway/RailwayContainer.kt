package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface RailwayContainer : INodeEntity {
	fun unsetRoutes(v: Route)
	fun getRoutes(limit: Int = 100): List<Route>
	fun addRoutes(): Route
	fun unsetRegions(v: Region)
	fun getRegions(limit: Int = 100): List<Region>
	fun addRegions(): Region
}