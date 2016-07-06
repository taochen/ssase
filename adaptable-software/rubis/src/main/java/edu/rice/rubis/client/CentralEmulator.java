package edu.rice.rubis.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

public class CentralEmulator {

	public static final Object lock = new Object();
	
	
	
	
	static String prefix = "/Users/tao/research/monitor/"; 
	static String[] inputs = new String[]{
		"CPU Usage.rtf",
		"Memory Usage.rtf",
		"Workload.rtf",
		"Concurrency.rtf" 
	};
	
	static String[] outputs = new String[]{
		"Availability.rtf",
		"Response Time.rtf",
		"Throughput.rtf"
		
	};
	
	
	//static Map<String, Map<String,Double>> maxMap = new HashMap<String, Map<String,Double>>();

	static String[] services = new String[] {
			"edu.rice.rubis.servlets.SearchItemsByCategory",
			"edu.rice.rubis.servlets.AboutMe",
			"edu.rice.rubis.servlets.BrowseCategories",
			"edu.rice.rubis.servlets.BrowseRegions",
			"edu.rice.rubis.servlets.StoreBuyNow",
			"edu.rice.rubis.servlets.BuyNow",
			"edu.rice.rubis.servlets.BuyNowAuth",
			"edu.rice.rubis.servlets.PutBid",
			"edu.rice.rubis.servlets.PutBidAuth",
			"edu.rice.rubis.servlets.PutComment",
			"edu.rice.rubis.servlets.PutCommentAuth",
			"edu.rice.rubis.servlets.RegisterItem",
			"edu.rice.rubis.servlets.RegisterUser",
			"edu.rice.rubis.servlets.SearchItemsByRegion",
			"edu.rice.rubis.servlets.SellItemForm",
			"edu.rice.rubis.servlets.StoreBid",
			"edu.rice.rubis.servlets.StoreComment",
			"edu.rice.rubis.servlets.ViewBidHistory",
			"edu.rice.rubis.servlets.ViewItem",
			"edu.rice.rubis.servlets.ViewUserInfo"
	};
	
