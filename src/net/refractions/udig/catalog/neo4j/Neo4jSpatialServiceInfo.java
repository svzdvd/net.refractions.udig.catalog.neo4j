package net.refractions.udig.catalog.neo4j;

import net.refractions.udig.catalog.IServiceInfo;


/**
 * @author Davide Savazzi
 */
public class Neo4jSpatialServiceInfo extends IServiceInfo {

	// Constructor
	
	Neo4jSpatialServiceInfo(Neo4jSpatialService service) {
        this.title = service.getIdentifier().toString();
        this.description = "Neo4j Spatial Service (" + this.title + ")";
    }
	
}
