package org.ssascaling.actuator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.ssascaling.primitive.Type;
import org.ssascaling.util.Ssascaling;
import org.ssascaling.util.Tuple;

/**
 * Used by Dom0.
 * 
 * This usually only needed for software control primitives, as hardware control primitives
 * can be controlled directly in Dom0.
 * @author tao
 *
 */
public class ActuationSender {
	
	// VM ID - <IP, port>
	private Map<String, Tuple<String, Integer>> map = new HashMap<String, Tuple<String, Integer>>();
	
	static {
		instance = new ActuationSender();
	}
	
	private static ActuationSender instance;
	
	public static ActuationSender getInsatnce(){
		return instance;
	}
	
	private ActuationSender(){
		init();
	}
	
	public boolean send(String dest, String data) {
		return TCPsend(dest, data);
	}
	
    public boolean httpSend(String dest, String data) {
		
		System.out.print("To " + dest + " with " + "\n" + data);

		// Initialization section:
		// Try to open a socket on port 25
		// Try to open input and output streams
		try {
			URL url = new URL("http://"+map.get(dest).getVal1()+":"+map.get(dest).getVal2()
					+"/rubis_servlets/servlet/ConfigServlet?data="+
			URLEncoder.encode(data, "utf-8") ); 
			
		    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		    conn.setRequestMethod("GET");
		    conn.connect();
		 
		    conn.getResponseCode();
		   
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: hostname");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return true;
	}

	public boolean TCPsend(String dest, String data) {
		
		System.out.print("To " + dest + " with " + "\n" + data);

		Socket smtpSocket = null;
		DataOutputStream os = null;
		// Initialization section:
		// Try to open a socket on port 25
		// Try to open input and output streams
		try {
			smtpSocket = new Socket(map.get(dest).getVal1(), map.get(dest).getVal2());
			os = new DataOutputStream(smtpSocket.getOutputStream());
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: hostname");
			try {
				if (smtpSocket != null) {
					smtpSocket.close();
				}

				if (os != null) {
					os.close();
				}
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			System.err
					.println("Couldn't get I/O for the connection to: hostname");
			try {
				if (smtpSocket != null) {
					smtpSocket.close();
				}

				if (os != null) {
					os.close();
				}
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// If everything has been initialized then we want to write some data
		// to the socket we have opened a connection to on port 25
		if (smtpSocket != null && os != null) {
			try {
				// The capital string before each colon has a special meaning to
				// SMTP
				// you may want to read the SMTP specification, RFC1822/3
				os.writeBytes(data);
				os.close();
				smtpSocket.close();
			} catch (UnknownHostException e) {
				System.err.println("Trying to connect to unknown host: " + e);
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	private void init(){  
		 Properties configProp = new Properties();
			
			try {
				configProp.load(Ssascaling.class.getClassLoader().getResourceAsStream("dom0.properties"));
				
				String[] vms = configProp.getProperty("vm").split(",");
				String[] ports = configProp.getProperty("vm_ports").split(",");
				String[] ips = configProp.getProperty("vm_ips").split(",");
				
				for (int i = 0; i < vms.length;i++) {
					map.put(vms[i], new Tuple<String, Integer>(ips[i],Integer.parseInt(ports[i])));
				}
				
		    } catch (IOException e) {
				e.printStackTrace();
			}
	}
	
}
