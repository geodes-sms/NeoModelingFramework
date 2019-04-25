package geodes.sms.codegenerator

import geodes.sms.codegenerator.template.kotlin.Implementation
import geodes.sms.codegenerator.template.kotlin.Interface
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Values
import java.io.File

object CodeGenerator {

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
            //TODO MATCH (p:EPackage)-->(c:EClass or EDataType) to query all EClassifiers (EClass and EDataType)
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

    fun generateFromEmfMetampdel() {}

}