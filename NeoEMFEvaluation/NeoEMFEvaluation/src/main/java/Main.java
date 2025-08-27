import fr.inria.atlanmod.neoemf.config.ImmutableConfig;
import fr.inria.atlanmod.neoemf.data.blueprints.neo4j.config.BlueprintsNeo4jConfig;
import fr.inria.atlanmod.neoemf.resource.PersistentResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import fr.inria.atlanmod.neoemf.data.blueprints.util.BlueprintsUriFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {
        // Path to your Neo4j store
        String dbPath = "C:\\Users\\lamp6\\.Neo4jDesktop2\\Data\\dbmss\\dbms-e3bd9e12-3000-4922-8ac8-8c53279facb2\\data\\databases\\neo4j";

        // Path to your .ecore file
        File sourceFile = new File("models/metamodel/wadl2.ecore");

        // Set up ResourceSet and register Ecore factory
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put("ecore", new EcoreResourceFactoryImpl());

        // Always register base Ecore package
        EPackage.Registry.INSTANCE.put(EcorePackage.eINSTANCE.getNsURI(), EcorePackage.eINSTANCE);

        // Create URIs
        URI sourceUri = URI.createFileURI(sourceFile.getAbsolutePath());
        URI targetUri = new BlueprintsUriFactory().createLocalUri(dbPath);

        // NeoEMF config
        ImmutableConfig config = new BlueprintsNeo4jConfig().autoSave();

        try (
                PersistentResource targetResource = (PersistentResource) resourceSet.createResource(targetUri)
        ) {
            // Load the metamodel
            Resource sourceResource = resourceSet.createResource(sourceUri);
            sourceResource.load(Collections.emptyMap());

            // Register all EPackages from the loaded .ecore
            for (Object obj : sourceResource.getContents()) {
                if (obj instanceof EPackage) {
                    EPackage pkg = (EPackage) obj;
                    EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
                }
            }

            // Copy into the persistent resource
            targetResource.getContents().addAll(EcoreUtil.copyAll(sourceResource.getContents()));
            targetResource.save(config.toMap());

            System.out.println("Saved metamodel to: " + targetResource.getURI());

        } catch (IOException e) {
            throw new RuntimeException("Error processing metamodel", e);
        }
    }
}
