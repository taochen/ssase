package org.ssascaling.actuator;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.ssascaling.primitive.Type;
import org.ssascaling.util.Ssascaling;

/**
 * Used by domU.
 * @author tao
 *
 */
public class ActuationReceiver {
	
	private int port;
	
	// Type name - invoker
	private Map<String, Invoker> map = new HashMap<String, Invoker>();
	
	public ActuationReceiver(Type[] types, Invoker[] invokers){
		for (int i = 0; i < types.length;i++) {
			map.put(types[i].toString(), invokers[i]);
		}
		init();
	}
	
	public void receive() {

		ServerSocket echoServer = null;

		// Try to open a server socket on port 9999
		// Note that we can't choose a port less than 1023 if we are not
		// privileged users (root)
		try {
			echoServer = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println(e);
		}
		// Create a socket object from the ServerSocket to listen and accept
		// connections.
		// Open input and output streams

		for (;;) {
			try {

				final Socket clientSocket = echoServer.accept();
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {

							DataInputStream is = new DataInputStream(
									clientSocket.getInputStream());
						
							doActuation(is);
						} catch (IOException e) {
							System.out.println(e);
						}
					}

				}).start();

			} catch (IOException e) {
				System.out.println(e);
			}

		}

	}
	
	private void doActuation (DataInputStream is){
		String line = null;
		try {
			
			// servicename-type-value
	
			while ((line = is.readLine()) != null) {				
				String[] split = line.split("-");
				// Invoke as it read the file.
				map.get(split[1]).invoke(split[0], Long.parseLong(split[2]));			
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void init(){  
		Properties configProp = new Properties();
		
		try {
			configProp.load(Ssascaling.class.getClassLoader().getResourceAsStream("domU.properties"));
			port = Integer.parseInt(configProp.getProperty("vm_port"));
			//Monitor.setSampleInterval(Integer.parseInt(configProp.getProperty("sampling_interval")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
