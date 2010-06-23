package net.refractions.udig.catalog.neo4j;

import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;


/**
 * @author Davide Savazzi
 */
public class RefreshNeo4jSpatialServiceOp implements IOp {

	public void op(Display display, Object target, IProgressMonitor monitor) throws Exception {
		Neo4jSpatialService service = (Neo4jSpatialService) target;
		service.refresh(monitor);
	}

}
