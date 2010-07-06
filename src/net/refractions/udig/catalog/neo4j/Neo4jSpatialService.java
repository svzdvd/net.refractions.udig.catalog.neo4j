package net.refractions.udig.catalog.neo4j;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.ICatalog;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IResolveChangeEvent;
import net.refractions.udig.catalog.IResolveDelta;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.URLUtils;
import net.refractions.udig.catalog.internal.CatalogImpl;
import net.refractions.udig.catalog.internal.ResolveChangeEvent;
import net.refractions.udig.catalog.internal.ResolveDelta;

import org.eclipse.core.runtime.IProgressMonitor;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStoreFactory;


/**
 * @author Davide Savazzi
 */
public class Neo4jSpatialService extends IService {

	// Constructor
    
    Neo4jSpatialService(URL url, Map<String, Serializable> params) {
        this.url = url;
        this.id = new ID(url);
        this.params = params;
    }
    
    
    // Methods

    public <T> T resolve(Class<T> adaptee, IProgressMonitor monitor) throws IOException {
        if (adaptee == null) {
            throw new NullPointerException("No adaptor specified");
        }
        
        if (adaptee.isAssignableFrom(Neo4jSpatialDataStore.class)) {
            return adaptee.cast(getDataStore(monitor));
        }
        
        if (adaptee.isAssignableFrom(File.class)) {
            return adaptee.cast(toFile());
        }
        
        return super.resolve(adaptee, monitor);
    }

    public <T> boolean canResolve(Class<T> adaptee) {
        if (adaptee == null) {
            return false;
        }
        
        return adaptee.isAssignableFrom(Neo4jSpatialDataStore.class) || super.canResolve(adaptee);
    }        
    
	public List<Neo4jSpatialGeoResource> resources(IProgressMonitor monitor) throws IOException {
		if (resources == null) {
			resources = new ArrayList<Neo4jSpatialGeoResource>();
			String[] layers = getDataStore(monitor).getTypeNames();
			for (int i = 0; i < layers.length; i++) {
				resources.add(new Neo4jSpatialGeoResource(this, layers[i]));
			}
		}

		return resources;
	}
        
    public Neo4jSpatialServiceInfo getInfo(IProgressMonitor monitor) throws IOException {
        return (Neo4jSpatialServiceInfo) super.getInfo(monitor);
    }
    	
    public Map<String, Serializable> getConnectionParams() {
        return params;
    }
    
    public void refresh(IProgressMonitor monitor) {
		Neo4jSpatialDataStore dataStore = (Neo4jSpatialDataStore) getDataStore(monitor);
		dataStore.clearCache();    	
		resources = null;
		
		// TODO compare old and new resources and
		// fire an Event for every added or deleted resource?
		fireChangeEvent(monitor);
    }
    
    public Neo4jSpatialDataStore getDataStore(IProgressMonitor monitor) {
    	if (dataStore == null) {
    		try {
    			dataStore = Activator.getDefault().getDataStore(params);
        		fireChangeEvent(monitor);
            } catch (IOException e) {
            	e.printStackTrace();
                error = e;
            }
        }
        return dataStore;
    }
    
    public Status getStatus() {
        return error != null ? Status.BROKEN : dataStore == null ? Status.NOTCONNECTED : Status.CONNECTED;
    }

    public Throwable getMessage() {
		return error;
	}
    
    public URL getIdentifier() {
        return url;
    }
    
    public ID getID() {
        return id;
    }    
    
    public File toFile() {
        Map<String, Serializable> parametersMap = getConnectionParams();
        URL url = (URL) parametersMap.get(Neo4jSpatialDataStoreFactory.URLP.key);
        return URLUtils.urlToFile(url);
    }    
    
    
    // Private methods
    
	protected IServiceInfo createInfo(IProgressMonitor monitor) throws IOException {
		if (info == null) { 
			Neo4jSpatialDataStore dataStore = getDataStore(monitor);
            if (dataStore == null) {
            	// could not connect
            	return null;
            }
                	
            info = new Neo4jSpatialServiceInfo(this);
        }
        return info;
	}
    
    private void fireChangeEvent(IProgressMonitor monitor) {
        IResolveDelta delta = new ResolveDelta(this, IResolveDelta.Kind.CHANGED);
        ResolveChangeEvent event = new ResolveChangeEvent(this, IResolveChangeEvent.Type.POST_CHANGE, delta);
        
        ICatalog catalog = parent(monitor);
        if (catalog instanceof CatalogImpl) {
            ((CatalogImpl) catalog).fire(event);
        }
    }    
    
    
	// Attributes
	
    private URL url;
    private ID id;
    private Map<String, Serializable> params = null;    
    private Throwable error;    
    private Neo4jSpatialDataStore dataStore;
    private List<Neo4jSpatialGeoResource> resources;
}