import fr.inria.atlanmod.neoemf.config.ImmutableConfig;
import fr.inria.atlanmod.neoemf.data.blueprints.neo4j.config.BlueprintsNeo4jConfig;
import fr.inria.atlanmod.neoemf.resource.PersistentResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.resource.Resource;
import fr.inria.atlanmod.neoemf.data.blueprints.util.BlueprintsUriFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.io.IOException;
import java.util.Collections;

public class Main {

    public static void main(String[] args) {

        ImmutableConfig config = new BlueprintsNeo4jConfig().autoSave();
        BlueprintsUriFactory factory = new BlueprintsUriFactory();
        URI sourceUri = URI.createURI("model/sample.xmi");
        URI targetUri = new BlueprintsUriFactory().createLocalUri("databases/sample.graphdb");
        ResourceSet resourceSet = new ResourceSetImpl();
        try (PersistentResource targetResource = (PersistentResource) resourceSet.createResource(targetUri)) {
            targetResource.save(config.toMap());

//            Stopwatch stopwatch = Stopwatch.createStarted();

            Resource sourceResource = resourceSet.createResource(sourceUri);
            sourceResource.load(Collections.emptyMap());

            targetResource.getContents().addAll(EcoreUtil.copyAll(sourceResource.getContents()));
            targetResource.save(config.toMap());

            System.out.printf(targetResource.getURI().toString());
//            stopwatch.stop();
//            Log.info("Model created in {0} seconds", stopwatch.elapsed().getSeconds());

//            Helpers.compare(sourceResource, targetResource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
