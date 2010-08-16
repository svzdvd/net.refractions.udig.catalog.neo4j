/**
 * 
 */
package net.refractions.udig.catalog.neo4j;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.project.ILayer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.data.DataStore;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStoreFactory;
import org.osgi.framework.BundleContext;


/**
 * Neo4j Plugin Activator. 
 * It mantains an index of open Neo4j DataStores. 
 * 
 * @author Davide Savazzi
 */
public class Activator extends AbstractUIPlugin {

	// Constructor
	    

	public Activator() {
        super();
        
        openDataStores = new HashMap<String,Neo4jSpatialDataStore>();
        dataStorefactory = new Neo4jSpatialDataStoreFactory();
        plugin = this;
    }
    
    
    // Public methods
    
	/**
	 * Return a Neo4j DataStore.
	 */
	public Neo4jSpatialDataStore getDataStore(Map<String, Serializable> params) throws IOException {
		if (dataStorefactory.canProcess(params)) {
			String id = dataStorefactory.getDataStoreUniqueIdentifier(params);
			synchronized (openDataStores) {
				Neo4jSpatialDataStore dataStore = openDataStores.get(id);
				if (dataStore == null) {
	    			dataStore = (Neo4jSpatialDataStore) dataStorefactory.createDataStore(params);					
	    			openDataStores.put(id, dataStore);
	    			
	        		log("Opened Neo4j Database: " + id);
				}
				return dataStore;
    		}
		} else {
			// invalid parameters
			return null;
		}
	}
	
	/**
	 * Close all open DataStores.
	 */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        
        synchronized (openDataStores) {
        	for (String id : openDataStores.keySet()) {
        		DataStore dataStore = openDataStores.get(id);
        		dataStore.dispose();
        	}
        	openDataStores.clear();
		}
        
        super.stop(context);
    }

    public Neo4jSpatialService getLayerService(ILayer layer, IProgressMonitor monitor) {
		try {
			IService service = layer.getGeoResource().service(monitor);
			if (service instanceof Neo4jSpatialService) {
				return (Neo4jSpatialService) service;
			}			
		} catch (IOException e) {
			log(e.getMessage(), e);
		}

		return null;
    }
    
    public ILayer findLayer(String layerName, List<ILayer> layers) {
		for (ILayer layer : layers) {
			if (layer.getName().equals(layerName)) {
				return layer;
			}
		}
		return null;
    }
    
    /**
     * Return a static reference to this Activator
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Open an Error Message Dialog
     */
    public static void openError(final Display display, final String title, final String message) {
		display.asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(
						display.getActiveShell(),
						title,
						message);
			}});    	
    }
    
    public static void log(String message) {
    	System.out.println(message);
        // TODO getDefault().getLog().log(new Status(IStatus.INFO, ID, message));
    }        
    
    public static void log(String message, Throwable t) {
    	t.printStackTrace();
    	System.out.println(message);
        // int status = t instanceof Exception || message != null ? IStatus.ERROR : IStatus.WARNING;
        // TODO getDefault().getLog().log(new Status(status, ID, IStatus.OK, message, t));
    }    
	
    
	// Attributes

    private Map<String,Neo4jSpatialDataStore> openDataStores;
    private Neo4jSpatialDataStoreFactory dataStorefactory = new Neo4jSpatialDataStoreFactory();
    private static Activator plugin;

    public final static String ID = "net.refractions.udig.catalog.neo4j";
}