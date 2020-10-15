package geodes.sms.neo4j.io

import geodes.sms.neo4j.io.type.AsBoolean

fun main() {
    val dbUri = "bolt://localhost:7687"
    val username = "neo4j"
    val password = "admin"
    val graphManager = GraphManager(dbUri, username, password)

    fun userAction1() {
        val n1 = graphManager.createNode("Node1")
        val n2 = graphManager.createNode("Node1")
        val n3 = graphManager.createNode("Node2")

        val n4 = n1.createChild("ref", "Node4")
        val n5 = n4.createChild("ref", "Node4")
        graphManager.saveChanges()

        n5.createOutRef("ref2", n1)
        graphManager.saveChanges()

        //n1.putProperty()
        graphManager.saveChanges()
    }   // end of scope; nc objectReference cleared

    fun userAction2() {
        val c = graphManager.loadNode(1000027, "C")
        val a = graphManager.loadNode(1000023, "A")

        val outputs = a.loadOutConnectedNodes("b", "B")
        println(outputs.size)

        val b = outputs.first { it._id == 1000026L }
        //b.removeOutRef("c", c)
        b.createChild("c", "C")
        b.createChild("c", "C")
//
        graphManager.saveChanges()
    }

    fun userAction3() {
        val n1 = graphManager.createNode("Node")
        n1.putProperty("pr2", "str")
        graphManager.saveChanges()

        n1.putProperty("pr3", 44L)
        n1.putProperty("pr1", false)
        graphManager.saveChanges()

        //n1.unload()
        val res = n1.getProperty("pr1", AsBoolean)

        println(res)
    }
    userAction3()

    graphManager.close()
}