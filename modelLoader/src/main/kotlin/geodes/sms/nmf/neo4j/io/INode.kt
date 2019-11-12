package geodes.sms.nmf.neo4j.io

import org.neo4j.driver.Value


interface INode {
    val id: Long
    val alias: String
    val props: Map<String, Value>

    fun setProperty(name: String, input: Any)
    fun removeProperty(name: String)

}