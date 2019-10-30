package geodes.sms.nmf.neo4j.io


//used to restrict ID setter
interface GraphStateListener {
    fun onSave(id: Long)
    fun onSave()
}