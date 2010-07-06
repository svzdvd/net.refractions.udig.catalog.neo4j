package net.refractions.udig.catalog.neo4j.findpath;

import java.util.ArrayList;

import net.refractions.udig.catalog.neo4j.Activator;
import net.refractions.udig.catalog.neo4j.Neo4jSpatialService;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ui.render.displayAdapter.MapMouseEvent;
import net.refractions.udig.project.ui.tool.SimpleTool;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.gis.spatial.query.SearchClosest;
import org.neo4j.graphdb.Transaction;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;


/**
 * @author Davide Savazzi
 */
public class WayPointTool extends SimpleTool implements Constants {

	// Constructor
	
	public WayPointTool() {
		super(MOUSE);
	}
	
	
	// Public methods
	
	public void onMousePressed(MapMouseEvent evt) {
		IProgressMonitor monitor = new NullProgressMonitor();
		Neo4jSpatialService neo4jService = Activator.getDefault().getLayerService(getContext().getSelectedLayer(), monitor);
		if (neo4jService == null) {
			// TODO show error
			return;
		}

		Neo4jSpatialDataStore dataStore = (Neo4jSpatialDataStore) neo4jService.getDataStore(monitor);
				
		IBlackboard mapboard = context.getMap().getBlackboard();
				
		String waypointsLayer = mapboard.getString(BLACKBOARD_WAYPOINTSLAYER);
	    if (!mapboard.contains(BLACKBOARD_WAYPOINTS)) {
	    	mapboard.put(BLACKBOARD_WAYPOINTS, new ArrayList<SpatialDatabaseRecord>());
	    }
	    final ArrayList<SpatialDatabaseRecord> waypoints = (ArrayList<SpatialDatabaseRecord>) mapboard.get(BLACKBOARD_WAYPOINTS);
				
		SpatialDatabaseService spatialDatabase = dataStore.getSpatialDatabaseService();
		Transaction tx = spatialDatabase.getDatabase().beginTx();
		try {
			Layer layer = spatialDatabase.getLayer(getContext().getSelectedLayer().getName());

			System.out.println("geometrytype: " + layer.getGeometryType());
			System.out.println("guess geometrytype: " + layer.guessGeometryType());
			// TODO check type is org.neo4j.gis.spatial.Constants.GTYPE_POINT

			if (!layer.getName().equals(waypointsLayer)) {
				waypoints.clear();
				mapboard.put(BLACKBOARD_WAYPOINTSLAYER, layer.getName());
			}
					
			// TODO add 10% buffer to searchWindow?
			Envelope bbox = getContext().getViewportModel().getBounds();
					
			System.out.println("using bbox: " + bbox);
					
			Coordinate clickPt = getContext().pixelToWorld(evt.x, evt.y);
			SearchClosest search = new SearchClosest(layer.getGeometryFactory().createPoint(clickPt), bbox);

			long start = System.currentTimeMillis();
			layer.getIndex().executeSearch(search);
			long stop = System.currentTimeMillis();
			System.out.println("search executed in " + (stop - start) + "ms");
		            
			System.out.println("items found: " + search.getResults().size());
					
		    // TODO add only the first if it exists
		    final SpatialDatabaseRecord foundWaypoint = search.getResults().size() > 0 ? search.getResults().get(0) : null;
		    if (foundWaypoint != null) {
		    	waypoints.add(foundWaypoint);
			} else {
				// TODO
				System.out.println("this shouldn't happen: no waypoint found");
			}
					
			tx.success();
		} finally {
			tx.finish();
		}

		// TODO duplicated code
		ILayer layer = Activator.getDefault().findLayer("Neo4j Network", getContext().getMapLayers());
		if (layer != null) layer.refresh(getContext().getViewportModel().getBounds());
								
	    final IStatusLineManager statusBar = getContext().getActionBars().getStatusLineManager();
	    if (statusBar == null) {
	    	return; // shouldn't happen if the tool is being used.
	    }
	            
	    getContext().updateUI(new Runnable() {
	    	public void run() {
	    		statusBar.setErrorMessage(null);
	            statusBar.setMessage(waypoints.size() + " waypoints");
	        }
	    });
	}
}
