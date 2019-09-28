package geodes.sms.nmf.neo4j.io

import org.neo4j.driver.v1.Value
import org.neo4j.driver.v1.Values


//used to restrict ID setter
interface StateListener {
    fun onSave(id: Long)
}

interface INode {
    val id: Long
    val alias : String
    fun setProperty(name: String, value: Any)
}

class Node(private val props: HashMap<String, Value>): INode,
    StateListener {

    private companion object {
        var n: Int = 0
            get() = field++

        fun createAlias(): String { //"n" + "$n".padStart(3, '0')
            val length = 3
            val base = n.toString()

            return if (length <= base.length) "n$base"
            else {
                val sb = StringBuilder(length + 1)
                sb.append('n')
                for (i in 1..(length - base.length))
                    sb.append('0')
                sb.append(base).toString()
            }
        }
    }

    override val alias = "n$n"
    override var id : Long = -1
        private set

    override fun onSave(id: Long) {
        this.id = id
        props.clear()
    }

    override fun setProperty(name: String, value: Any) {
        props[name] = Values.value(value)
    }
}