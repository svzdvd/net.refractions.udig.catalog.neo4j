package net.refractions.udig.catalog.neo4j.findpath;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.catalog.neo4j.Activator;
import net.refractions.udig.catalog.neo4j.Neo4jSpatialService;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ui.tool.AbstractActionTool;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.SpatialRelationshipTypes;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.impl.shortestpath.Dijkstra;
import org.neo4j.graphalgo.impl.util.DoubleAdder;
import org.neo4j.graphalgo.impl.util.DoubleComparator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;


/**
 * Tool to find the shortest path between selected waypoints, using Dijkstra algorithm.
 * 
 * @author Davide Savazzi
 */
public class FindPathTool extends AbstractActionTool implements FindPathConstants {

	// Public methods
	
	public void dispose() {
	}	
	
	public void run() {
		Display display = getContext().getViewportPane().getControl().getDisplay();
		final IBlackboard mapboard = getContext().getMap().getBlackboard();
		
        // get waypoints from blackboard
        if (!mapboard.contains(BLACKBOARD_WAYPOINTSLAYER) || !mapboard.contains(BLACKBOARD_WAYPOINTS)) {
			Activator.openError(display, "Error finding Path", "Please select at least two WayPoints");	
            return;
        }

        final SpatialDatabaseRecord[] waypoints = ((ArrayList<SpatialDatabaseRecord>) mapboard.get(BLACKBOARD_WAYPOINTS)).toArray(new SpatialDatabaseRecord[] {});
        if (waypoints.length < 2) {
			Activator.openError(display, "Error finding Path", "Please select at least two WayPoints");	
            return;
        }
        
		final ILayer pointsLayer = Activator.getDefault().findLayer(mapboard.getString(BLACKBOARD_WAYPOINTSLAYER), getContext().getMapLayers());
		if (pointsLayer == null) {
        	// this shouldn't happen
			Activator.openError(display, "Error finding Path", "Unable to retrieve Layer");			
            return;
		}
		
        // run with backgroundable progress monitoring
        IRunnableWithProgress operation = new IRunnableWithProgress() {
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            	Display display = getContext().getViewportPane().getControl().getDisplay();
            	
            	Neo4jSpatialService neo4jService = Activator.getDefault().getLayerService(pointsLayer, monitor);
				Neo4jSpatialDataStore dataStore = (Neo4jSpatialDataStore) neo4jService.getDataStore(monitor);

				SpatialDatabaseService spatialDatabaseService = dataStore.getSpatialDatabaseService();
				GraphDatabaseService databaseService = spatialDatabaseService.getDatabase();
				Transaction tx = databaseService.beginTx();
				try {
					List<SpatialDatabaseRecord> pathRecords = new ArrayList<SpatialDatabaseRecord>();
					
					mapboard.put(BLACKBOARD_PATH, pathRecords);
					
					// TODO don't delete waypoints automatically, create a tool for this purpose
					((ArrayList<SpatialDatabaseRecord>) mapboard.get(BLACKBOARD_WAYPOINTS)).clear();					
					
					for (int i = 1; i < waypoints.length; i++) {
				        SpatialDatabaseRecord one = waypoints[i - 1];
				        SpatialDatabaseRecord two = waypoints[i];
				        	
				        Node startNode = databaseService.getNodeById(one.getId());
				        Node endNode = databaseService.getNodeById(two.getId());
				        	
						// point <- edge -> point <- edge -> point
						Dijkstra<Double> sp = new Dijkstra<Double>(
							0.0, 
							startNode, 
							endNode, 
							new CostEvaluator<Double>() {
								public Double getCost(Relationship relationship, Direction direction) {
									Node startNode = relationship.getStartNode();											
									if (direction.equals(Direction.INCOMING)) {
										// TODO use a constant name
										// TODO if property doesn't exists, decode geometry and calculate it
										return (Double) startNode.getProperty("_network_length");
									} else {
										return 0.0;
									}
								}
							}, 
							new DoubleAdder(), 
							new DoubleComparator(), 
							Direction.BOTH, 
							SpatialRelationshipTypes.NETWORK);
						
						List<Node> pathNodes = sp.getPathAsNodes();
						if (pathNodes != null) {
							for (Node geomNode : pathNodes) {
								// TODO cache Layer data
								Layer layer = spatialDatabaseService.findLayerContainingGeometryNode(geomNode);
								pathRecords.add(new SpatialDatabaseRecord(layer.getName(), layer.getGeometryEncoder(), layer.getCoordinateReferenceSystem(), layer.getExtraPropertyNames(), geomNode));
							}
						}
					}
						
					tx.success();
				} catch (Exception e) {
					Activator.log(e.getMessage(), e);
					Activator.openError(display, "Error finding Path", "Unable to find Path");								
				} finally {
					tx.finish();
					
					// TODO duplicated code
					ILayer layer = Activator.getDefault().findLayer("Neo4j Network", getContext().getMapLayers());
					if (layer != null) layer.refresh(getContext().getViewportModel().getBounds());
				}
			}
        };
        PlatformGIS.runInProgressDialog("Finding shortest path...", true, operation, true);
	}
}