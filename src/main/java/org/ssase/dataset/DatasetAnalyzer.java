package org.ssase.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class DatasetAnalyzer {

	public static void main(String[] args) {
		new DatasetAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/femosaa/completed_results",  "Response Time", "Energy", true);
		//new DatasetAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/amazon-ec2/completed_results",  "Execution.txt", "-", false);
	}
	
	
	private void readFEMOSAA(String path, String v1, String v2, boolean secondLevel){
		// RT
		Map<String, Vector> result1 = new HashMap<String, Vector>();
		
		//Energy
		Map<String, Vector> result2 = new HashMap<String, Vector>();
		
		// RT
		Map<String, List<Double>> map1 = new HashMap<String, List<Double>>();
		
		//Energy
		Map<String, List<Double>> map2 = new HashMap<String, List<Double>>();
		
		
		// RT
		Map<String, List<Double>> less_map1 = new HashMap<String, List<Double>>();
		
		//Energy
		Map<String, List<Double>> less_map2 = new HashMap<String, List<Double>>();
		
		File f1 = new File(path);
		for (File f2 : f1.listFiles()) {
			if(f2.getName().equals(".DS_Store")) {
				continue;
			}
			System.out.print("Doing " + f2.getName() + "\n");
			for (File f3 : f2.listFiles()) {
				
				for (File f4 : f3.listFiles()) {
					//System.out.print("Doing " + f3.getName() + "\n");
					if(!f4.getName().equals(v2)) {
						if(secondLevel) {
						    read(f4, result1, map1, less_map1);
						} else {
							readOneLevel(f4, result1, map1, less_map1);	
						}
						
					} else {
						if(secondLevel) {
						   read(f4, result2, map2, less_map2);
						} else {
							readOneLevel(f4, result2, map2, less_map2);	
						}
					}
				}
				
			}
		}
		
		
		List<String> list = new ArrayList<String>();
		for(String key : result1.keySet()) {
			list.add(key);
		}
		
		Collections.sort(list);
		
		System.out.print("----------"+v1+"--------------\n");
		
		for(String key : list) {
			double v = result1.get(key).value / result1.get(key).size;
			System.out.print(key + ": " + v + "\n");
			//for(Double d : map1.get(key)) {
			//	System.out.print(d + "\n");
			//}
		}
		
		printCDF(path, less_map1,v1);
		System.out.print("----------"+v2+"--------------\n");
		if(result2.size() == 0) {
			return;
		}
		list.clear();
		for(String key : result2.keySet()) {
			list.add(key);
		}
		
		Collections.sort(list);
		
		for(String key : list) {
			double v = result2.get(key).value / result2.get(key).size;
			System.out.print(key + ": " + v + "\n");
		}
		
		printCDF(path,less_map2,v2);
		
	}
	
	private void printCDF(String path, Map<String, List<Double>> map, String v){
		List<Double> list = new ArrayList<Double>();
		Set <Double> set = new HashSet <Double>();
		int i = 0;
		for(String key : map.keySet()) {
			set.addAll(map.get(key));
			//list.addAll(map.get(key));
			i += map.get(key).size(); 
			System.out.print("size " + i + "\n");
		}
		
		list.addAll(set);
		Collections.sort(list);
		
		
		for(String key : map.keySet()) {
			System.out.print(key + "\n");
			List<Double> sub = map.get(key);
			String data = "";
			double pred = -1;
			for (Double d : list){
				if(d == pred) {
					continue;
				}
				double p = 0.0;
				for (Double d1 : sub) {
					
					if(d1 <= d) {
						p = p + 1.0;
					}
				}
				p = p / sub.size();
				data = data +d+" "+p+"\n";
				System.out.print("("+d+","+p+")\n");
				pred = d;
			}
			
			try {
				File f = new File(path+"/cdf/");
				if(!f.exists()) {
					f.mkdirs();
				}
				BufferedWriter bw = new BufferedWriter(new FileWriter(path+"/cdf/"+key+"-"+v.replace(" ", "")+".txt", true));

				
				//System.out.print(data.toString() + "\n");
				bw.write(data);
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void read(File f4, Map<String, Vector> result1, Map<String, List<Double>> map1, Map<String, List<Double>> less_map1){
		for (File f5 : f4.listFiles()) {
			List<Double> list = new ArrayList<Double>();
			System.out.print("*********Doing " + f5.getName() + "\n");
			for (File f6 : f5.listFiles()) {
				
				if(!f6.getName().endsWith("_data.rtf")) {
					continue;
				}
				
				//System.out.print("Doing " + f5.getName() + "\n");
				try {

					int k = 0;
					String line = null;
					// System.out.print("Read " + subFile.getAbsolutePath() + "\n");
					BufferedReader reader = new BufferedReader(new FileReader(f6));
					while ((line = reader.readLine()) != null) {

						if(k < list.size()) {
							double value = list.get(k) + Double.parseDouble(line);
							list.add(k, value);
						} else {
							list.add(Double.parseDouble(line));
						}
						
						
						 k++;
					}

					reader.close();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				
			}
			
			
			double total = 0.0;
			for (Double d : list) {
				double avg = d / 10.0;
				total += avg;
				if(!result1.containsKey(f5.getName())) {
					result1.put(f5.getName(), new Vector(avg, 1));
					map1.put(f5.getName(), new ArrayList<Double>());
					map1.get(f5.getName()).add(avg);
				} else {
					double v = result1.get(f5.getName()).value + avg;
					int size = result1.get(f5.getName()).size + 1;
					result1.put(f5.getName(), new Vector(v, size));
					map1.get(f5.getName()).add(avg);
				}
			}
			
			
			// AVG for a case
			total = total / list.size();
			if(!less_map1.containsKey(f5.getName())) {
				less_map1.put(f5.getName(), new ArrayList<Double>());
				less_map1.get(f5.getName()).add(total);
			} else {
				less_map1.get(f5.getName()).add(total);
			}
		
		}
		
	}
	
	private void readOneLevel(File f4, Map<String, Vector> result1, Map<String, List<Double>> map1, Map<String, List<Double>> less_map1){
		List<Double> list = new ArrayList<Double>();
		for (File f5 : f4.listFiles()) {
		
			
			//for (File f6 : f5.listFiles()) {
				
				if(!f5.getName().endsWith("_data.rtf")) {
					continue;
				}
				System.out.print("*********Doing " + f5.getName() + "\n");
				//System.out.print("Doing " + f5.getName() + "\n");
				try {

					int k = 0;
					String line = null;
					// System.out.print("Read " + subFile.getAbsolutePath() + "\n");
					BufferedReader reader = new BufferedReader(new FileReader(f5));
					while ((line = reader.readLine()) != null) {

						if(k < list.size()) {
							double value = list.get(k) + Double.parseDouble(line);
							list.add(k, value);
						} else {
							list.add(Double.parseDouble(line));
						}
						
						
						 k++;
					}

					reader.close();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				
			//}
			
			
			
		
		}
		
		
		double total = 0.0;
		for (Double d : list) {
			double avg = d / 10.0;
			total += avg;
			if(!result1.containsKey(f4.getName())) {
				result1.put(f4.getName(), new Vector(avg, 1));
				map1.put(f4.getName(), new ArrayList<Double>());
				map1.get(f4.getName()).add(avg);
			} else {
				double v = result1.get(f4.getName()).value + avg;
				int size = result1.get(f4.getName()).size + 1;
				result1.put(f4.getName(), new Vector(v, size));
				map1.get(f4.getName()).add(avg);
			}
		}
		
		
		// AVG for a case
		total = total / list.size();
		if(!less_map1.containsKey(f4.getName())) {
			less_map1.put(f4.getName(), new ArrayList<Double>());
			less_map1.get(f4.getName()).add(total);
		} else {
			less_map1.get(f4.getName()).add(total);
		}
	
	}
	
	
	private class Vector{
		protected double value;
		protected int size;
		public Vector(double value, int size) {
			super();
			this.value = value;
			this.size = size;
		}
		
		
	}
}
