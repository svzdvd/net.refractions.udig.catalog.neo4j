/**
 * 
 */
package net.refractions.udig.catalog.neo4j;

import java.util.Iterator;
import java.util.List;

import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.neo4j.gis.spatial.Constants;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.LineStringNetworkGenerator;
import org.neo4j.gis.spatial.Search;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.gis.spatial.query.SearchAll;
import org.neo4j.graphdb.Transaction;


/**
 * @author Davide Savazzi
 */
public class GenerateLineStringNetworkOp implements IOp {

	public void op(Display display, Object target, IProgressMonitor monitor) throws Exception {
		// monitor progress
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
	    
		Neo4jSpatialGeoResource geoResource = (Neo4jSpatialGeoResource) target;
		Neo4jSpatialDataStore dataStore = (Neo4jSpatialDataStore) geoResource.service().getDataStore(monitor);
		SpatialDatabaseService spatialDatabase = dataStore.getSpatialDatabaseService();
		Layer layer = spatialDatabase.getLayer(geoResource.getTypeName());

		if (layer.getGeometryType() == Constants.GTYPE_LINESTRING ||
			layer.getGeometryType() == Constants.GTYPE_MULTILINESTRING) {
			
			LineStringNetworkGenerator networkGenerator;
			
	        Transaction tx = dataStore.beginTx();
	        try {
	        	Layer netPointsLayer = spatialDatabase.getLayer(layer.getName() + " - network points", true);
	        	netPointsLayer.setCoordinateReferenceSystem(layer.getCoordinateReferenceSystem());
	        	
	        	Layer netEdgesLayer = spatialDatabase.getLayer(layer.getName() + " - network edges", true);
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
		} else {
			// TODO show error message
			System.out.println("Type invalid: " + layer.getGeometryType());
		}
	}
}