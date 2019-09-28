package geodes.sms.emfextension.ecore

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl


// MinimalEObjectImpl.Container() is EObjectImpl()
class MyEObjectImpl : Visitable, MinimalEObjectImpl.Container() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

class EClassImpl : Visitable, org.eclipse.emf.ecore.impl.EClassImpl() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

class EPackageImpl : Visitable, org.eclipse.emf.ecore.impl.EPackageImpl() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

class EDataTypeImpl : Visitable, org.eclipse.emf.ecore.impl.EDataTypeImpl() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

class EEnumImpl : Visitable, org.eclipse.emf.ecore.impl.EEnumImpl() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

class EEnumLiteralImpl : Visitable, org.eclipse.emf.ecore.impl.EEnumLiteralImpl() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

class EReferenceImpl : Visitable, org.eclipse.emf.ecore.impl.EReferenceImpl() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

class EAttributeImpl : Visitable, org.eclipse.emf.ecore.impl.EAttributeImpl() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

class EAnnotationImpl : Visitable, org.eclipse.emf.ecore.impl.EAnnotationImpl() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

