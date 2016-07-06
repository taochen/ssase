package edu.rice.rubis.servlets;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.apache.catalina.ServerFactory;
import org.apache.coyote.http11.Http11Protocol;
import org.ssase.sensor.SensoringController;
import org.ssase.sensor.control.CompressionSensor;

import edu.rice.rubis.beans.StandardThreadExecutorWrapper;

public class StimulusListener implements ServletRequestListener{
	

	public void requestInitialized(ServletRequestEvent arg0) {
		SensoringController.recordPriorToTask(null, "maxThread");
		SensoringController.recordPriorToTask(null, "minSpareThreads");

	}

	public void requestDestroyed(ServletRequestEvent event) {
		Long value = StandardThreadExecutorWrapper.postPerf.get(Thread.currentThread());
		if(value != null){
			//System.out.print("Request end at " + System.currentTimeMillis() + ", total RT is: " + (System.currentTimeMillis() - value) +  "\n");
			SensoringController.recordPostToTask(null, value, "Response Time");
			String url =((javax.servlet.http.HttpServletRequest)event.getServletRequest()).getRequestURI();
			url = url.substring(url.lastIndexOf('/')+1);
			SensoringController.recordPostToTask(null, 0, "Workload-"+url);
			StandardThreadExecutorWrapper.postPerf.remove(Thread.currentThread());
		}
		
		//System.out.print("*******************url " + url + "\n");
		SensoringController.recordPostToTask(null, 0, "maxThread");
		SensoringController.recordPostToTask(null, 0, "minSpareThreads");
		
		SensoringController.recordPostToTask(null, 0, "maxBytesLocalDisk");
		SensoringController.recordPostToTask(null, 0, "maxBytesLocalHeap");
		
	}


}
