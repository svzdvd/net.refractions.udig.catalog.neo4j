package net.refractions.udig.catalog.neo4j;

import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.graphdb.Transaction;


/**
 * @author Davide Savazzi
 */
public class DeleteNeo4jSpatialLayerOp implements IOp {

	public void op(Display display, Object target, IProgressMonitor monitor) throws Exception {
		// TODO ask user confirmation!
		// TODO add monitor support		
		
		Neo4jSpatialGeoResource geoResource = (Neo4jSpatialGeoResource) target;
		Neo4jSpatialDataStore dataStore = (Neo4jSpatialDataStore) geoResource.service().getDataStore(monitor);
		
		Transaction tx = dataStore.beginTx();
		try {
			SpatialDatabaseService spatialDatabase = dataStore.getSpatialDatabaseService();
			spatialDatabase.deleteLayer(geoResource.getTypeName());
			
			tx.success();
		} finally {
			tx.finish();
		}		
	}

}
