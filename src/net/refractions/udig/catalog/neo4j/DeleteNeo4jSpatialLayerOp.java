package net.refractions.udig.catalog.neo4j;

import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;


/**
 * @author Davide Savazzi
 */
public class DeleteNeo4jSpatialLayerOp implements IOp {

	public void op(Display display, Object target, IProgressMonitor monitor) throws Exception {
		if (monitor == null) monitor = new NullProgressMonitor();
		
		// TODO ask user confirmation!
		
		Neo4jSpatialGeoResource geoResource = (Neo4jSpatialGeoResource) target;
		Neo4jSpatialDataStore dataStore = (Neo4jSpatialDataStore) geoResource.service().getDataStore(monitor);
		
		SpatialDatabaseService spatialDatabase = dataStore.getSpatialDatabaseService();
		spatialDatabase.deleteLayer(geoResource.getTypeName(), 
				new ProgressMonitorWrapper("Deleting Layer " + geoResource.getTypeName(), monitor));
	}

}
