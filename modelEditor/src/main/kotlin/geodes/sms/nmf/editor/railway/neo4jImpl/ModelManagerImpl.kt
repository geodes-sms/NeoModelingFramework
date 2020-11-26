package geodes.sms.nmf.editor.railway.neo4jImpl
    
import geodes.sms.neo4j.io.GraphManager
import geodes.sms.neo4j.io.entity.INodeEntity
import geodes.sms.nmf.editor.railway.*
    
class ModelManagerImpl(dbUri: String, username: String, password: String): AutoCloseable {
    private val manager = GraphManager(dbUri, username, password)
    
    fun saveChanges() {
        manager.saveChanges()
    }

    fun clearCache() {
        manager.clearCache()
    }

    fun clearDB() {
        manager.clearDB()
    }

    override fun close() {
        manager.close()
    }
    
	fun createRailwayContainer(): RailwayContainer {
	    return RailwayContainerNeo4jImpl(manager.createNode("RailwayContainer"))
	}
	
	fun loadRailwayContainerById(id: Long): RailwayContainer {
	    return RailwayContainerNeo4jImpl(manager.loadNode(id, "RailwayContainer"))
	}
	
	fun unload(node: RailwayContainer) {
	    manager.unload(node)
	}
	
	fun createRegion(): Region {
	    return RegionNeo4jImpl(manager.createNode("Region"))
	}
	
	fun loadRegionById(id: Long): Region {
	    return RegionNeo4jImpl(manager.loadNode(id, "Region"))
	}
	
	fun unload(node: Region) {
	    manager.unload(node)
	}
	
	fun createRoute(): Route {
	    return RouteNeo4jImpl(manager.createNode("Route"))
	}
	
	fun loadRouteById(id: Long): Route {
	    return RouteNeo4jImpl(manager.loadNode(id, "Route"))
	}
	
	fun unload(node: Route) {
	    manager.unload(node)
	}
	
	fun createSensor(): Sensor {
	    return SensorNeo4jImpl(manager.createNode("Sensor"))
	}
	
	fun loadSensorById(id: Long): Sensor {
	    return SensorNeo4jImpl(manager.loadNode(id, "Sensor"))
	}
	
	fun unload(node: Sensor) {
	    manager.unload(node)
	}
	
	fun createSegment(): Segment {
	    return SegmentNeo4jImpl(manager.createNode("Segment"))
	}
	
	fun loadSegmentById(id: Long): Segment {
	    return SegmentNeo4jImpl(manager.loadNode(id, "Segment"))
	}
	
	fun unload(node: Segment) {
	    manager.unload(node)
	}
	
	fun createSwitch(): Switch {
	    return SwitchNeo4jImpl(manager.createNode("Switch"))
	}
	
	fun loadSwitchById(id: Long): Switch {
	    return SwitchNeo4jImpl(manager.loadNode(id, "Switch"))
	}
	
	fun unload(node: Switch) {
	    manager.unload(node)
	}
	
	fun createSwitchPosition(): SwitchPosition {
	    return SwitchPositionNeo4jImpl(manager.createNode("SwitchPosition"))
	}
	
	fun loadSwitchPositionById(id: Long): SwitchPosition {
	    return SwitchPositionNeo4jImpl(manager.loadNode(id, "SwitchPosition"))
	}
	
	fun unload(node: SwitchPosition) {
	    manager.unload(node)
	}
	
	fun createSemaphore(): Semaphore {
	    return SemaphoreNeo4jImpl(manager.createNode("Semaphore"))
	}
	
	fun loadSemaphoreById(id: Long): Semaphore {
	    return SemaphoreNeo4jImpl(manager.loadNode(id, "Semaphore"))
	}
	
	fun unload(node: Semaphore) {
	    manager.unload(node)
	}
	
}