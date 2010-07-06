package net.refractions.udig.catalog.neo4j.findpath;

import java.awt.Color;
import java.awt.Shape;
import java.util.Collections;
import java.util.List;

import net.refractions.udig.mapgraphic.MapGraphic;
import net.refractions.udig.mapgraphic.MapGraphicContext;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.ui.graphics.ViewportGraphics;

import org.neo4j.gis.spatial.SpatialDatabaseRecord;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


public class GraphMapGraphic implements MapGraphic, Constants {

	public void draw(MapGraphicContext context) {
		IBlackboard mapboard = context.getMap().getBlackboard();
        
		List<SpatialDatabaseRecord> waypoints = (List<SpatialDatabaseRecord>) mapboard.get(BLACKBOARD_WAYPOINTS);
        if (waypoints == null) waypoints = Collections.EMPTY_LIST;
        
        List<SpatialDatabaseRecord> pathElements = (List<SpatialDatabaseRecord>) mapboard.get(BLACKBOARD_PATH);
        if (pathElements == null) pathElements = Collections.EMPTY_LIST;
        
        ViewportGraphics graphics = context.getGraphics();

        for (SpatialDatabaseRecord waypoint : waypoints) {
            if (waypoint.getGeometry() instanceof Point) {
                Point point = (Point) waypoint.getGeometry();
                java.awt.Point pixel = context.worldToPixel(point.getCoordinate());
                
                System.out.println("displaying waipont " + point + " " + pixel);
                
            	graphics.setColor(new Color(221, 41, 69));
                graphics.fillRect(pixel.x - 5, pixel.y - 5, 10, 10);
                
                graphics.setColor(Color.BLACK);
                // graphics.setStroke(ViewportGraphics.LINE_SOLID, 2);
                graphics.drawRect(pixel.x - 5, pixel.y - 5, 10, 10);                
            } else {
            	// TODO
            	System.out.println("NOT A POINT");
            }
        }

        graphics.setStroke(ViewportGraphics.LINE_SOLID, 3);
    	graphics.setColor(new Color(221, 41, 69));
        for (SpatialDatabaseRecord pathElement : pathElements) {
        	Geometry geometry = pathElement.getGeometry();
        	Shape shape = context.toShape(geometry, pathElement.getCoordinateReferenceSystem());
        	graphics.draw(shape);
        }
	}
}