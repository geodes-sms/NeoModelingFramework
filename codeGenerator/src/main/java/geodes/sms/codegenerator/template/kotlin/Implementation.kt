package geodes.sms.codegenerator.template.kotlin


object Implementation {

    fun genHeader(packageName: String, className: String, superTypes: List<String>) = """
        package  geodes.sms.modeleditor.$packageName.neo4jImpl

        import geodes.sms.modeleditor.$packageName.*
        import geodes.sms.neo4jecore.Neo4jEObject
        import geodes.sms.neo4jecore.Neo4jEObjectImpl
        import org.neo4j.driver.v1.Session
        import org.neo4j.driver.v1.Values

        class ${className}Neo4jImpl(override val dbSession: Session, override val id: Int, override val label: String) :$className
        ${superTypes.joinToString("") { ", $it by ${it}Neo4jImpl(dbSession, id, label)" }}
        ${when(superTypes.size) {
            0 -> ", Neo4jEObject by Neo4jEObjectImpl(dbSession, id, label) {"
            1 -> "{"
            else -> "{\n" + genCommonFunctions()
        }}

        """.trimIndent()


    private fun genCommonFunctions() = """
    override fun remove(): Boolean {
        return try {
            dbSession.writeTransaction {it.run("MATCH (n:${'$'}label) WHERE ID(n)={nodeID}" +
                    " OPTIONAL MATCH (n)-[* {containment:true}]->(m)" +
                    " DETACH DELETE n, m",
                Values.parameters("nodeID", this.id))}
            true
        } catch (e : Exception) {
            e.printStackTrace()
            false
        }
    }

    """.trimIndent()


    fun genAttributeGetterAndSetter(attrName: String, eType: String, upperBound: Int, lowerBound: Int) : String {
        val type = Util.type.getOrDefault(eType, eType)
        val constraint = when (upperBound) {
            -1, -2 -> if (lowerBound != 0) "if (attrValue.size < $lowerBound) false else" else ""
            1 -> ""
            else -> if (lowerBound != 0) "if (attrValue.size < $lowerBound || attrValue.size > $upperBound) false else"
                    else "if (attrValue.size > $upperBound) false else"
        }

        return """
        override fun set${attrName.capitalize()}(attrValue: ${if(upperBound == 1) type else "List<$type>"}) : Boolean {
            return $constraint try {
                dbSession.writeTransaction {it.run("MATCH (c:${'$'}label) WHERE ID(c)={nodeID} ${
                when (eType) {
                    "java.util.Map" -> " MERGE (c)-[:$attrName{containment: true}]->(map) SET map={value}"
                    else -> " SET c.$attrName = {value}"
                }}",
                Values.parameters("nodeID", this.id, "value", ${when(eType){
                    "java.math.BigInteger", "java.math.BigDecimal" -> "attrValue.toString()"
                    "java.util.Date" -> "java.time.ZonedDateTime.ofInstant(attrValue.toInstant(), java.time.ZoneId.systemDefault())"
                    else -> "attrValue"
                }}))}
                true
            } catch (e : Exception) {
                e.printStackTrace()
                false
            }
        }

        override fun get${attrName.capitalize()}() : ${if(upperBound == 1) "$type?" else "List<$type>?"} {
            //return ${if(upperBound == 1) type else "emptyList<$type>()"}
            return  try {
                val res = dbSession.readTransaction {it.run("MATCH (c:${'$'}label) WHERE ID(c) = {nodeID} ${
                    when(eType) {
                        "java.util.Map" -> "MATCH (c)-[:$attrName]->(map) RETURN map {.*} AS p"
                        else -> "RETURN c.$attrName AS p"
                    }}",
                    Values.parameters("nodeID", this.id))}
                ${if(upperBound == 1) Util.returnSingleType[type] else Util.returnListType[type]}
            } catch (e : Exception) {
                e.printStackTrace()
                null
            }
        }
        """
    }

