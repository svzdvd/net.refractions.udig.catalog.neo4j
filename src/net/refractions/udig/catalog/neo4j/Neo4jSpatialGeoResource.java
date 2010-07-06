package net.refractions.udig.catalog.neo4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.URLUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.styling.Style;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 * @author Davide Savazzi
 */
public class Neo4jSpatialGeoResource extends IGeoResource {

	// Constructor

    Neo4jSpatialGeoResource(Neo4jSpatialService service, String typename) {
        this.service = service;
        this.parent = service;        
        this.typename = typename;
        
        try {
            identifier = new URL(parent.getIdentifier().toString() + "#" + URLUtils.cleanFilename(typename));
            id = new ID(parent.getID(), URLUtils.cleanFilename(typename));
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        	
            identifier = parent.getIdentifier();
        }        
    }	
	
    
    // Public methods
    
    public URL getIdentifier() {
        return identifier;
    }
    
    public ID getID() {
        return id;
    }
	
    public Status getStatus() {
        return parent.getStatus();
    }

    public Throwable getMessage() {
        return parent.getMessage();
    }
    
    public <T> T resolve(Class<T> adaptee, IProgressMonitor monitor) throws IOException {
        if (adaptee == null) {
            return null;
        }
        
        if (adaptee.isAssignableFrom(IGeoResource.class)) {
            return adaptee.cast(this);
        }
        
        if (adaptee.isAssignableFrom(IGeoResourceInfo.class)) {
            return adaptee.cast(createInfo(monitor));
        }
        
        if (adaptee.isAssignableFrom(FeatureStore.class)) {
            FeatureSource<SimpleFeatureType, SimpleFeature> fs = featureSource(monitor);
            if (fs instanceof FeatureStore) {
                return adaptee.cast(fs);
            }
        }
        
        if (adaptee.isAssignableFrom(FeatureSource.class)) {
            return adaptee.cast(featureSource(monitor));
        }
        
        if (adaptee.isAssignableFrom(Neo4jSpatialDataStore.class)) {
            return adaptee.cast(parent.getDataStore(monitor));
        }
        
        if (adaptee.isAssignableFrom(Style.class)) {
        	Style style = style(monitor);
        	if (style != null) {
                return adaptee.cast(style(monitor));
        	}
        }
        
        return super.resolve(adaptee, monitor);
    }

    public Style style(IProgressMonitor monitor) {
    	// don't return a style
        return null;
    }

    public Neo4jSpatialService service() {
    	return parent;
    }
    
    public <T> boolean canResolve(Class<T> adaptee) {
        if (adaptee == null) {
        	return false;
        }
        
        return (adaptee.isAssignableFrom(IGeoResourceInfo.class) || 
                adaptee.isAssignableFrom(FeatureStore.class) || 
                adaptee.isAssignableFrom(FeatureSource.class) || 
                adaptee.isAssignableFrom(IService.class) ||
                adaptee.isAssignableFrom(Style.class)) ||
                super.canResolve(adaptee);
    }
    
    public Neo4jSpatialGeoResourceInfo getInfo(IProgressMonitor monitor) throws IOException {
        return (Neo4jSpatialGeoResourceInfo) super.getInfo(monitor);
    }
    	
    public String getTypeName() {
		return typename;
	}
	

    // Private methods
   
    private FeatureSource<SimpleFeatureType, SimpleFeature> featureSource(IProgressMonitor monitor) throws IOException {
        return parent.getDataStore(monitor).getFeatureSource(typename);
    }

    protected Neo4jSpatialGeoResourceInfo createInfo(IProgressMonitor monitor) throws IOException{
        return new Neo4jSpatialGeoResourceInfo(this, monitor);
    }
    
    
	// Attributes

	private Neo4jSpatialService parent;
    private String typename;
    private URL identifier;
    private ID id;
}