package geodes.sms.nmf.loader.neo4j2emf


import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Values
import org.neo4j.driver.types.Node
import org.neo4j.driver.types.Relationship


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


/*
object KotlinCodeGenerator {

    //move to model loader module neo4j2emf
    fun generateFromNeo4jMetamodel(dbDriver: Driver, ePackageID: Long, outputDir: String) : String {

        /** get package info */
        val packageName = dbDriver.session().use { it.readTransaction { transaction ->
            val res = transaction.run("MATCH (p:EPackage) " +
                    " WHERE ID(p)={ePackageID} RETURN p.name as packageName",
                Values.parameters("ePackageID", ePackageID))
            res.single()["packageName"].asString().toLowerCase()
        }}

        val implPath = File("$outputDir/$packageName/neo4jImpl")
        val interfacePath = File("$outputDir/$packageName")
        implPath.mkdirs()
        interfacePath.mkdirs()

        dbDriver.session().use { session -> session.readTransaction { transaction ->
            val res = transaction.run("MATCH (p:EPackage)-->(c:EClass) WHERE ID(p)={ePackageID}" +
                    " OPTIONAL MATCH (c)-[:eSuperTypes]->(superClass:EClass)" +
                    " WITH c, collect(superClass.name) AS superClass" +
                    " OPTIONAL MATCH (c)-->(attr:EAttribute)" +
                    " WITH c, superClass, collect(attr{.name, .eType, .upperBound, .lowerBound}) AS eAttr" +
                    " OPTIONAL MATCH (c)-->(ref:EReference)-[:eType]->(refType:EClass)" +
                    " RETURN c AS eClass, superClass, eAttr," +
                    " collect(ref{.name, .upperBound, .containment, refType:refType.name}) AS eRef",
                Values.parameters("ePackageID", ePackageID)
            )

            val managerInterface = File(interfacePath, "ModelManager.kt").bufferedWriter()
            val managerImpl = File(implPath, "ModelManagerNeo4jImpl.kt").bufferedWriter()
            managerInterface.write(Interface.ManagerClass.genHeader(packageName))
            managerImpl.write(Implementation.ManagerClass.genHeader(packageName))

            res.forEach { record ->
                val eClass = record["eClass"].asNode()
                val superClass = record["superClass"].asList { it.asString().capitalize() } //(Values.ofString())
                val className = eClass["name"].asString().capitalize()
                val isAbstract = (eClass["abstract"].asBoolean() || eClass["interface"].asBoolean())

                val eAttr = record["eAttr"].asList(Values.ofMap())
                val eRef = record["eRef"].asList(Values.ofMap())

                val interfaceWriter = File(interfacePath, "$className.kt").bufferedWriter()
                val implWriter = File(implPath, "${className}Neo4jImpl.kt").bufferedWriter()

                interfaceWriter.write(Interface.genHeader(packageName, className, superClass))
                implWriter.write(Implementation.genHeader(packageName, className, superClass))

                if (!isAbstract) {
                    managerInterface.write(Interface.ManagerClass.addClass(className))
                    managerImpl.write(Implementation.ManagerClass.addClass(className, eClass["name"].asString()))
                }

                eAttr.forEach {
                    val attrName = it["name"] as String
                    val eType = it["eType"] as String
                    val upperBound = (it["upperBound"] as Long).toInt()
                    val lowerBound = (it["lowerBound"] as Long).toInt()

                    interfaceWriter.write(Interface.genAttributeGetterAndSetter(attrName, eType, upperBound))
                    implWriter.write(Implementation.genAttributeGetterAndSetter(attrName, eType, upperBound, lowerBound))
                }

                eRef.forEach {
                    val refName = it["name"] as String
                    val endClass = it["refType"] as String
                    //val endSubClass = (it["refSubType"] as List<*>).joinToString(",") { s -> "'$s'" }
                    val containment = it["containment"] as Boolean
                    val upperBound = (it["upperBound"] as Long).toInt()   //neo4j maps INTEGER value to java long

                    interfaceWriter.write(Interface.genRefSetter(refName, endClass, upperBound))
                    implWriter.write(Implementation.genRefSetter(refName, endClass, upperBound, containment))
                }

                implWriter.write("\n}")
                interfaceWriter.write("\n}")
                interfaceWriter.close()
                implWriter.close()
            }

            managerInterface.write("}")
            managerImpl.write("}")
            managerImpl.close()
            managerInterface.close()

            //null
        }}
        return packageName
    }
}*/