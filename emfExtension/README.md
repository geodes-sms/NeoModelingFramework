Emf extension module provide useful extensions for emf.
1. A visitor pattern on Ecore MOF
    
Usage example:

    val model = "./EmfModel/Latex.ecore"
    
    // Global set registry: Resource.Factory.Registry.INSTANCE.extensionToFactoryMap
    // Local registry for resourceSet: resourceSet.resourceFactoryRegistry.extensionToFactoryMap
    Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.apply {
        put("ecore", EcoreResourceFactoryImpl())
        //put("xmi", XMIResourceFactoryImpl())
    }
    
    // custom factory
    EcorePackage.eINSTANCE.eFactoryInstance = VisitorEcoreFactory()

    val visitor = object: Visitor {
        override fun visit(eClass: EClass) {
            
        }
        //...
    }
    val resourceSet = ResourceSetImpl()
    val resource = resourceSet.getResource(URI.createFileURI(model), true)
    val iterator = EcoreUtil.getAllContents<Visitable>(resource, true)
    iterator.forEach {
        it.accept(visitor)
        //do smth
    }