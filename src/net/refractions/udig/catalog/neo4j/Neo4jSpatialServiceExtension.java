package net.refractions.udig.catalog.neo4j;

import java.awt.RenderingHints.Key;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.AbstractDataStoreServiceExtension;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.URLUtils;

import org.geotools.data.DataStore;
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
		return new DataStoreFactorySpi() {
	        private Neo4jSpatialDataStoreFactory dataStoreFactory = new Neo4jSpatialDataStoreFactory();

			public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
				return Activator.getDefault().getDataStore(params);
			}

			public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
				// TODO
				throw new UnsupportedOperationException();
			}

			public boolean canProcess(Map<String, Serializable> params) {
				return dataStoreFactory.canProcess(params);
			}

			public String getDescription() {
				return dataStoreFactory.getDescription();
			}

			public String getDisplayName() {
				return dataStoreFactory.getDisplayName();
			}

			public Param[] getParametersInfo() {
				return dataStoreFactory.getParametersInfo();
			}

			public boolean isAvailable() {
				return dataStoreFactory.isAvailable();
			}

			public Map<Key, ?> getImplementationHints() {
				return dataStoreFactory.getImplementationHints();
			}
		};
	}
}