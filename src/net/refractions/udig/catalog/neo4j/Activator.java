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
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.data.DataStore;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStoreFactory;
import org.osgi.framework.BundleContext;


/**
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
    
	public Neo4jSpatialDataStore getDataStore(Map<String, Serializable> params) throws IOException {
		if (dataStorefactory.canProcess(params)) {
			String id = dataStorefactory.getDataStoreUniqueIdentifier(params);
			synchronized (openDataStores) {
				Neo4jSpatialDataStore dataStore = openDataStores.get(id);
				if (dataStore == null) {
	    			dataStore = (Neo4jSpatialDataStore) dataStorefactory.createDataStore(params);					
	    			openDataStores.put(id, dataStore);
	    			
	        		System.out.println("Opened Neo4j Database: " + id);
				}
				return dataStore;
    		}
		} else {
			// invalid parameters
			return null;
		}
	}
	
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        
        synchronized (openDataStores) {
        	for (String id : openDataStores.keySet()) {
        		DataStore dataStore = openDataStores.get(id);
        		dataStore.dispose();
        		
        		System.out.println("Closed Neo4j Database: " + id);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
    
    public static Activator getDefault() {
        return plugin;
    }
    
	
	// Attributes

    private Map<String,Neo4jSpatialDataStore> openDataStores;
    private Neo4jSpatialDataStoreFactory dataStorefactory = new Neo4jSpatialDataStoreFactory();
    private static Activator plugin;

    public final static String ID = "net.refractions.udig.catalog.neo4j";
}