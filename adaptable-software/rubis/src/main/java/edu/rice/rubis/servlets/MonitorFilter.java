package edu.rice.rubis.servlets;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.ssase.actuator.ActuationReceiver;
import org.ssase.actuator.Invoker;
import org.ssase.primitive.Type;
import org.ssase.sensor.*;
import org.ssase.util.SSAScalingThreadPool;

public class MonitorFilter implements Filter {
	
	public void destroy() {
		
	}

	public void doFilter(ServletRequest req, ServletResponse rep,
			FilterChain chain) throws IOException, ServletException {
        String url =((javax.servlet.http.HttpServletRequest)req).getRequestURI();
        url = url.substring(url.lastIndexOf('/')+1);
         System.out.print("start " +Thread.currentThread() +"**********"+url+"***********\n"); 
//        try {
//			Thread.sleep((long)10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//        url = url.substring(url.lastIndexOf('/')+1);
//        double rt = SensoringController.recordPriorToTask(url, "Response Time");
//        double av = SensoringController.recordPriorToTask(url, "Availability");
//        double re = SensoringController.recordPriorToTask(url, "Reliability");
//        SensoringController.recordPriorToTask(url, "Workload");
//       // SensoringController.recordPriorToTask(url, ServabilitySensor.index);
//        //System.out.print(url + "\n");
//        Config.getThread(url);
//        
//        // The only post action before propagation of the service execution
//        //SensoringController.recordPostToTask(url, 0, ServabilitySensor.index);
//     
//        
//        SensoringController.recordPriorToTask(url, "Concurrency");
//        /*try {
//			MonitoringController.execute(url, chain, FilterChain.class.getDeclaredMethod("doFilter", 
//				new Class[]{ServletRequest.class,ServletResponse.class}), new Object[]{req,rep});	
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} */
//        // ********** Propagate the service execution chain ***********
        chain.doFilter(req, rep);
//        // ********** Propagate the service execution chain ***********
//        
//        SensoringController.recordPostToTask(url, rt, "Response Time");
//        SensoringController.recordPostToTask(url, av, "Availability");
//        SensoringController.recordPostToTask(url, re, "Reliability");
//        SensoringController.recordPostToTask(url, 0, "Throughput");
//        SensoringController.recordPostToTask(url, 0, "Workload");
//        SensoringController.recordPostToTask(url, 0, "Concurrency");
//		Config.releaseThread(url);
       // System.out.print("end " +Thread.currentThread() +"**********"+url+"***********\n");
	}

	public void init(FilterConfig arg0) throws ServletException {
			System.out.print("Filer intilized **************\n");	
	}

}