    /**
     * Create new reference on DB between this node and endNode.
     * According to EMF reference may be added only when when endNode is not contained by any other node
     * and ref upperBound is not exceeded
     * @return boolean whether reference has been added
     */
    fun genRefSetter(refName: String, endClass: String, upperBound: Int, containment: Boolean) : String {

        val startNodeLabel = "${'$'}label"
        val endNodeLabel = "${'$'}{endNode.label}"

        return when (upperBound) {
            -1 -> """
                override fun add${refName.capitalize()}(endNode: ${endClass.capitalize()}) : Boolean {
                    return try {
                        val res = dbSession.writeTransaction {it.run("MATCH (c:$startNodeLabel) WHERE ID(c) = {startNodeID}" +
                            " MATCH (e:$endNodeLabel) WHERE ID(e) = {endNodeID}" +
                            " OPTIONAL MATCH (e)<-[{containment: true}]-(endNodeContainer)" +
                            " WITH c, e, COUNT(endNodeContainer) = 0 AS predicate" +
                            " FOREACH (ignoreMe IN CASE WHEN predicate THEN [1] ELSE [] END |" +
                            "   CREATE (c)-[:$refName {containment:{containment}}]->(e))" +
                            " RETURN predicate AS isAdded",
                            Values.parameters("startNodeID", this.id, "endNodeID", endNode.id, "containment", $containment))}
                        res.single()["isAdded"].asBoolean()
                    } catch (e : Exception) {
                        e.printStackTrace()
                        false
                    }
                }
                """
            1 -> """
                override fun set${refName.capitalize()}(endNode: ${endClass.capitalize()}) : Boolean {
                    return try {
                        val res = dbSession.writeTransaction {it.run("MATCH (c:$startNodeLabel) WHERE ID(c)={startNodeID}" +
                            " MATCH (e:$endNodeLabel) WHERE ID(e)={endNodeID}" +
                            " OPTIONAL MATCH (c)-[r:$refName {containment: {containment}}]->(:$endNodeLabel)" +
                            " OPTIONAL MATCH (e)<-[{containment: true}]-(endNodeContainer)" +
                            " WITH c, e, r, COUNT(endNodeContainer) = 0 AS predicate" +
                            " FOREACH (ignoreMe IN CASE WHEN predicate THEN [1] ELSE [] END |" +
                            "   DELETE r   MERGE (c)-[:$refName {containment:{containment}}]->(e))" +
                            " RETURN predicate AS isAdded",
                            Values.parameters("startNodeID", this.id, "endNodeID", endNode.id, "containment", $containment))}
                        res.single()["isAdded"].asBoolean()
                    } catch (e : Exception) {
                        e.printStackTrace()
                        false
                    }
                 }
                 """
            //if upperBound > 1; upperBound is limited
            else  -> """
                override fun add${refName.capitalize()}(endNode: ${endClass.capitalize()}) : Boolean {
                    return try {
                        val res = dbSession.writeTransaction { it.run("MATCH (c:$startNodeLabel) WHERE ID(c) = {startNodeID}" +
                            " MATCH (e:$endNodeLabel) WHERE ID(e) = {endNodeID}" +
	  						" OPTIONAL MATCH (c)-[r:$refName {containment:true}]->(:$endNodeLabel)" +
                            " OPTIONAL MATCH (e)<-[{containment: true}]-(endNodeContainer)" +
                            " WITH c, e, COUNT(r) < $upperBound AND COUNT(endNodeContainer) = 0 AS predicate" +
                            " FOREACH (ignoreMe IN CASE WHEN predicate THEN [1] ELSE [] END | " +
                            "  CREATE (c)-[r:$refName{containment:{containment}}]->(e))" +
                            " RETURN predicate AS isAdded",
                            Values.parameters("startNodeID", this.id, "endNodeID", endNode.id, "containment", $containment))}
                        res.single()["isAdded"].asBoolean()
                    } catch (e : Exception) {
                        e.printStackTrace()
                        false
                    }
                }
                """
        }
    }


    object ManagerClass {

        fun genHeader(packageName: String) = """
        package  geodes.sms.modeleditor.$packageName.neo4jImpl

        import org.neo4j.driver.v1.AuthTokens
        import org.neo4j.driver.v1.GraphDatabase
        import org.neo4j.driver.v1.Values
        import geodes.sms.modeleditor.$packageName.*

        class ModelManagerNeo4jImpl(uri: String, username: String, password: String): ModelManager, AutoCloseable {

            private val driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password))
            private val session = driver.session()

            override fun close() {
                session.close()
                driver.close()
            }

        """.trimIndent()

        fun addClass(className: String, originalLabel: String) = """
            override fun create$className() : $className {
                val id = session.readTransaction {
                    val result = it.run("CREATE (c:$originalLabel) RETURN ID(c) AS ID")
                    result.single()["ID"].asInt()
                }
                return ${className}Neo4jImpl(session, id, "$originalLabel")
            }

            override fun get${className}ByID(id: Int) : $className? {
                return session.readTransaction {
                    val res = it.run("MATCH (c:$originalLabel) WHERE ID(c)={id} RETURN COUNT(c) AS num",
                        Values.parameters("id", id))
                    if (res.single()["num"].asInt() == 1) ${className}Neo4jImpl(session, id, "$originalLabel") else null
                }
            }
        """
    }
}

