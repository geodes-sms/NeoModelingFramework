package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface SwitchPosition : RailwayElement {
	fun setPosition(v: Position?)
	fun getPosition(): Position?
	fun setRoute(v: Route)
	fun unsetRoute(v: Route)
	fun getRoute(): Route?
	fun setTarget(v: Switch)
	fun unsetTarget(v: Switch)
	fun getTarget(): Switch?
}