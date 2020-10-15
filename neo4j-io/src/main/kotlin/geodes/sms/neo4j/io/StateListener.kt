package geodes.sms.neo4j.io

interface StateListener {
    fun onCreate(id: Long)
    fun onUpdate()
    fun onRemove()
    fun onDetach()
}