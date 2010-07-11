package net.refractions.udig.catalog.neo4j;

import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;


/**
 * @author Davide Savazzi
 */
public class DeleteNeo4jSpatialLayerOp implements IOp {

	public void op(Display display, Object target, IProgressMonitor monitor) throws Exception {
		ConfirmDialogRunnable dialog = new ConfirmDialogRunnable(display);
		display.syncExec(dialog);
		
		if (dialog.returnValue == Window.OK) {
			Neo4jSpatialGeoResource geoResource = (Neo4jSpatialGeoResource) target;
			Neo4jSpatialDataStore dataStore = (Neo4jSpatialDataStore) geoResource.service().getDataStore(monitor);
			
			SpatialDatabaseService spatialDatabase = dataStore.getSpatialDatabaseService();
			spatialDatabase.deleteLayer(geoResource.getTypeName(), 
					new ProgressMonitorWrapper("Deleting Layer " + geoResource.getTypeName(), monitor));
		}
	}

	class ConfirmDialogRunnable implements Runnable {
		
		public ConfirmDialogRunnable(Display display) {
			this.display = display;
		}
		
		public void run() {
			Dialog dialog = new Dialog(display.getActiveShell()) {
				protected void createButtonsForButtonBar(Composite parent) {
					createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
					createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
				}					
					
				protected Control createDialogArea(Composite parent) {
					Control control = super.createDialogArea(parent);
					// TODO how to display longer message inside Dialog?
					control.getShell().setText("Confirm");
					return control;
				}
			};
				
			returnValue = dialog.open();
		}

		Display display;
		int returnValue;
	}
}