package net.refractions.udig.catalog.neo4j.shpwizard;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.URLUtils;
import net.refractions.udig.catalog.neo4j.Activator;
import net.refractions.udig.catalog.neo4j.ProgressMonitorWrapper;
import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.neo4j.gis.spatial.ShapefileImporter;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStoreFactory;


/**
 * @author Davide Savazzi
 */
public class ShpImportWizard extends Wizard implements INewWizard {

	// Public methods
    
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle("SHP to Neo4j import");
        ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
        
        ImageDescriptor banner = imageRegistry.getDescriptor(WIZ_GIF);
        if (banner == null) {
        	URL bannerURL = Activator.getDefault().getBundle().getEntry(WIZ_GIF);        
        	banner = ImageDescriptor.createFromURL(bannerURL);
            imageRegistry.put(WIZ_GIF, banner);
        }
        setDefaultPageImageDescriptor(banner);
        
        setNeedsProgressMonitor(true);
        mainPage = new ShpImportWizardPage();
    }

    public void addPages() {
        super.addPages();
        addPage(mainPage);
    }

    public boolean canFinish() {
        return super.canFinish() && canFinish;
    }

    public boolean performFinish() {
        // run with backgroundable progress monitoring
        IRunnableWithProgress operation = new IRunnableWithProgress() {
            public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
    	        String shpPath = mainPage.getShpFile();
    	        
    	        // remove extension
    			shpPath = shpPath.substring(0, shpPath.lastIndexOf("."));
    	        
    	        String layerName = mainPage.getLayerName();
    	        if (layerName == null || layerName.trim().equals("")) {
    	        	layerName = shpPath.substring(shpPath.lastIndexOf(File.separator) + 1);
    	        }
           	
    	        String neo4jPath = mainPage.getNeo4jDir();
    	        if (!neo4jPath.endsWith(File.separator)) {
    	        	neo4jPath += File.separator;
    	        }
    	        neo4jPath += "neostore.id";
    	    
    	        try {
        	        Map<String,Serializable> params = new HashMap<String,Serializable>();
        	        params.put(Neo4jSpatialDataStoreFactory.URLP.key, URLUtils.fileToURL(new File(neo4jPath)));
    	        	
        	        Neo4jSpatialDataStore dataStore = Activator.getDefault().getDataStore(params);
            		ShapefileImporter importer = new ShapefileImporter(dataStore.getSpatialDatabaseService().getDatabase(), new ProgressMonitorWrapper("Importing...", monitor));
        	    	importer.importFile(shpPath, layerName);
				} catch (Throwable e) {
					e.printStackTrace();
	                String message = "An error occurred while reading the ShapeFile";
	                ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, Activator.ID, e);
				}
            }
        };
        PlatformGIS.runInProgressDialog("Importing a SHP file to a Neo4j Database", true, operation, true);
        return true;
    }
    
    protected void enableFinishButton() {
    	canFinish = true;
    }
    
    protected void disableFinishButton() {
    	canFinish = false;
    }
    
    
    // Attributes

    private boolean canFinish = false;
    private ShpImportWizardPage mainPage;
    private static final String WIZ_GIF = "icons/shpwizard/worldimage_wiz.gif";
}
