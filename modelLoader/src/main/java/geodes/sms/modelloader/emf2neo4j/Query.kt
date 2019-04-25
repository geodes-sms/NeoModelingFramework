package geodes.sms.modelloader.emf2neo4j


abstract class Query : Comparable<Query> {
    abstract val queryOrder: Int
    abstract val query: String

    override fun toString() : String {
        return query
    }

    /*
    override fun equals(other: Any?): Boolean {
        return when (other) {
            null, !is Query -> false
            else -> query == other.query
        }
        /*
        if(other == null || other !is Query)
            return false
        return query == other.query*/
    }

    override fun hashCode(): Int {
        return query.hashCode()
    }*/

    /**
     * Ascending order
     * TreeSet ues only compareTo() method. Not equals() and hashCode()
     * 0 means obj are the same and will not be added to TreeSet
     *
     * If a.compareTo(b) == 1 then b.compareTo(a) must be == -1
     */
    override fun compareTo(other: Query): Int {
        val res = this.queryOrder - other.queryOrder
        return when {
            res != 0 -> res
            else     -> this.query.compareTo(other.query)
        }
    }
}

class QueryMatch(alias: String, id: Long) : Query() {
    override val queryOrder = 1
    override val query = "MATCH ($alias) WHERE ID($alias) = $id"
}

class QueryCreateSingleNode(
    alias: String,
    label: String,
    propsAlias: String) : Query() {

    override val queryOrder = 2
    override val query = "CREATE ($alias:$label {$propsAlias})"
}

class QueryCreateRef(
    parentAlias: String,
    refName: String,
    isContainment: Boolean,
    childAlias: String) : Query() {

    override val queryOrder = 3
    override val query = "CREATE ($parentAlias)-[:$refName {containment:$isContainment}]->($childAlias)"
}

class QueryReturn(alias: Set<String>): Query() {
    override val queryOrder = 4
    override val query = "RETURN {${alias.joinToString { "$it:ID($it)" } }} AS nodeIDs"
    //"RETURN { alias1: ID(alias1), alias2: ID(alias2) ...}"
}

