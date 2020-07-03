package geodes.sms.neo4j.io

import org.neo4j.driver.Driver
import org.neo4j.driver.internal.value.MapValue
import org.neo4j.driver.internal.value.StringValue

class DBSchemaManager(private val driver: Driver) {

    fun createPropertyUniqueConstraint(nodeLabel: String, propertyName: String, constraintName: String) {
        val session = driver.session()
        session.writeTransaction { it.run("CREATE CONSTRAINT \$constraintName" +
                " ON (n:LabelName) ASSERT n.$propertyName IS UNIQUE",
            MapValue(mapOf("constraintName" to StringValue(constraintName))))
        }
        session.close()
    }

    fun dropPropertyUniqueConstraint(constraintName: String) {
        val session = driver.session()
        session.writeTransaction { it.run("DROP CONSTRAINT \$constraintName",
            MapValue(mapOf("name" to StringValue(constraintName))))
        }
        session.close()
    }
}