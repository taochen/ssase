package edu.rice.rubis.invoker;

import org.ssase.actuator.Invoker;
import org.apache.catalina.ServerFactory;
import org.apache.coyote.http11.Http11Protocol;

public class CompressionInvoker implements Invoker {

	/**
	 * 0 means switch off, otherwise on
	 */
	public boolean invoke(String service, long value) {
		String v = value == 0 ? "off" : "on";

		Http11Protocol hp = ((Http11Protocol) ServerFactory.getServer()
				.findService("Catalina").findConnectors()[0]
				.getProtocolHandler());

		synchronized (hp) {
			hp.setCompression(v);
		}
		System.out.print("compression set to new value " + value + "\n");
		return true;
	}

}
