package geodes.sms.modelloader.neo4j2emf


import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Values
import org.neo4j.driver.v1.types.Node
import org.neo4j.driver.v1.types.Relationship


class Neo4jModelReader (dbUri: String, username: String, password: String, val rootID : Long) : AutoCloseable {

    private val driver = GraphDatabase.driver(dbUri, AuthTokens.basic(username, password))
    private val bufferCapacity = 10
    private var position : Int = 0
    private var hasData : Boolean = true

    fun readSubGraph() : List<Neo4jObject> {
        return driver.session().readTransaction {
            val res = it.run("MATCH p=(s)-[*0..]->(n)" +
                    " WHERE ID(s) = {rootID}" +
                    " OPTIONAL MATCH (n)-[out]->()" +
                    " OPTIONAL MATCH (n)<-[inp]-()" +
                    " RETURN DISTINCT n AS node, collect(DISTINCT inp) AS inputRefs, collect(DISTINCT out) AS outputRefs" +
                    " ORDER BY n.name" + //size(inputRefs)
                    " SKIP {skip} LIMIT {limit}",
                Values.parameters("rootID", rootID, "skip", position, "limit", bufferCapacity)).list()

            hasData = res.size >= bufferCapacity
            position += bufferCapacity

            res.map { record ->
                Neo4jObject(
                    node = record["node"].asNode(),
                    inputRefs = record["inputRefs"].asList(Values.ofRelationship()),
                    outputRefs = record["outputRefs"].asList(Values.ofRelationship())
                )
            }
        }
    }

    fun hasData() : Boolean = hasData

    /*
    fun getModel(packageID: Int) {
        driver.session().readTransaction {
            val res = it.run("MATCH (p:EPackage)-->(c:EClass) WHERE ID(p) = {ePackageID}" +
                    " OPTIONAL MATCH (subClass:EClass)-[:eSuperTypes*]->(c)-[:eSuperTypes*]->(superClass:EClass)" +
                    " OPTIONAL MATCH (c)-->(attr:EAttribute)" +
                    " OPTIONAL MATCH (c)-->(ref:EReference)-[:eType]->(rt1:EClass)" +
                    " OPTIONAL MATCH (superClass)-->(superAttr:EAttribute)" +
                    " OPTIONAL MATCH (superClass)-->(superRef:EReference)-[:eType]->(rt2:EClass)" +
                    " RETURN c AS eClass, collect(DISTINCT subClass.name) AS subclasses," +
                    " collect(DISTINCT attr) + collect(DISTINCT superAttr) AS eAttributes," +
                    " collect(DISTINCT ref{.*, endClass:rt1.name}) + collect(DISTINCT superRef {.*, endClass:rt2.name})" +
                    " AS eReferences", Values.parameters("ePackageID", packageID))

            res.forEach { record ->
                val eClass = record["eClass"].asNode()

                //println(eClass["name"].asString())

                val subclasses = record["subclasses"].asList(Values.ofString())
                val eAttributes = record["eAttributes"].asList(Values.ofNode())
                val eReferences = record["eReferences"].asList(Values.ofMap())
            }
        }
    }*/


    override fun close() {
        driver.close()
    }
}

data class Neo4jObject(val node : Node, val inputRefs : List<Relationship>, val outputRefs : List<Relationship>)