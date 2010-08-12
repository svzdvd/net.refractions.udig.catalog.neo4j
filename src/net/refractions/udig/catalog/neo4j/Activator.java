/**
 * 
 */
package net.refractions.udig.catalog.neo4j;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceFactory;
import net.refractions.udig.project.ILayer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.data.DataStore;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStoreFactory;
import org.osgi.framework.BundleContext;


/**
 * @author Davide Savazzi
 */
public class Activator extends AbstractUIPlugin implements IStartup {

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
	    			
	        		log("Opened Neo4j Database: " + id);
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
        		
        		// TODO log is already stopped? it throws NPE...
        		log("Closed Neo4j Database: " + id);
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
    
    public static Activator getDefault() {
        return plugin;
    }

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

	@Override
    public void earlyStartup() {
		if (this.openDataStores.size() == 0) {
			ensureDefaultDatabasesLoaded();
		}
    }

	private void ensureDefaultDatabasesLoaded() {
        HashSet<File> dbDirs = new HashSet<File>();
        for(String path: new String[]{".",System.getenv("HOME")}) {
        	try {
	        	File dir = new File(path);
	        	if(dir.exists() && dir.isDirectory()) {
		            for(String subdir: new String[]{"dev/neo4j","neo4j",".amanzi","workspace"}) {
		            	findDbDirs(dbDirs, new File(dir.getCanonicalFile(),subdir), 0);
		            }
	        	}
        	}catch(Exception e){
        		System.err.println("Failed to perform search at '" + path + "': "+e);
        	}
        }
        if(dbDirs.size()>0){
    	    ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
            IServiceFactory serviceFactory = CatalogPlugin.getDefault().getServiceFactory();
	        for(File dir:dbDirs){
	        	try {
	                URL url = new File(dir,"neostore.id").toURI().toURL();
	                System.out.println("Searching for service for "+url);
	                for(IService service:serviceFactory.createService(url)){
	                	System.out.println("Found service: "+service);
                		catalog.add(service);
	                }
                } catch (MalformedURLException e) {
                	System.err.println("Failed to find service for "+dir);
	                e.printStackTrace();
                }
	        }
        }
    }
    
    private void findDbDirs(HashSet<File> dbDirs, File dir, int depth) {
    	if(depth > 4) return;
    	String[] files = dir.list();
    	if(files.length > 50) return;
       	for(String file:files){
    		if(file.startsWith("neostore")){
    			System.out.println("Found Neo4j Database at: "+dir);
    			dbDirs.add(dir);
    			return;
    		}
    	}
       	for(String file:files){
       		File f;
            try {
	            f = new File(dir.getCanonicalFile(),file);
	       		if(f.isDirectory()) {
	       			findDbDirs(dbDirs,f,depth+1);
	       		}
            } catch (IOException e) {
            }
    	}
   	}

}