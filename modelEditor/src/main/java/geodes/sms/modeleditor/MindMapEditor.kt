package geodes.sms.modeleditor


import geodes.sms.modeleditor.mindmaps.neo4jImpl.ModelManagerNeo4jImpl


object MindMapEditor {

    @JvmStatic
    fun main(args: Array<String>) {
        val dbUri = "bolt://localhost:7687"
        val username = "neo4j"
        val password = "admin"
        val manager = ModelManagerNeo4jImpl(dbUri, username, password)

        val mindMap = manager.createMindMap()
        val centralTopic1 = manager.createCentralTopic()
        val centralTopic2 = manager.createCentralTopic()
        val mainTopic1 = manager.createMainTopic()

        mindMap.setTitle("mindMap")
        centralTopic1.setName("ct 1")
        centralTopic2.setName("ct 2")
        mainTopic1.setName("main topic1")
        /*val mainTopic2 = manager.createMainTopic()
        val subTopic1 = manager.createSubTopic()
        val subTopic2 = manager.createSubTopic()*/

        println("set ct1: " + mindMap.setCentralTopic(centralTopic1))
        println("set ct2: " + mindMap.setCentralTopic(centralTopic2))
        centralTopic2.addMainTopics(mainTopic1)

        println(centralTopic1.getName())
        /*
        val mindMap = manager.getMindMapByID(855)!!
        val centralTopic1 = manager.getCentralTopicByID(856)!!
        val centralTopic2 = manager.getCentralTopicByID(857)!!
        //println("set st: " + mindMap.setCentralTopic(centralTopic1))

        /*
        val marker1 = manager.createMarker()
        val marker2 = manager.createMarker()
        val marker3 = manager.createMarker()
        val marker4 = manager.createMarker()
        val marker5 = manager.createMarker()
        val marker6 = manager.createMarker()

        marker1.setName("mark 1")
        marker2.setName("mark 2")
        marker3.setName("mark 3")
        marker4.setName("mark 4")
        marker5.setName("mark 5")
        marker6.setName("mark 6")*/

        val marker1 = manager.getMarkerByID(824)!!
        val marker2 = manager.getMarkerByID(825)!!
        val marker3 = manager.getMarkerByID(826)!!
        val marker4 = manager.getMarkerByID(827)!!
        val marker5 = manager.getMarkerByID(828)!!
        val marker6 = manager.getMarkerByID(829)!!

        println(centralTopic2.addMarker(marker6))
        println(centralTopic1.addMarker(marker2))
        println(centralTopic1.addMarker(marker3))
        println(centralTopic1.addMarker(marker4))
        println(centralTopic1.addMarker(marker5))
        println(centralTopic1.addMarker(marker6))
        println(centralTopic1.addMarker(marker6))

*/
        manager.close()
    }
}
