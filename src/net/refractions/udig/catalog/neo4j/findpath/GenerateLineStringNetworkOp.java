/**
 * 
 */
package net.refractions.udig.catalog.neo4j.findpath;

import java.util.Iterator;
import java.util.List;

import net.refractions.udig.catalog.neo4j.Activator;
import net.refractions.udig.catalog.neo4j.Neo4jSpatialGeoResource;
import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.neo4j.gis.spatial.Constants;
import org.neo4j.gis.spatial.DefaultLayer;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.LineStringNetworkGenerator;
import org.neo4j.gis.spatial.Search;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.WKBGeometryEncoder;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.gis.spatial.query.SearchAll;
import org.neo4j.graphdb.Transaction;


/**
 * @author Davide Savazzi
 */
public class GenerateLineStringNetworkOp implements IOp {

	public void op(Display display, Object target, IProgressMonitor monitor) throws Exception {
		if (monitor == null) monitor = new NullProgressMonitor();
		
		Neo4jSpatialGeoResource geoResource = (Neo4jSpatialGeoResource) target;
		Neo4jSpatialDataStore dataStore = (Neo4jSpatialDataStore) geoResource.service().getDataStore(monitor);
		SpatialDatabaseService spatialDatabase = dataStore.getSpatialDatabaseService();
		DefaultLayer layer = (DefaultLayer) spatialDatabase.getLayer(geoResource.getTypeName());

		if (layer == null) {
			Activator.log("Layer NOT found: " + layer);
			Activator.openError(display, "Error creating Network", "Unable to retrieve Layer");
			return;
		} 
		
		Integer geomType = layer.getOrGuessGeometryType();
		if (geomType == null) {
			Activator.openError(display, "Error creating Network", "Unable to read Layer Geometry Type");
			return;			
		}
		
		if (geomType != Constants.GTYPE_LINESTRING && geomType != Constants.GTYPE_MULTILINESTRING) {
			Activator.openError(display, "Error creating Network", "A Network can be created only with a (Multi) LineString Layer");			
			return;
		}
			
		LineStringNetworkGenerator networkGenerator;
			
	    Transaction tx = dataStore.beginTx();
	    try {
	    	// TODO put these layer nodes in relationship?
	        	
	        DefaultLayer netPointsLayer = (DefaultLayer) spatialDatabase.getOrCreateLayer(layer.getName() + " - network points", WKBGeometryEncoder.class, DefaultLayer.class);
	        netPointsLayer.setCoordinateReferenceSystem(layer.getCoordinateReferenceSystem());
	        	
	        DefaultLayer netEdgesLayer = (DefaultLayer) spatialDatabase.getOrCreateLayer(layer.getName() + " - network edges", WKBGeometryEncoder.class, DefaultLayer.class);
	        netEdgesLayer.setCoordinateReferenceSystem(layer.getCoordinateReferenceSystem());
	        	
	        networkGenerator = new LineStringNetworkGenerator(netPointsLayer, netEdgesLayer);
	        	
			tx.success();
	    } finally {
	    	tx.finish();
	    }
	        
        Search search = new SearchAll();	        	
	    tx = dataStore.beginTx();
	    try {
	    	layer.getIndex().executeSearch(search);
			tx.success();
	    } finally {
	    	tx.finish();
	    }
	        
	    List<SpatialDatabaseRecord> results = search.getResults();
	    monitor.beginTask("Creating Network...", results.size());
	    try {
			Iterator<SpatialDatabaseRecord> it = results.iterator();
			while (it.hasNext()) {
				tx = dataStore.beginTx();
				try {
					int worked = 0;
					for (int i = 0; i < 1000 && it.hasNext(); i++) {
						networkGenerator.add(it.next());
						worked++;
					}

					monitor.worked(worked);

					tx.success();
				} finally {
					tx.finish();
				}
			}
		} finally {
			monitor.done();
		}		
	}
}