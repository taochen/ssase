package edu.rice.rubis.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ssase.sensor.SensoringController;

public class DGServlet extends HttpServlet {

	
	  public void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws IOException, ServletException
	  {
		
		  try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  SensoringController.destory();
		  /*String service = request.getParameter("service");
		  if (service == null || "".equals(service)) {
			  SensoringController.writeMonitorResult();
		  } else {
		      SensoringController.writeMonitorResult(service);
		  }*/
	  }
	  

	  public void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws IOException, ServletException
	  {
	    doGet(request, response);
	  }
}
