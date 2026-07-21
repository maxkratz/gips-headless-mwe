package mwemetamodel;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.emoflon.smartemf.persistence.SmartEMFResourceFactoryImpl;

public class MweModelGenerator {

	private Root root;

	public MweModelGenerator() {
		root = MwemetamodelFactory.eINSTANCE.createRoot();
	}

	public void genGuestNode(final String name) {
		Objects.requireNonNull(name);
		final Guest guest = MwemetamodelFactory.eINSTANCE.createGuest();
		guest.setName(name);
		root.getGuests().add(guest);
	}

	public void genHostNode(final String name, final int capacity) {
		Objects.requireNonNull(name);
		final Host host = MwemetamodelFactory.eINSTANCE.createHost();
		host.setName(name);
		host.setCapacity(capacity);
		root.getHosts().add(host);
	}

	public void persistModel(final String path) {
		Objects.requireNonNull(path);

		// Workaround: Always use absolute path
		final URI absPath = URI.createFileURI(System.getProperty("user.dir") + "/" + path);

		// Create new model for saving
		final ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new SmartEMFResourceFactoryImpl(null));
		// ^null is okay if all paths are absolute
		final Resource r = rs.createResource(absPath);
		// Fetch model contents from eMoflon
		r.getContents().add(root);
		try {
			r.save(null);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public Root loadModel(final String path) {
		Objects.requireNonNull(path);

		// Workaround: Always use absolute path
		final URI absPath = URI.createFileURI(System.getProperty("user.dir") + "/" + path);

		final ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new SmartEMFResourceFactoryImpl(null));
		// ^null is okay if all paths are absolute
		final Resource res = rs.getResource(absPath, true);
		root = (Root) res.getContents().get(0);
		return root;
	}

}
