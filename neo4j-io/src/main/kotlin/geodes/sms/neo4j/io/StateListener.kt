package geodes.sms.neo4j.io

interface StateListener {
    /*fun onCreate(id: Long)  //state = unchanged
    fun onUpdate()    //clear cache; change state after modified (modif -> unchanged)
    fun onRemove()
    fun onDetach()*/

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