package edu.rice.rubis.invoker;

import java.sql.SQLException;

import org.ssase.actuator.Invoker;

import edu.rice.rubis.servlets.RubisHttpServlet;

public class ConnectionInvoker implements Invoker {

	public boolean invoke(String service, long value) {
		try {
			RubisHttpServlet.changeConnection((int)value);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.print("connection set to new value " + value + "\n");
		return true;
	}

}
