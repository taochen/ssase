package org.ssase.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

public class WSDreamPreProcessor {

	private static String prefix = "/Users/tao/research/projects/ssase-core/ssase/experiments-data/on-off/wsdream/";
	private static double threshold = 2.1;

	private static String[] users = new String[]{"3","6","70","141","50","40","68","88","108","105","122","18","89","95","19","120","97","80","55","104"};//20
	private static String[] services = new String[]{"147","1018","383","1981","34","1787","1854","1095","1040","969"};//10

	
//	private static String[] users = new String[]{"20","73","75","55","103","89","69","99","64","43","6","117","85","33","91","118","45","65","101","18"};//20
//	private static String[] services = new String[]{"651","1736","769","1829","1697","1830","2183","867","1077","1997"};//10

	
//	private static String[] users = new String[]{"10","122","93","36","50","72","108","101","124","5","35","47","6","74","97","106","40","116","120","1"};//20
//	private static String[] services = new String[]{"1723","1791","1487","392","896","2205","258","214","2464","1633"};//10

	
//	private static String[] users = new String[]{"44","89","15","30","52","42","116","91","59","132","39","31","138","14","22","139","41","9","32","58"};//20
//	private static String[] services = new String[]{"131","1772","296","2054","1224","765","2195","394","1089","2009"};//10

	
	
	
//	private static String[] users = new String[]{"126","1","29","66","73","47","106","129","88","44","54","36","5","124","8","120","49","104","75","77"};//20
//	private static String[] services = new String[]{"1514","489","1787","1151","1262","36","1059","998","1209","658"};//10

	
	
//	private static String[] users = new String[]{"140","68","139","23","85","49","74","99","91","80","1","12","48","111","95","107","35","33","18","113"};//20
//	private static String[] services = new String[]{"264","888","845","890","899","1049","1553","1911","2448","576"};//10

	
//	private static String[] users = new String[]{"67","87","7","113","86","69","24","37","112","89","22","96","55","121","129","98","32","135","27","34"};//20
//	private static String[] services = new String[]{"2109","261","1223","1267","172","306","2297","820","2199","300"};//10

	
//	private static String[] users = new String[]{"136","64","99","68","48","109","73","92","137","110","130","139","25","106","112","90","70","129","100","20"};//20
//	private static String[] services = new String[]{"2256","187","1592","1062","1001","370","1094","1321","996","1790"};//10

	
	
//	private static String[] users = new String[]{"58","27","33","8","66","35","38","5","65","41","101","132","62","30","43","93","82","51","77","135"};//20
//	private static String[] services = new String[]{"2376","1008","1556","2060","776","919","1838","717","1218","1086"};//10

	
	
//	private static String[] users = new String[]{"87","63","46","20","93","112","52","61","116","68","88","58","35","38","77","136","139","98","125","2"};//20
//	private static String[] services = new String[]{"1318","1245","539","1091","2275","129","1720","2007","706","1511"};//10

	public  static void main(String[] args) {
		//parse("rtdata");
		//parse("tpdata");
		
		
		
		generateData("rtdata");
		generateData("tpdata");
		
		
		//preprocess();
	
	}
	
	private static void preprocess(){
		Set<Integer> set = new HashSet<Integer>();
		String o ="";
		for (int i = 0; i < 20; i++){
			
			
			
			int n = new java.util.Random().nextInt(142);
			while(set.contains(n)) {
				n = new java.util.Random().nextInt(142);
			}
			
			o += "\"" + n + "\",";
			set.add(n);
			//o = o.substring(0, o.length()-1);
		
		}
		System.out.print(o + "\n");
		set.clear();
		o ="";
		for (int i = 0; i < 10; i++){
			
			
			int n = new java.util.Random().nextInt(2499);
			while(set.contains(n)) {
				n = new java.util.Random().nextInt(2499);
			}
			
			o += "\"" + n + "\",";
			set.add(n);
			
		}
		System.out.print(o);
	}
	
