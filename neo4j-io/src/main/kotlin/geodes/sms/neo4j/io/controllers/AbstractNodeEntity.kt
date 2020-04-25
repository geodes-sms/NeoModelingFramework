package geodes.sms.neo4j.io.controllers

import geodes.sms.neo4j.io.StateListener

/*
abstract class AbstractNodeEntity : IEntity, StateListener.Creatable {
    override var id: Long = id
        private set

            override fun onCreate(id: Long) {
        this.id = id
    }

    override fun equals(other: Any?): Boolean {
        return other is INodeEntity && other.id == this.id
    }

    override fun hashCode(): Int = id.hashCode()
}*/