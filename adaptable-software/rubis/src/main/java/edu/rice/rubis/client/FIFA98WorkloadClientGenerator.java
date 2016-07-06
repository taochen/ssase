package edu.rice.rubis.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FIFA98WorkloadClientGenerator {

	final static String command1 = "/usr/bin/gzip -dc /Users/tao/research/projects/fifa98-workload/";
	final static String command2	= " | /Users/tao/research/projects/fifa98-workload/ita_public_tools/bin/checklog";
	final static String c = "/bin/sh /Users/tao/research/projects/fifa98-workload/shell/read_file ";
	// This is what as defined in the transition table, where a new request would be made based up on
	// the current service, there are 26 valid service in total.
	final static int average_reqeust_per_client = 26;
	private static int counter = 0;
	private static String previous_day = "";
	// Record the number of client
	static List<Integer> results = new ArrayList<Integer>();
	
	public static void main(String[] args) {
		File file = new File("/Users/tao/research/projects/fifa98-workload/");
		File[] files = file.listFiles();
		
		for (File f : files) {
			if(".DS_Store".equals(f.getName())) continue;
			if(!f.isDirectory()) {
				if(Integer.parseInt(f.getName().split("_")[1].split("day")[1]) < 67) continue;
				
				
				if(f.getName().equals("wc_day72_1.gz")) break;
				
				readFile(f.getName());
			}
		}
		
		System.out.print("The number of total request (per day) in FIFA 98 workload should be:\n");
		
		for (Integer in : results) {
			System.out.print(in + ",\n");
		}
		
//	System.out.print("The number of total client (divided by 'average_reqeust_per_client') in FIFA 98 workload should be:\n");
//		
//		for (Integer in : results) {
//			System.out.print(in/average_reqeust_per_client + ",\n");
//		}
	}
	
	private static void readFile(String name){
		counter++;
		System.out.print("File: "+ name.split("_")[1] + "\n");
		 BufferedReader br = null;
	        Process p = null;
			try {
				System.out.print("Command: "+ c + name + "\n");
				p = Runtime.getRuntime().exec(c + name);
				
				p.waitFor();
				
				br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				String line = null; 
				String request = null;
				
				while((line = br.readLine()) != null) {
					
					if (line.trim().startsWith("Total Requests:")) {					
						request = line.trim().split(" ")[line.trim().split(" ").length - 1];
						break;
					}
				}
				
				int n = 0;
				
				if(previous_day.equals(name.split("_")[1])){
					n = Integer.parseInt(request) + results.get(results.size() - 1);
					replace(results.size() - 1, n);
				} else {
					if(results.size() != 0) {
						
					// Switch on if we need average of a day
					//n = Math.round(results.get(results.size() - 1)/(counter - 1));
					//replace(results.size() - 1, n);
					
				
					n = Integer.parseInt(request);
					results.add(n);
					counter = 1;
					} else {
						n = Integer.parseInt(request);
						results.add(n);
					}
				}
				
				previous_day = name.split("_")[1];
				
				
				//results.add(Integer.parseInt(maxClient) - Integer.parseInt(minClient));
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					p.destroy();
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
			
	}
	
	public static void main1(String[] args) {
		int[] array = new int[56];
		String prefix = "wc_day";
		String postfix = ".gz";
		for(int i = 37; i < 93; i++) {
			array[i-37] = i;
		}
		
		
		for(int i : array) {
			
			//if(i < 42) continue;
			
			for(int j = 1; j < 12/*max is 11*/; j++) {
				
				
//				try {
//					Thread.sleep(30000L);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				try {
					download(prefix+i+"_"+j+postfix);
					//download("wc_day39_5.gz");
				} catch (FileNotFoundException e) {
					System.err.print(prefix+i+"_"+j+postfix + " not found, so skip it\n");
					break;
				}
				
			
			}
			
		}
		
//		try {
//			download("wc_day39_2.gz");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
	}
	
	public static void download(String name) throws FileNotFoundException{
		
		int BUFFER_SIZE = 4096;
		
		long startTime = System.currentTimeMillis();
		String ftpUrl = "http://ita.ee.lbl.gov/traces/WorldCup/"+name;
		
		String savePath = "/Users/tao/research/projects/fifa98-workload/";
		
		File f = new File(savePath+name);
		if(f.exists()) {
			System.out.print(name + " already exist\n");
			return;
		}
		FileOutputStream outputStream = null;
		InputStream inputStream = null;
		//ftpUrl = String.format(ftpUrl, user, pass, host);
		System.out.println("Connecting to FTP server to download " + name + "\n");
		try {
			URL url = new URL(ftpUrl);
			URLConnection conn = url.openConnection();
			inputStream = conn.getInputStream();
		
			
			long filesize = conn.getContentLength();
			System.out.println("Size of the file to download in kb is: "
					+ filesize / 1024);
			outputStream = new FileOutputStream(savePath+name);
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			long endTime = System.currentTimeMillis();
			System.out.println("File downloaded");
			System.out.println("Download time in sec. is:-"
					+ (endTime - startTime) / 1000);
			
		} catch (FileNotFoundException ex) {
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(outputStream != null) outputStream.close();
				if(inputStream != null) inputStream.close();
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}
	
	private static void replace(int index, int value){
		results.remove(index);
		results.add(index, value);
	}
	
}
