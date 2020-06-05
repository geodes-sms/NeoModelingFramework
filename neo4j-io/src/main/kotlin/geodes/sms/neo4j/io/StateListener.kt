package geodes.sms.neo4j.io

interface StateListener {
//    fun onCreate(id: Long)
//    fun onUpdate()
//    fun onRemove()
//    fun onDetach()

    interface Creatable {
        fun onCreate(id: Long)
    }

    interface Updatable {
        fun onUpdate()
    }

    interface Removable {
        fun onRemove()
    }

    //remove from cache
    interface Detachable {
        fun onDetach()
    }
}