	private static void generateData(String path){
		
		// from 1 to 10
		int[][] service_values = new int[][]{
				
				{1,10,6,7,8,5,10,3,9,8,2,5,1,8,9,1,7,5,5,5,4,2,6,8,9,2,4,3,4,10,10,2,6,5,2,3,2,9,7,8,1,3,7,9,7,4,3,2,2,6,7,1,4,2,9,4,2,1,4,8,9,5,7,10},
				{3,5,1,9,3,5,9,1,6,10,2,9,8,3,5,1,5,9,10,8,7,9,8,5,5,6,2,10,6,7,10,2,3,2,2,9,2,9,10,7,8,2,10,3,8,7,1,9,6,9,5,3,3,9,10,7,3,2,9,8,3,5,1,10},
				{6,10,7,4,1,10,4,2,5,4,1,8,9,3,10,6,3,9,2,3,4,8,9,8,7,2,8,4,10,8,5,5,8,4,10,1,10,8,3,7,4,3,6,8,10,6,6,5,6,10,4,9,8,5,6,4,2,7,7,5,3,4,8,9},
				{5,8,2,8,8,3,7,9,6,3,6,3,3,9,10,1,1,8,3,5,4,10,7,9,2,7,6,3,1,3,2,9,8,5,6,3,1,7,8,2,3,4,3,6,10,8,7,9,6,9,10,1,8,4,2,4,2,6,6,8,2,5,10,3},
				{9,1,4,4,1,5,9,8,6,3,10,2,2,7,7,10,6,5,1,2,2,9,3,6,5,1,2,5,8,5,6,1,2,9,1,1,7,5,2,9,9,6,7,8,1,3,2,2,1,6,6,1,7,10,1,3,2,7,3,7,3,8,8,9},
				{10,9,3,9,3,2,3,2,6,1,9,4,6,9,6,5,10,1,8,3,3,3,9,8,2,10,4,6,4,10,7,2,1,8,2,10,4,5,4,10,4,2,6,5,9,3,1,2,3,9,6,5,5,10,8,1,8,4,1,2,5,6,6,6},
				{2,4,8,3,8,4,9,5,5,6,7,9,10,6,1,9,4,6,2,4,9,8,9,4,8,10,5,4,8,5,6,5,3,9,4,2,7,4,1,2,8,5,3,2,3,3,10,5,7,3,7,6,2,4,8,8,9,2,6,2,9,3,5,1},
				{4,8,1,7,6,1,5,7,4,6,5,1,9,4,8,4,3,6,10,9,6,6,8,6,10,2,10,6,6,6,10,8,2,2,1,10,4,7,10,7,6,8,2,5,3,2,4,6,6,7,10,8,7,7,7,1,10,4,7,6,9,1,6,5},
				{3,2,5,5,9,1,10,4,6,9,10,10,4,7,5,2,1,8,5,1,5,4,10,2,4,8,5,3,10,5,5,8,8,1,2,4,4,7,7,4,7,4,10,7,3,5,6,8,2,7,6,2,6,8,7,5,9,4,3,1,4,10,6,3},
				{7,8,5,7,8,5,9,9,10,9,8,4,2,10,8,2,8,2,5,4,2,7,1,8,5,2,10,6,6,3,2,1,2,3,1,8,6,10,4,5,10,2,5,5,6,9,9,4,8,7,1,1,9,7,6,2,1,7,2,8,2,6,9,8}

				
		};
		int[][] users_values = new int[][]{
			
				{0,1,0,0,1,1,0,1,0,0,1,1,1,0,1,0,1,0,0,0,0,1,1,0,0,0,1,1,1,1,0,1,0,0,1,0,1,1,0,1,0,0,0,0,1,0,1,0,1,0,1,0,1,1,0,0,1,1,1,1,1,0,1,0},
				{1,0,1,1,1,0,1,0,0,1,0,0,1,1,1,0,1,0,0,1,0,0,1,1,1,1,1,0,0,0,1,1,0,1,0,1,0,0,0,1,0,0,1,1,1,1,0,1,1,1,0,1,0,1,1,0,1,1,1,0,0,0,1,1},
				{0,0,0,1,1,0,0,0,1,1,1,0,1,1,0,0,1,1,1,1,1,0,0,1,1,0,0,0,0,1,1,0,0,0,0,1,0,0,0,1,1,0,0,0,0,1,0,0,0,1,0,1,0,1,1,0,1,1,0,0,1,0,1,1},
				{1,0,0,1,1,1,0,1,1,0,1,0,0,0,0,1,1,0,0,0,1,0,0,1,0,0,0,0,1,0,1,1,1,1,0,1,0,1,0,0,1,1,1,1,0,0,1,1,1,0,1,1,1,1,1,0,0,0,1,1,0,0,0,0},
				{0,1,1,1,0,0,1,1,0,0,0,0,1,1,1,0,0,1,1,0,1,0,1,1,1,0,0,0,0,0,1,0,1,1,1,0,1,1,0,0,0,1,1,1,1,0,1,1,1,1,0,0,0,0,1,0,0,0,1,0,0,1,0,1},
				{1,0,1,1,1,0,0,0,0,1,1,1,0,1,1,1,1,1,1,1,0,1,1,1,0,1,1,1,0,0,1,0,1,1,0,0,1,1,0,0,0,1,1,1,1,1,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1,1,0,0},
				{0,1,1,1,1,0,1,1,0,1,0,0,0,1,0,1,1,1,0,0,0,1,1,1,1,0,0,1,1,0,0,0,1,1,0,0,0,0,1,0,0,0,1,1,0,0,1,0,1,1,0,0,0,1,0,1,0,0,1,1,0,0,0,1},
				{0,1,1,1,0,1,0,0,0,0,0,1,0,1,1,1,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,1,1,0,0,0,1,1,1,0,0,0,1,1,0,0,1,1,0,1,0,0},
				{0,0,1,1,1,0,0,1,1,0,0,0,1,1,0,0,0,1,1,0,0,0,0,1,1,0,1,1,1,1,0,1,1,1,1,0,1,1,0,0,1,1,1,1,1,0,1,1,1,0,1,1,1,0,0,1,0,1,0,0,1,1,0,0},
				{0,1,0,0,0,0,1,1,1,0,1,1,1,0,1,0,1,1,0,0,1,1,0,1,0,1,1,1,1,1,0,1,0,0,0,0,1,1,0,1,0,1,1,0,0,0,0,1,1,0,1,1,1,0,1,1,1,0,0,0,1,0,0,1},
				{1,1,1,1,1,0,1,1,1,0,1,1,1,1,0,0,1,1,0,0,1,0,1,1,1,0,1,1,1,1,0,0,0,1,1,0,0,0,1,0,1,0,1,1,1,0,0,1,1,1,0,0,0,1,0,1,1,1,1,0,0,0,1,0},
				{0,0,1,0,0,1,0,1,1,0,0,1,0,0,0,1,0,1,1,0,1,1,1,1,0,1,0,1,1,0,0,1,1,0,0,1,0,0,0,0,1,1,0,0,1,1,1,1,1,0,0,0,0,1,0,0,1,0,0,0,1,1,1,1},
				{1,1,0,0,0,0,0,0,0,1,1,1,1,0,0,0,1,0,1,1,1,0,1,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,0,1,1,1,0,0,0,1,1,0,1,1,1,1,1,0,1,0,1,1,0,0,1,0,1,0},
				{0,1,1,0,0,1,1,1,1,0,0,1,1,1,0,0,1,1,0,0,0,0,0,1,1,1,0,1,0,1,1,0,0,1,0,1,0,0,0,0,1,0,0,1,0,1,0,1,1,0,0,0,1,1,0,1,1,0,0,1,1,1,1,0},
				{1,0,0,0,1,1,0,0,0,1,1,0,0,0,1,1,0,1,1,1,1,1,0,1,1,1,0,1,0,1,1,0,0,1,1,0,1,1,1,1,0,0,0,1,0,0,0,0,1,0,1,1,1,0,0,0,0,0,1,0,1,1,1,1},
				{1,1,0,0,0,1,0,0,0,1,1,1,1,0,0,1,1,1,0,0,1,0,1,1,0,0,1,1,1,0,0,1,1,1,0,0,0,1,0,0,1,1,1,1,1,1,0,0,0,0,1,0,1,1,1,1,1,0,0,0,1,1,0,0},
				{1,0,1,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,1,1,0,1,1,1,0,0,1,1,0,0,1,1,1,0,0,1,0,0,0,0,0,1,0,1,1,1,1,0,0,1,0,1,0,1,0,1,0,0,0,0,0,0,1,1},
				{1,1,1,1,0,0,0,1,0,1,0,1,0,0,1,1,1,1,0,0,0,1,1,0,0,1,1,0,0,0,0,1,0,0,0,1,1,1,0,0,1,0,0,1,1,1,0,1,1,0,0,1,1,0,1,0,1,1,1,0,0,1,0,1},
				{0,0,1,1,1,0,1,1,0,0,1,1,1,0,0,1,1,1,0,0,1,1,1,0,0,1,1,1,0,0,1,1,1,0,1,1,1,0,0,1,1,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,1,1,1,0,1,0,1},
				{0,0,0,1,1,1,0,1,1,0,1,1,1,1,0,1,1,1,1,1,0,0,1,1,0,1,1,1,0,0,0,1,1,0,0,1,1,0,0,0,1,0,0,1,1,0,0,0,0,1,0,0,1,1,1,1,0,0,0,0,1,1,0,1}

			
		};
		// Assume sequentially connected service composition
		for (int i = 0; i < 64; i++){
			
			double finalValue = Double.MAX_VALUE;
			for (int k = 0; k < users.length; k++) {
				writeDataset(path, "Workload-"+users[k], String.valueOf(users_values[k][i]));
			}
			
			for (int j = 0; j < services.length; j++) {
				
				double value = 0.0;
				double no = 0.0;
				for (int k = 0; k < users.length; k++) {
					if(users_values[k][0] == 1) {
						if("rtdata".equals(path)) {
						  double v = readProcessedData("processed/"+path, "service"+services[j], "user"+users[k], i);
						  //System.out.print(v + "\n");
						  if (v != 0) {
							  if(v < threshold) {
								  value++; 
							  }
							  
							  no++;
						  }
						} else {
						  double v = readProcessedData("processed/"+path, "service"+services[j], "user"+users[k], i);
						  if (v != 0) {
							  value += v;
							  no++;
						  }
						}
						
					}
					
					
					
				}
				
				value = no == 0.0? 0 : value / no;
				 //System.out.print(value + "\n");
				if("rtdata".equals(path)) { // assuming a total of 10 sequentially connected service, reliability
					double tempFinal = 1 -  (Math.pow(1 - value, service_values[j][i]));
					if(tempFinal != 0 && tempFinal < finalValue) {
						finalValue = tempFinal;
					}
				} else if("tpdata".equals(path)) {// assuming a total of 10 sequentially connected service, throughput
					double tempFinal = value * service_values[j][i];
					if(tempFinal != 0 && tempFinal < finalValue) {
						finalValue = tempFinal;
					}
				}
				
				writeDataset(path, "Control-"+services[j], String.valueOf(service_values[j][i]));
				
			}
			
			
			writeDataset(path, path, String.valueOf(finalValue));
			
		}
		
	}
	
