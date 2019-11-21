package util

/**
 * Modifiable list with maximum size
 */
class BoundedList<E>(val upperBound: Int): ArrayList<E>(upperBound) {

    override fun add(element: E): Boolean {
        return if (size < upperBound)
            super.add(element)
        else false
    }

    override fun add(index: Int, element: E) {
        if (index in 0 until upperBound)
            super.add(index, element)
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        return if (index in 0 until upperBound && size + elements.size <= upperBound)
            super.addAll(index, elements)
        else false
    }

    override fun addAll(elements: Collection<E>): Boolean {
        return if (size + elements.size <= upperBound)
            super.addAll(elements)
        else false
    }
}