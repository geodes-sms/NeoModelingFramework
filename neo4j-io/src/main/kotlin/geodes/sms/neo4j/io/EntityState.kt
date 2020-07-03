package geodes.sms.neo4j.io

enum class EntityState {
    NEW, PERSISTED, MODIFIED, PRE_REMOVED, REMOVED, DETACHED  //unloaded
}