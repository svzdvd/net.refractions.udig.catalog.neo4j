package net.refractions.udig.catalog.neo4j.findpath;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.catalog.neo4j.Activator;
import net.refractions.udig.mapgraphic.MapGraphic;
import net.refractions.udig.mapgraphic.MapGraphicContext;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.ui.graphics.ViewportGraphics;

import org.neo4j.gis.spatial.SpatialDatabaseRecord;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


/**
 * MapGraphic that draws selected waypoints and the shortest path found between them.
 * 
 * TODO style support
 */
public class GraphMapGraphic implements MapGraphic, FindPathConstants {

	public void draw(MapGraphicContext context) {
		IBlackboard mapboard = context.getMap().getBlackboard();
        
		List<SpatialDatabaseRecord> waypoints = (List<SpatialDatabaseRecord>) mapboard.get(BLACKBOARD_WAYPOINTS);
        if (waypoints == null) waypoints = Collections.EMPTY_LIST;
        
        List<SpatialDatabaseRecord> pathElements = (List<SpatialDatabaseRecord>) mapboard.get(BLACKBOARD_PATH);
        if (pathElements == null) pathElements = Collections.EMPTY_LIST;
        
        ViewportGraphics graphics = context.getGraphics();

        graphics.setStroke(ViewportGraphics.LINE_SOLID, 3);
    	graphics.setColor(new Color(221, 41, 69));
        for (SpatialDatabaseRecord pathElement : pathElements) {
        	Geometry geometry = pathElement.getGeometry();
        	if (geometry.getGeometryType().equalsIgnoreCase("MultiLineString")) {
        		for (int i = 0; i < geometry.getNumGeometries(); i++) {
        			drawLineString(context, graphics, geometry.getGeometryN(i), pathElement.getCoordinateReferenceSystem());
	        	}
	        } else if (geometry.getGeometryType().equalsIgnoreCase("LineString")) {
    			drawLineString(context, graphics, geometry, pathElement.getCoordinateReferenceSystem());
	        }
        }

        graphics.setStroke(ViewportGraphics.LINE_SOLID, 1);
        for (SpatialDatabaseRecord waypoint : waypoints) {
            if (waypoint.getGeometry() instanceof Point) {
                Point point = (Point) waypoint.getGeometry();
                java.awt.Point pixel = context.worldToPixel(point.getCoordinate());
 
            	graphics.setColor(new Color(221, 41, 69));
                graphics.fillRect(pixel.x - 5, pixel.y - 5, 10, 10);
                graphics.setColor(Color.BLACK);
                graphics.drawRect(pixel.x - 5, pixel.y - 5, 10, 10);                
            } else {
            	Activator.log("This shouldn't happen: invalid waypoint " + waypoint);
            }
        }
	}
	
	private void drawLineString(MapGraphicContext context, ViewportGraphics graphics, Geometry geometry, CoordinateReferenceSystem crs) {
		Coordinate[] coordinates = geometry.getCoordinates();
		for (int i = 1; i < coordinates.length; i++) {
            java.awt.Point a = context.worldToPixel(coordinates[i - 1]);
        	java.awt.Point b = context.worldToPixel(coordinates[i]);		
			graphics.drawLine(a.x, a.y, b.x, b.y);
		}
	}
}