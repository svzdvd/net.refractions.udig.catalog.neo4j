package net.refractions.udig.catalog.neo4j;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.AbstractDataStoreServiceExtension;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.URLUtils;

import org.geotools.data.DataStoreFactorySpi;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStoreFactory;


/**
 * @author Davide Savazzi
 */
public class Neo4jSpatialServiceExtension extends AbstractDataStoreServiceExtension {
    
    // Public methods
    
	public Map<String, Serializable> createParams(URL url) {
		File file = URLUtils.urlToFile(url);
	    if (file.exists()) {
	    	Map<String, Serializable> params = new HashMap<String, Serializable>();
	        params.put(Neo4jSpatialDataStoreFactory.URLP.key, url);
	        if (getDataStoreFactory().canProcess(params)) {
	        	return params;
	        }
	    }
	    
	    // unable to create the parameters, url must be for another service
	    return null;
	}
	
	public IService createService(URL id, Map<String, Serializable> params) {
        if (params.containsKey(Neo4jSpatialDataStoreFactory.URLP.key)) {
            URL url = null;
            if (params.get(Neo4jSpatialDataStoreFactory.URLP.key) instanceof URL) {
                url = (URL) params.get(Neo4jSpatialDataStoreFactory.URLP.key);
            } else {
            	try {
                    url = (URL) Neo4jSpatialDataStoreFactory.URLP.parse(params.get(Neo4jSpatialDataStoreFactory.URLP.key).toString());
                    params.put(Neo4jSpatialDataStoreFactory.URLP.key, url);
                } catch (Throwable e) {
                    e.printStackTrace();
                    return null;
                }
            }
            
            String file = url.getFile();
            if (!file.endsWith("neostore.id")) {
            	return null;
            }
            
            if (id == null) {
            	return new Neo4jSpatialService(url, params);
            } else {
            	return new Neo4jSpatialService(id, params);
            }
        }
		
		// key not found
		return null;
	}

	public String reasonForFailure(URL url) {
		// TODO
		return "";
	}

	
	// Private methods
	
	protected DataStoreFactorySpi getDataStoreFactory() {
        if (dataStoreFactory == null) {
        	dataStoreFactory = new Neo4jSpatialDataStoreFactory();
        }

        return dataStoreFactory;
	}
	
	
	// Attributes

	private Neo4jSpatialDataStoreFactory dataStoreFactory;
}