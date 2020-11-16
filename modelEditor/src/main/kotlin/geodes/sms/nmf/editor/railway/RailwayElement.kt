package geodes.sms.nmf.editor.railway

import geodes.sms.neo4j.io.entity.INodeEntity

interface RailwayElement : INodeEntity {
	fun setId(v: Int?)
	fun getId(): Int?
}