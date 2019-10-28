package geodes.sms.nmf.neo4j.io

import org.neo4j.driver.v1.Value
import org.neo4j.driver.v1.Values


class Node : INode, GraphStateListener {

    /** Constructor is used to create proxy for a new node */
    constructor(graph: NodeStateListener) {
        this.graph = graph
        this.state = StateInited
        this.id = -1
    }

    /** Used to create proxy Node object for existing node in DB */
    constructor(graph: NodeStateListener, id: Long) {
        this.graph = graph
        this.state = StateNotInited()
        this.id = id
    }

    private companion object {
        var n: Int = 0
            get() = field++

        fun createAlias(): String { //"n" + "$n".padStart(3, '0')
            val length = 6
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

    private val graph: NodeStateListener
    private var state: NodeState
    override val alias: String
        get() = state.alias

    override var id: Long
        private set
    private val props = hashMapOf<String, Value>()


    //// Inherited from GraphStateListener
    override fun onSave(id: Long) {
        this.id = id
        onSave()
    }

    override fun onSave() {
        state = StateNotInited()
        props.clear()
    }
    ////


    override fun remove() {
        graph.onRemove(this)
        props.clear()
        state = StateInited
    }

    override fun setProperty(name: String, value: Any?) {

        //check value here
        props[name] = Values.value(value)
        state.update()
    }

    override fun getProperties(): Map<String, Value> = props


    private inner class StateNotInited : NodeState {

        override val alias: String
            get() {
                //graph.onMatch()
                state = StateInited
                return "n$n"
            }

        override fun update() {
            graph.onUpdate(this@Node, props)
            state = StateInited
        }
    }

    private object StateInited : NodeState {
        override val alias = "n$n"
        override fun update() = Unit
    }
}