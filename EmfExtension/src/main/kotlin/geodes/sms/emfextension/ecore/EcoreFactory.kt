package geodes.sms.emfextension.ecore

import org.eclipse.emf.ecore.impl.EcoreFactoryImpl

/** Creates visitable instances */
class EcoreFactory : EcoreFactoryImpl() {

    override fun createEPackage() = EPackageImpl()
    override fun createEClass() = EClassImpl()
    override fun createEAnnotation() = EAnnotationImpl()
    override fun createEAttribute() = EAttributeImpl()
    override fun createEReference() = EReferenceImpl()
    override fun createEDataType()  = EDataTypeImpl()
    override fun createEEnum() = EEnumImpl()
    override fun createEEnumLiteral() = EEnumLiteralImpl()
}