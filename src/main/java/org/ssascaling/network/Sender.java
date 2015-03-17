package org.ssascaling.network;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import org.ssascaling.sensor.SensoringController;
import org.ssascaling.util.Ssascaling;

public class Sender {
	
	
	private int port;
	// Dom0 IP
	private String address;
	
	public Sender(InputStream input){
		init(input);
	}

	public void send(String data) {
System.out.print(data + "\n");
		Socket smtpSocket = null;
		DataOutputStream os = null;
		// Initialization section:
		// Try to open a socket on port 25
		// Try to open input and output streams
		try {
			smtpSocket = new Socket(address, port);
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
			}
		}
	}
	
	
	private void init(InputStream input){  
		Properties configProp = new Properties();
		
		try {
			configProp.load(input);
			port = Integer.parseInt(configProp.getProperty("port"));
			address = configProp.getProperty("ip_address");
			SensoringController.setSampleInterval(Integer.parseInt(configProp.getProperty("sampling_interval")));
			//SensoringController.setSampleInterval(10000);
			SensoringController.setVMID(configProp.getProperty("vm_id"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
