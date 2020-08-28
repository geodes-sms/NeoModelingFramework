package geodes.sms.neo4j.io.util

import geodes.sms.neo4j.io.exception.LoverBoundExceedException
import geodes.sms.neo4j.io.exception.UpperBoundExceedException
import java.util.*

/** Modifiable list with maximum size */
class BoundedList<E>(val upperBound: Int, val loverBound: Int) {//: ArrayList<E>(upperBound) {
    private val data = LinkedList<E>()

    fun add(element: E) =
        if (data.size < upperBound) data.add(element)
        else throw UpperBoundExceedException(upperBound, "")

    fun pop(): E =
        if (data.size > loverBound) data.pop()
        else throw LoverBoundExceedException(loverBound, "")

    fun clear() = data.clear()
}