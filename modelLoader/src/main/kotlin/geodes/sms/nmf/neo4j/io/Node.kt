package geodes.sms.nmf.neo4j.io

import org.eclipse.emf.ecore.EEnumLiteral
import org.neo4j.driver.internal.value.*
import org.neo4j.driver.v1.Value
import org.neo4j.driver.v1.Values
import java.time.ZoneId
import java.time.ZonedDateTime


class Node : INode, GraphStateListener {

    constructor(graph: NodeStateListener) {
        this.graph = graph
        this.id = -1
        this.nodeState = StateRegistered    //Node and props are inited in query (CREATE)
        this.propsState = StateRegistered
    }

    constructor(graph: NodeStateListener, id: Long) {
        this.graph = graph
        this.id = id
        this.nodeState = NodeStateNotRegistered()
        this.propsState = NodeStateNotRegistered()
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
    var nodeState: State    //= StateRegistered //Node must be inited in query (MATCH or CREATE)
        private set
    private var propsState: State
    override val alias: String = "n$n"
    override val props = hashMapOf<String, Value>()
    override var id: Long = -1  //id
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

    override fun setProperty(name: String, value: Any) {
        val input = when (value) {
            is String ->  StringValue(value)
            is Int ->   IntegerValue(value.toLong())
            is Long ->  IntegerValue(value)
            is Short -> IntegerValue(value.toLong())
            is Boolean -> BooleanValue.fromBoolean(value)
            is Byte ->  IntegerValue(value.toLong())
            is ByteArray -> BytesValue(value)
            is Char ->    StringValue(value.toString())
            is Double ->  FloatValue(value)
            is Float ->   FloatValue(value.toDouble())
            is EEnumLiteral -> StringValue(value.literal)
            is java.util.Date -> DateTimeValue(ZonedDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault()))
            is List<*> -> Values.value(value)    //EList
            //is Map<*, *> -> "Map"  //EMap
            is java.math.BigDecimal -> StringValue(value.toString())
            is java.math.BigInteger -> StringValue(value.toString())
            //is EEnum = EClass = EDataType  //not in model instance; is not a direct attr type
            else ->  NullValue.NULL //not persistable value
        }

        props[name] = input
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