package geodes.sms.nmf.neo4j.io

import org.neo4j.driver.Value

class ReferenceParameter(
    val type: String,
    val startNode: Entity,
    val endNode: Entity,
    val props: Map<String, Value>
)