package edu.rice.rubis.servlets;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

import edu.rice.rubis.beans.PageCacheManager;

public class EngineListener implements LifecycleListener{

	public void lifecycleEvent(LifecycleEvent event) {
		System.out.print("Event  " + event + "\n");
		// Try to ensure that this is run before the filter are created.
	
	}

}