	private static int[] FIFA98 = new int[]{
		// June
	//		5586176,
	//		7188041,
	//		7894566,
	//		8297253,
	//		8093068,
	//		5452684,
		5734309,
		//day44 forward
		14527889,
		20068724,
		50395084,
		52406319,
		48956621,
		23528986,
		21093494,
		58013849,
		40732114,
		37911141,
		35898959,
		33695769,
		17390942,
		17224132,
		36698571,
		48340882,
		48667813,
		50539711,
		68819074,
		21414464,
		19117739,
		62228636,
		73291868,
		
		// July
		28356264,
	//		17499486,
	//		44502990,
	//		17885066,
	//		12821605
		
	};
	
	
	static boolean init = false;
	/**
	 * @param args
	 */
	//500,250,750,500,250,750,900,350,650,250,900,250,900,250,1000,500,250,600
	//,200,500,100
	//int[] clients = new int[]{500,250,1000,750,500,250,750,900,350,650,250,900,250,900,250,1000,500,250,600
	//,200,500,100};
//500,1000
//int[] clients = new int[]{500};
// int[] clients = new int[]{250,125, /**/500,/**/ 375,250,125,375,450,175,325,125,450,125,450,125,500,250,125,300,100,250,50};
//int[] clients = new int[]{325,163,650,488,325,163,488,585,228,423,163,585,163,585,163,650,325,163,390,130,325,65};
//int[] clients = new int[]{375,188,750,563,375,188,563,675,263,488,188,675,188,675,188,750,375,188,450,150,375,75};


//int[] clients = new int[]{100,50,200,150,100,50,150,180,70,130,50,180,50,180,50,200,100,50,120,40,100,20};
//int[] clients = new int[]{150,75,300,225,150,75,225,270,105,195,75,270,75,270,75,300,150,75,180,60,150,30};

// pre 150 intervals {100,100,100,100,100,100,100,100,100,100};
//int[] clients = new int[]{200,100,400,300,200,100,300,360,140,260,100,360,100,360,100,400,200,100,240,80,200,40};
// addtional {200,100,400,300,200,100,300,360,140,260,100,360,100,360,100,400,200,100,240,80,200,40}

//int[] clients = new int[]{300};
	public static void main(final String[] args) {
		int[] clients = null;
		if(init) {

			
			List<Integer> list = new ArrayList<Integer>();
			Random ran = new Random();
			for (int i = 0; i < 30; i++){
				list.add(ran.nextInt(500) + 100);
			}
			clients = new int[list.size()];
			for (int i = 0; i < list.size(); i++){
				clients[i] = list.get(i);
				System.out.print("("+i+","+clients[i]+")\n");
			}
			
		} else {
			  clients = new int[FIFA98.length];
				for (int i = 0; i < FIFA98.length;i++) {
					clients[i] = Math.round((int)(FIFA98[i]  / 120000));
					System.out.print("("+i+","+clients[i]+")\n");
				}
		}
		
	  
		
		
		activateSensors();
		
		//if(1==1) return;
		try {
			Thread.sleep(10000L);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//deactivateSensors();
		//if(1==1) return;
		for (int i = 0; i < clients.length;i++){
			RUBiSProperties.numberOfClient = (int) (clients[i]*1);
		    new Thread(new Runnable(){

				public void run() {
					ClientEmulator.main(new String[]{}, args.length >= 1? args[0] : null);					
				}
		    	
		    }).start();
		    synchronized (lock){
		    	try {
					lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		    System.setOut(System.out);
	        System.setErr(System.out);
		}
		
		deactivateSensors();
		Runtime.getRuntime().exit(0);
		
		
	}
	
	public static boolean activateSensors() {

		HttpURLConnection conn = null;
		// Initialization section:
		// Try to open a socket on port 25
		// Try to open input and output streams
		try {
			ResourceBundle configuration = ResourceBundle.getBundle("rubis");
			URL url = new URL("http://" + configuration.getString("httpd_hostname")+":"+
					configuration.getString("httpd_port")
					+ "/rubis_servlets/servlet/ConfigServlet?data=1");

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			System.out.print(configuration.getString("httpd_hostname") + " : " + conn.getResponseCode() + "\n");

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: hostname");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}

		return true;
	}
	
	public static boolean deactivateSensors() {

		HttpURLConnection conn = null;
		// Initialization section:
		// Try to open a socket on port 25
		// Try to open input and output streams
		try {
			ResourceBundle configuration = ResourceBundle.getBundle("rubis");
			URL url = new URL("http://"+configuration.getString("httpd_hostname")+":"+
					configuration.getString("httpd_port")+"/rubis_servlets/DGServlet");

			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();

			System.out.print(configuration.getString("httpd_hostname") + " : " + conn.getResponseCode() + "\n");

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: hostname");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}

		return true;
	}
	
	public static void terminate() {
	URL yahoo;
	try {
		
		ResourceBundle configuration = ResourceBundle.getBundle("rubis");
		 
		yahoo = new URL("http://"+configuration.getString("httpd_hostname")+":"+configuration.getString("httpd_port")+"/rubis_servlets/DGServlet");
	
    URLConnection yc = yahoo.openConnection();
    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                            yc.getInputStream()));
    String inputLine;

    while ((inputLine = in.readLine()) != null) 
        System.out.println(inputLine);
    in.close();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
	
	public static void main1(final String[] args) {
		BufferedReader reader = null;
		try{
		List<Double> values = null;
		for (String service : services) {

			//map.put(service, new HashMap<String,double[]>());
			//maxMap.put(service, new HashMap<String,Double>());
			double max = 0.0;
			for (String output : outputs) {
				max = 0.0;
				reader = new BufferedReader(new FileReader(prefix+service+"/"+output));
				values = new LinkedList<Double>();
				String line = null;
				while((line = reader.readLine()) != null) {
					values.add(Double.parseDouble(line));
					if (max < values.get(values.size()-1)) {
						max = values.get(values.size()-1);
					}
				}
				double[] newValues = new double[values.size()];
				for (int i = 0;i<newValues.length;i++) {
					newValues[i] = values.get(i)/max;
				}
				
				//map.get(service).put(output, newValues);
				reader.close();
			}
			for (String input : inputs) {
				max = 0.0;
				reader = new BufferedReader(new FileReader(prefix+service+"/"+input));
				values = new LinkedList<Double>();
				String line = null;
				while((line = reader.readLine()) != null) {
					if (input.equals("CPU Usage.rtf")) {
						values.add(Double.parseDouble(line)*4.67/100);
					} else if (input.equals("Memory Usage.rtf")) {
						
					    values.add(Double.parseDouble(line));
					} else if (input.equals("Response Time.rtf")){
						values.add(Double.parseDouble(line));
					} else if (input.equals("Throughput.rtf")){
						values.add(Double.parseDouble(line));
					} else {
						values.add(Double.parseDouble(line));
					}
					
					if (max < values.get(values.size()-1)) {
						max = values.get(values.size()-1);
					}
				}
				
				
				//map.get(service).put(input, newValues);
				reader.close();
			
			
			}
		}
		}catch(Exception e) {
			
		}
		
		
	}
}
