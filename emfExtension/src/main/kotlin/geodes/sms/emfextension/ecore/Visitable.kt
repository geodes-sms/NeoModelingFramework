package geodes.sms.emfextension.ecore

interface Visitable {
    fun accept(visitor: Visitor)
}