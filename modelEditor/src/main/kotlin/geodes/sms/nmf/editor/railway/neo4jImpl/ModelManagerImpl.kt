package geodes.sms.nmf.editor.railway.neo4jImpl

import geodes.sms.neo4j.io.controllers.IGraphManager
import geodes.sms.nmf.editor.railway.*
    
class ModelManagerImpl(dbUri: String, username: String, password: String): AutoCloseable {
    private val manager = IGraphManager.getDefaultManager(dbUri, username, password)
    
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
	
	fun loadRailwayContainerList(limit: Int = 100): List<RailwayContainer> {
	    return manager.loadNodes("RailwayContainer", limit) { RailwayContainerNeo4jImpl(it) }
	}
	
	fun unload(node: RailwayContainer) {
	    manager.unload(node)
	}
	
	fun remove(node: RailwayContainer) {
	    manager.remove(node)
	}
	
	fun createRegion(): Region {
	    return RegionNeo4jImpl(manager.createNode("Region"))
	}
	
	fun loadRegionById(id: Long): Region {
	    return RegionNeo4jImpl(manager.loadNode(id, "Region"))
	}
	
	fun loadRegionList(limit: Int = 100): List<Region> {
	    return manager.loadNodes("Region", limit) { RegionNeo4jImpl(it) }
	}
	
	fun unload(node: Region) {
	    manager.unload(node)
	}
	
	fun remove(node: Region) {
	    manager.remove(node)
	}
	
	fun createRoute(): Route {
	    return RouteNeo4jImpl(manager.createNode("Route"))
	}
	
	fun loadRouteById(id: Long): Route {
	    return RouteNeo4jImpl(manager.loadNode(id, "Route"))
	}
	
	fun loadRouteList(limit: Int = 100): List<Route> {
	    return manager.loadNodes("Route", limit) { RouteNeo4jImpl(it) }
	}
	
	fun unload(node: Route) {
	    manager.unload(node)
	}
	
	fun remove(node: Route) {
	    manager.remove(node)
	}
	
	fun createSensor(): Sensor {
	    return SensorNeo4jImpl(manager.createNode("Sensor"))
	}
	
	fun loadSensorById(id: Long): Sensor {
	    return SensorNeo4jImpl(manager.loadNode(id, "Sensor"))
	}
	
	fun loadSensorList(limit: Int = 100): List<Sensor> {
	    return manager.loadNodes("Sensor", limit) { SensorNeo4jImpl(it) }
	}
	
	fun unload(node: Sensor) {
	    manager.unload(node)
	}
	
	fun remove(node: Sensor) {
	    manager.remove(node)
	}
	
	fun createSegment(): Segment {
	    return SegmentNeo4jImpl(manager.createNode("Segment"))
	}
	
	fun loadSegmentById(id: Long): Segment {
	    return SegmentNeo4jImpl(manager.loadNode(id, "Segment"))
	}
	
	fun loadSegmentList(limit: Int = 100): List<Segment> {
	    return manager.loadNodes("Segment", limit) { SegmentNeo4jImpl(it) }
	}
	
	fun unload(node: Segment) {
	    manager.unload(node)
	}
	
	fun remove(node: Segment) {
	    manager.remove(node)
	}
	
	fun createSwitch(): Switch {
	    return SwitchNeo4jImpl(manager.createNode("Switch"))
	}
	
	fun loadSwitchById(id: Long): Switch {
	    return SwitchNeo4jImpl(manager.loadNode(id, "Switch"))
	}
	
	fun loadSwitchList(limit: Int = 100): List<Switch> {
	    return manager.loadNodes("Switch", limit) { SwitchNeo4jImpl(it) }
	}
	
	fun unload(node: Switch) {
	    manager.unload(node)
	}
	
	fun remove(node: Switch) {
	    manager.remove(node)
	}
	
	fun createSwitchPosition(): SwitchPosition {
	    return SwitchPositionNeo4jImpl(manager.createNode("SwitchPosition"))
	}
	
	fun loadSwitchPositionById(id: Long): SwitchPosition {
	    return SwitchPositionNeo4jImpl(manager.loadNode(id, "SwitchPosition"))
	}
	
	fun loadSwitchPositionList(limit: Int = 100): List<SwitchPosition> {
	    return manager.loadNodes("SwitchPosition", limit) { SwitchPositionNeo4jImpl(it) }
	}
	
	fun unload(node: SwitchPosition) {
	    manager.unload(node)
	}
	
	fun remove(node: SwitchPosition) {
	    manager.remove(node)
	}
	
	fun createSemaphore(): Semaphore {
	    return SemaphoreNeo4jImpl(manager.createNode("Semaphore"))
	}
	
	fun loadSemaphoreById(id: Long): Semaphore {
	    return SemaphoreNeo4jImpl(manager.loadNode(id, "Semaphore"))
	}
	
	fun loadSemaphoreList(limit: Int = 100): List<Semaphore> {
	    return manager.loadNodes("Semaphore", limit) { SemaphoreNeo4jImpl(it) }
	}
	
	fun unload(node: Semaphore) {
	    manager.unload(node)
	}
	
	fun remove(node: Semaphore) {
	    manager.remove(node)
	}
	
}