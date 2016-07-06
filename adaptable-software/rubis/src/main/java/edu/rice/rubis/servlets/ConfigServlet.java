package edu.rice.rubis.servlets;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ssase.actuator.ActuationReceiver;
import org.ssase.actuator.Invoker;
import org.ssase.primitive.Type;
import org.ssase.sensor.SensoringController;

import edu.rice.rubis.beans.PageCacheManager;

/**
 * This servlet just for activate the sensors. 
 * @author tao
 *
 */
public class ConfigServlet extends HttpServlet {

	 public void init (ServletConfig config) {
		// SensoringController.init(0, config.getServletContext());
		 // Need to initilize the change here, so that the filer can be setup properly by Ehcache.
		 PageCacheManager.getInstance().initFilter();
		 SensoringController.run();
		/* System.out.print("Geting ready !!!!!!!!! \n");
		   ar = new ActuationReceiver(new Type[]{Type.Thread},
					new Invoker[]{new ThreadInvoker()});*/
//		 for (String name : SensoringController.getServiceName()) {
//		     Config.changeThread(name, 5/*This need to change with the setup on Dom0*/);
//		 }
	 }
	 
	 public void destroy() {
		 SensoringController.destory();
	 }


	  public void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws IOException, ServletException
	  {
		 /* System.out.print("Geting data !!!!!!!!! \n");
		  String data = request.getParameter("data");
		  System.out.print(data+"\n");
		  ar.HTTPreceive(data);*/
	  }
	

}
