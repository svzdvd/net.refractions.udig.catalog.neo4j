/**
 * 
 */
package net.refractions.udig.catalog.neo4j;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.data.DataStore;
import org.osgi.framework.BundleContext;


/**
 * @author Davide Savazzi
 */
public class Activator extends AbstractUIPlugin {

	// Constructor
	    

	public Activator() {
        super();
        
        openDataStores = new ArrayList<DataStore>();
        plugin = this;
    }
    
    
    // Public methods
    
	public void registerOpenDataStore(DataStore dataStore) {
		synchronized (openDataStores) {
			openDataStores.add(dataStore);
		}
	}
	
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        
        synchronized (openDataStores) {
        	for (DataStore dataStore : openDataStores) {
        		dataStore.dispose();
        	}
        	openDataStores.clear();
		}
        
        super.stop(context);
    }
    
    public static Activator getDefault() {
        return plugin;
    }
    
	
	// Attributes

    private List<DataStore> openDataStores;
    
    private static Activator plugin;

    public final static String ID = "net.refractions.udig.catalog.neo4j";
}