package geodes.sms.emfextension.ecore

import org.eclipse.emf.ecore.*

interface Visitor {
    fun visit(eObj: EObject)
    fun visit(ePackage: EPackage)
    fun visit(eClass: EClass)
    fun visit(eDataType: EDataType)
    fun visit(eEnum: EEnum)
    fun visit(eEnumLiteral: EEnumLiteral)
    fun visit(eReference: EReference)
    fun visit(eAttribute: EAttribute)
    fun visit(eAnnotation: EAnnotation)
    fun visit(eOperation: EOperation)
    fun visit(eParameter: EParameter)
}