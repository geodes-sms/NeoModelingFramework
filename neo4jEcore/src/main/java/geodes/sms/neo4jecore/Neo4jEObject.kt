package geodes.sms.neo4jecore

import org.neo4j.driver.v1.Session

interface Neo4jEObject {
    val id : Int
    val label : String  //== metaClass name
    val dbSession: Session

    /**
     * Remove node and all contents node (where containment equals true)
     */
    fun remove() : Boolean

    //fun eClass() : Neo4jEClass
    //fun eContainer() : Neo4jEObject
    //fun eContents() : List<Neo4jEObject>
    //fun allContents() : Iterator
}