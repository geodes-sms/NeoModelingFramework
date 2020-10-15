package geodes.sms.neo4j.io.entity

interface INodeEntity : IEntity {
    //val labels: List<String>
    val label: String
}