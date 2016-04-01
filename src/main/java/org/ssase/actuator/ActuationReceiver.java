package org.ssase.actuator;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.ssase.primitive.Type;
import org.ssase.util.Ssascaling;

/**
 * Used by domU.
 * @author tao
 *
 */
public class ActuationReceiver {
	
	private int port;
	ServerSocket echoServer = null;
	// Type name - invoker
	private Map<String, Invoker> map = new HashMap<String, Invoker>();
	
	public ActuationReceiver(Type[] types, Invoker[] invokers){
		for (int i = 0; i < types.length;i++) {
			map.put(types[i].toString(), invokers[i]);
		}
		init();
	}
	
	public void shutdown(){
		try {
			echoServer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void receive() {
		 TCPreceive();
	}
	
	public void HTTPreceive(String data) {
		String[] temp = data.split("\n");
		
		for (String line : temp) {				
			String[] split = line.split("-");
			// Invoke as it read the file.
			map.get(split[1]).invoke(split[0], Long.parseLong(split[2]));			
		}
	}
	
	public void TCPreceive() {


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

		
		System.out.println("Receiver us runing!!!\n");
		for (;;) {
			try {

				final Socket clientSocket = echoServer.accept();
				new Thread(new Runnable() {

					@Override
					public void run() {
						DataInputStream is = null;
						try {

							is = new DataInputStream(
									clientSocket.getInputStream());
						
							doActuation(is);
						} catch (IOException e) {
							System.out.println(e);
						} finally {
							try {
								clientSocket.close();
								if(is != null) is.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}

				}).start();

			} catch (IOException e) {
				System.out.println(e);
			} 

		}

	}
	
	public void invoke(String service, String type, long value){
		map.get(type).invoke(service, value);
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
