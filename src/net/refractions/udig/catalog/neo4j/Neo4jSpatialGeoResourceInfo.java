/**
 * 
 */
package net.refractions.udig.catalog.neo4j;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.refractions.udig.catalog.IGeoResourceInfo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * @author Davide Savazzi
 */
public class Neo4jSpatialGeoResourceInfo extends IGeoResourceInfo {

	// Constructor
	
	Neo4jSpatialGeoResourceInfo(Neo4jSpatialGeoResource neo4jSpatialGeoResource, IProgressMonitor monitor)  throws IOException {	
		this.neo4jSpatialGeoResource = neo4jSpatialGeoResource;
		
		Neo4jSpatialDataStore dataStore = neo4jSpatialGeoResource.service().getDataStore(monitor);
		String typename = neo4jSpatialGeoResource.getTypeName();
		
        featureType = dataStore.getSchema(typename);		
        bounds = dataStore.getBounds(typename);
	}

	
	// Public methods

	public String getName() {
		return neo4jSpatialGeoResource.getTypeName();
	}	

    public String getTitle() {
        return getName();
    }	
	
    public CoordinateReferenceSystem getCRS() {
        return featureType.getCoordinateReferenceSystem();
    }    

    public URI getSchema() {
    	try {
			return new URI(featureType.getName().getNamespaceURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
    }
	
	public ReferencedEnvelope getBounds() {
		return bounds;
	}
	
	
	// Attributes
	
	private Neo4jSpatialGeoResource neo4jSpatialGeoResource;
	private SimpleFeatureType featureType;	
	private ReferencedEnvelope bounds;	
}
