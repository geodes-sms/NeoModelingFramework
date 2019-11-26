package geodes.sms.nmf.neo4j.io

import geodes.sms.nmf.neo4j.Values
import org.neo4j.driver.Value
import org.neo4j.driver.internal.value.NullValue


class Node : INode, GraphStateListener {

    constructor(graph: NodeStateListener, props: Map<String, Value> = emptyMap()) {
        this.graph = graph
        this.id = -1
        this.nodeState = StateRegistered    //Node and props are inited in query (CREATE)
        this.propsState = PropsStateNotRegistered()
        if (props.isNotEmpty()) {
            this.props.putAll(props)
            propsState.register()
        }
    }

    constructor(graph: NodeStateListener, id: Long) {
        this.graph = graph
        this.id = id
        this.nodeState = NodeStateNotRegistered()
        this.propsState = PropsStateNotRegistered()
    }

    private companion object {
        var n: Int = 0
            get() = field++

        fun createAlias(): String { //"n" + "$n".padStart(6, '0')
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
    var nodeState: State    // StateRegistered ==> Node is inited in query (MATCH or CREATE)
        private set
    private var propsState: State   // StateRegistered ==> query contains SET statement for the Node
    override val alias: String = "n$n"
    override val props = hashMapOf<String, Value>()
    override var id: Long
        private set

    //// Inherited from GraphStateListener
    override fun onSave(id: Long) {
        this.id = id
        onSave()
    }

    override fun onSave() {
        nodeState  = NodeStateNotRegistered()
        propsState = PropsStateNotRegistered()
        props.clear()
    }
    ////

    /*
    override fun remove() {
        graph.onRemove(this)
        props.clear()
        state = StateRegistered
        should OPTIONAL MATCH removed Node
        to do: re-design remove function
    }*/

    override fun setProperty(name: String, input: Any) {
        props[name] = Values.value(input)
        propsState.register()
    }

    override fun removeProperty(name: String) {
        props[name] = NullValue.NULL
        propsState.register()
    }

    private inner class NodeStateNotRegistered : State {
        override fun register() {
            nodeState = StateRegistered
            graph.onMatch(this@Node)
        }
    }

    private inner class PropsStateNotRegistered : State {
        override fun register() {
            nodeState.register()
            propsState = StateRegistered
            graph.onUpdate(this@Node, props)
        }
    }

    /** Presented in query */
    private object StateRegistered : State {
        override fun register() = Unit
    }
}