	private static void parse(String path){
		File f = new File(prefix + path + ".txt");

		boolean r = true;

		System.out.print("Processing...");
		try {

			String line = null;
			// System.out.print("Read " + subFile.getAbsolutePath() + "\n");
			BufferedReader reader = new BufferedReader(new FileReader(f));
			while ((line = reader.readLine()) != null) {

				/*
				 * if (j <= 87) { j++; continue; }
				 */

				String[] split = line.split(" ");
				String service = "service"+split[1];
				String user = "user"+split[0];
				String data = split[2] + "-" + split[3];
				
				write("processed/"+path, service, user, data);
			}

			reader.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.print("Completed!");

	}
	
    private static double readProcessedData(String path, String service, String user, int time){
		
		String f = prefix + path + "/" + service + "/" + user + ".rtf";
		
		
		File file = new File(f);
		if(!file.exists()) {
			 System.out.print(f + "\n");
			return 0.0;
		}
		
		double r = 0.0;
		try {

			String line = null;
			// System.out.print("Read " + subFile.getAbsolutePath() + "\n");
			BufferedReader reader = new BufferedReader(new FileReader(f));
			while ((line = reader.readLine()) != null) {

				String[] p = line.split("-");
				if(Integer.parseInt(p[0]) == time) {
					r = Double.parseDouble(p[1]);
					break;
				}
			}

			reader.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return r;
	}
	
	
	private static void write(String path, String service, String user, String data){
		
		String f = prefix + path + "/" + service + "/" + user + ".rtf";
		File file = new File(prefix + path + "/" + service + "/");
		if(!file.exists()) {
			file.mkdirs();
		}
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));

			
			//System.out.print(data.toString() + "\n");
			bw.write(data + "\n");
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void writeDataset(String path, String name, String data){
		
		String f = prefix + "processed/dataset/" + path + "/" + name + ".rtf";
		
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));

			
			//System.out.print(data.toString() + "\n");
			bw.write(data + "\n");
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
