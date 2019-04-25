package  geodes.sms.modeleditor.mindmaps

interface ModelManager {
            fun createSubTopic() : SubTopic
            fun getSubTopicByID(id: Int) : SubTopic?
        
            fun createMainTopic() : MainTopic
            fun getMainTopicByID(id: Int) : MainTopic?
        
            fun createCentralTopic() : CentralTopic
            fun getCentralTopicByID(id: Int) : CentralTopic?
        
            fun createMarker() : Marker
            fun getMarkerByID(id: Int) : Marker?
        
            fun createMindMap() : MindMap
            fun getMindMapByID(id: Int) : MindMap?
        }