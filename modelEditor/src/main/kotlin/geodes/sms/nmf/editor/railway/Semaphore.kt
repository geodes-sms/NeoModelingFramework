package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface Semaphore : RailwayElement {
	fun setSignal(v: Signal?)
	fun getSignal(): Signal?
}