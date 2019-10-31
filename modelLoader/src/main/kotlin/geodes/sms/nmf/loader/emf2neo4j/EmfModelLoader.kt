package geodes.sms.nmf.loader.emf2neo4j

import geodes.sms.nmf.neo4j.io.INode
import org.eclipse.emf.common.util.TreeIterator
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import java.io.File


abstract class EmfModelLoader  {

    //abstract fun load(rootObj: EObject)
    abstract fun load(iterator: TreeIterator<EObject>)

    protected companion object {
        //val cache = hashMapOf<EObject, Pair<INode, Int>>()
        val cache = hashMapOf<EObject, INode>()
    }
}
