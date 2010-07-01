package net.refractions.udig.catalog.neo4j;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.neo4j.gis.spatial.Listener;


/**
 * @author Davide Savazzi
 */
public class ProgressMonitorWrapper implements Listener {

	// Constructor
	
	public ProgressMonitorWrapper(String taskName, IProgressMonitor monitor) {
		if (monitor == null) monitor = new NullProgressMonitor();
		
		this.taskName = taskName;
		this.monitor = monitor;
	}

	
	// Public methods
	
	public void begin(int unitsOfWork) {
		monitor.beginTask(taskName, unitsOfWork);
	}

	public void worked(int workedSinceLastNotification) {
		monitor.worked(workedSinceLastNotification);
	}

	public void done() {
		monitor.done();
	}

	public int suggestedCommitInterval() {
		return 1000;
	}
	
	
	// Attributes
	
	private String taskName;
	private IProgressMonitor monitor;

}
