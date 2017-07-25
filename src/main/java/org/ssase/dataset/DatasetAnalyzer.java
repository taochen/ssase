package org.ssase.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.io.*;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TTest;

import com.davidsoergel.dsutils.file.FileUtils;

/**
 * 
 * 
 * 
 * 
 * @author tao
 *
 */

public class DatasetAnalyzer {

	private static final String c1 = "onBOOSTING-TREE";
	private static final String c2 = "offBOOSTING-TREE";
	private static final String file = "_time.rtf"; //_data.rtf
	private static Map<String, List<Double>> global = new HashMap<String, List<Double>>();
	private static Map<String, List<Double>> detail_global = new HashMap<String, List<Double>>();
	private static boolean print_trace = false;
	
	public static void main1(String[] args) {
		new DatasetAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/femosaa/completed_results",  "Response Time", "Energy", true);
		new DatasetAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/wsdream/processed/completed_results",  "rtdata.rtf", "tpdata.rtf", false);
		new DatasetAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/amazon-ec2/completed_results",  "Execution.txt", "-", false);
	
		printCDF("/Users/tao/research/experiments-data/on-off/femosaa/completed_results", global, "All", new String[]{c1,c2});
		//printCDF("/Users/tao/research/experiments-data/on-off/femosaa/completed_results", global, "All", new String[]{"onTREE","offTREE"});
		//printCDF("/Users/tao/research/experiments-data/on-off/femosaa/completed_results", global, "All", new String[]{"onSVM","offSVM"});
		//printCDF("/Users/tao/research/experiments-data/on-off/femosaa/completed_results", global, "All", new String[]{"onMLP","offMLP"});
		//printCDF("/Users/tao/research/experiments-data/on-off/femosaa/completed_results", global, "All", new String[]{"onBAGGING-LR","offBAGGING-LR"});
		//printCDF("/Users/tao/research/experiments-data/on-off/femosaa/completed_results", global, "All", new String[]{"onBOOSTING-LR","offBOOSTING-LR"});
		//printCDF("/Users/tao/research/experiments-data/on-off/femosaa/completed_results", global, "All", new String[]{"onBAGGING-TREE","offBAGGING-TREE"});
		//printCDF("/Users/tao/research/experiments-data/on-off/femosaa/completed_results", global, "All", new String[]{"onBOOSTING-TREE","offBOOSTING-TREE"});
	}
	
	public static void main(String[] args) {
		//new DatasetAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/femosaa/completed_results",  "Response Time", "Energy", true);
		//new DatasetAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/wsdream/processed/completed_results",  "rtdata.rtf", "tpdata.rtf", false);
		new DatasetAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/amazon-ec2/completed_results",  "Execution.txt", "-", false);
		//new DatasetAnalyzer().readFEMOSAA("/Users/tao/research/experiments-data/on-off/amazon-ec2/dataset/completed_results_new",  "Execution.txt", "-", false);
		
		//printCDF("/Users/tao/research/experiments-data/on-off/femosaa/completed_results", detail_global, "All", new String[]{c1,c2});
		
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
			if(f2.getName().equals("cdf")) {
				continue;
			}
			if(f2.getName().equals(".DS_Store")) {
				continue;
			}
//			System.out.print("Doing " + f2.getName() + "\n");
//			
//			if(!f2.getName().equals("read-write-7")) {
//				continue;
//			}
//			
			
//			if(!f2.getName().equals("data1")) {
//			continue;
//		}
			//System.out.print("qos name " + f2.getName() + "\n");
			for (File f3 : f2.listFiles()) {
				
//				if(!f3.getName().equals("decrease")) {
//					continue;
//				}
				//System.out.print("qos name " + f3.getName() + "\n");
				for (File f4 : f3.listFiles()) {
					//System.out.print("qos name " + f3.getName() + "\n");
					
					if(secondLevel) {
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
					} else {
						if(!f3.getName().equals(v2)) {
							readOneLevel(f4, result1, map1, less_map1);	
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
		
		String[] keys = new String[2];
		int i = 0;
		for(String key : list) {
			
			detail_global.put(key, new ArrayList<Double>());
			
			double v = result1.get(key).value / result1.get(key).size;
			System.out.print(key + ": " + v + "\n");
			
			if(print_trace) {
			
			List<Double> new_list1 = new ArrayList<Double>();
			int k = 0;
			for(Double d : map1.get(key)) {
				if(k > 89) {
					k=0;
				}
				if(k >= new_list1.size()) {
					new_list1.add(k, d);
				} else {
					double old = new_list1.remove(k);
					new_list1.add(k, old + d);
				}
				
				//System.out.print("(" + k + "," +d + ")\n");
				k++;
			}
			
			double dvide = 1;//map1.get(key).size() / 90.0;
			System.out.print("dvide" + dvide + "\n");
			k = 1;
			for(Double d : new_list1) {
				System.out.print("(" + k + "," +(d/dvide) + ")\n");
				detail_global.get(key).add((d/dvide));
				k++;
			}
			
			}
			
			keys[i] = key;
			i++;
		}
		
		
		//statisticTest(less_map1, keys);
		//statisticTest(map1, keys);
		
		getBoxPlot(less_map1, keys);
		
		//printCDF(path, less_map1,v1);
		System.out.print("----------" + v2 + "--------------\n");
		if (result2.size() != 0) {

			list.clear();
			for (String key : result2.keySet()) {
				list.add(key);
			}

			Collections.sort(list);

			for (String key : list) {
				double v = result2.get(key).value / result2.get(key).size;
				System.out.print(key + ": " + v + "\n");
				
				if(print_trace) {
				
				List<Double> new_list2 = new ArrayList<Double>();
				int k = 0;
				for(Double d : map2.get(key)) {
					if(k > 89) {
						k=0;
					}
					if(k >= new_list2.size()) {
						new_list2.add(k, d);
					} else {
						double old = new_list2.remove(k);
						new_list2.add(k, old + d);
					}
					
					
					//System.out.print("(" + k + "," +d + ")\n");
					k++;
				}
				
				double dvide = 1;//map2.get(key).size() / 90.0;
				
				k = 1;
				for(Double d : new_list2) {
					System.out.print("(" + k + "," +(d/dvide) + ")\n");
					k++;
				}
				
				}
			}
			
			//statisticTest(less_map2, keys);
			//statisticTest(map2, keys);
			getBoxPlot(less_map2, keys);
		}
		
		//printCDF(path,less_map2,v2);
		
		
		double max1 = Double.MIN_VALUE;
		double min1 = Double.MAX_VALUE;
		
		double max2 = Double.MIN_VALUE;
		double min2 = Double.MAX_VALUE;
		
		for(String key : list) {
			
			
			for(Double d : less_map1.get(key)) {
				if(d > max1) {
					max1 = d;
				}
				
				if(d < min1) {
					min1 = d;
				}
			}
			
			if(less_map2.size() != 0) {
			
			for(Double d : less_map2.get(key)) {
				if(d > max2) {
					max2 = d;
				}
				
				if(d < min2) {
					min2 = d;
				}
			}
			}
		}
		System.out.print("max1 " + max1 + " min1 " + min1 + "\n");
		System.out.print("max2 " + max2 + " min2 " + min2 + "\n");
		for(String key : list) {
			
			List<Double> newList = new ArrayList<Double>();
			
			double nor = 0.0;
			for(Double d : less_map1.get(key)) {
				newList.add((d - min1) / (max1 - min1));
				nor += (d - min1) / (max1 - min1);
				//System.out.print(key + " " +  d + " : " + ((d - min1) / (max1 - min1)) + "\n");
			}
			
			nor = nor / less_map1.get(key).size();
			System.out.print(key + " " + v1 + " normalized " + nor + "\n");
			
			less_map1.put(key, newList);
			
			nor = 0.0;
			if(less_map2.size() != 0) {
			
				newList = new ArrayList<Double>();
				
				nor = 0.0;
				for(Double d : less_map2.get(key)) {
					newList.add((d - min2) / (max2 - min2));
					nor += (d - min2) / (max2 - min2);
					//System.out.print(key +" " + d + " : "  + ((d - min2) / (max2 - min2)) + "\n");
				}
				
				nor = nor / less_map2.get(key).size();
				System.out.print(key + " " + v2 + " normalized " + nor + "\n");
				
				less_map2.put(key, newList);
			
			}
		}
		
		for(String key : list) {
			
			if(!global.containsKey(key)) {
				global.put(key, new ArrayList<Double>());
			}
			
			global.get(key).addAll(less_map1.get(key));
			if(less_map2.size() != 0) {
				global.get(key).addAll(less_map2.get(key));
			}
			
		}
		
	}
	
	private void getBoxPlot(Map<String, List<Double>> map1, String[] keys){
		
		
		List<Double> list1 = map1.get(keys[0]);
		List<Double> list2 = map1.get(keys[1]);
		
		Collections.sort(list1);
		Collections.sort(list2);
		
		System.out.print("list1 " + list1.size()+"\n");
		System.out.print("list2 " + list2.size()+"\n");
		/**
		 *  median=1,
      upper quartile=1.2,
      lower quartile=0.4,
      upper whisker=1.5,
      lower whisker=0.2,
		 * 
		 */
		
		double max=0.0, min=0.0, uq=0.0, lq=0.0, median=0.0;
		
		System.out.print("----------" + keys[0] + "----------\n");
		
		max = list1.get(list1.size()-1);
		min = list1.get(0);
		median = getMediam(list1);
		if (list1.size() % 2 == 0) {
			uq = getMediam(list1.subList(list1.size()/2+1, list1.size()-1));
			lq = getMediam(list1.subList(0, list1.size()/2-2));
		} else {
			uq = getMediam(list1.subList(list1.size()/2+1, list1.size()-1));
			lq = getMediam(list1.subList(0, list1.size()/2-1));
		}
		System.out.print("median="+median+",\n");
		System.out.print("upper quartile="+uq+",\n");
		System.out.print("lower quartile="+lq+",\n");
		System.out.print("upper whisker="+max+",\n");
		System.out.print("lower whisker="+min+",\n");
		
		System.out.print("----------" + keys[1] + "----------\n");
		
		max = list2.get(list2.size()-1);
		min = list2.get(0);
		median = getMediam(list2);
		if (list2.size() % 2 == 0) {
			uq = getMediam(list2.subList(list2.size()/2+1, list2.size()-1));
			lq = getMediam(list2.subList(0, list2.size()/2-2));
		} else {
			uq = getMediam(list2.subList(list2.size()/2+1, list2.size()-1));
			lq = getMediam(list2.subList(0, list2.size()/2-1));
		}
		System.out.print("median="+median+",\n");
		System.out.print("upper quartile="+uq+",\n");
		System.out.print("lower quartile="+lq+",\n");
		System.out.print("upper whisker="+max+",\n");
		System.out.print("lower whisker="+min+",\n");
	}
	
	private double getMediam(List<Double> list) {
		if (list.size() % 2 == 0)
		    return ((double)list.get(list.size()/2) + (double)list.get(list.size()/2-1))/2.0;
		else
			return (double) list.get(list.size()/2);
	}
	
	private void statisticTest(Map<String, List<Double>> map1, String[] keys){
		
		double[] x = new double[map1.get(keys[0]).size()];
		double[] y = new double[map1.get(keys[0]).size()];
		System.out.print("x" + x.length + "\n");
		for (int i = 0;i < x.length;i++) {
			x[i] = map1.get(keys[0]).get(i);
			
			y[i] = map1.get(keys[1]).get(i);
		}
		
		WilcoxonSignedRankTest test = new WilcoxonSignedRankTest();
		double p = test.wilcoxonSignedRankTest(x, y, false);
		double es = test.getEffectSize(x, y);
		
		TTest t = new TTest();
		MannWhitneyUTest m = new MannWhitneyUTest();
		
		System.out.print("p-value1=" + t.pairedTTest(x, y) + "\n");
		System.out.print("p-value2=" + p + "\n");
		System.out.print("effect size1=" + effectSize(x,y) + "\n");
		System.out.print("effect size2=" + es + "\n");
	}
	
	private double effectSize(double[] x, double[] y) {
		
		double m1 = 0.0;
		double m2 = 0.0;
		
		for (double d : x) {
			m1+=d;
		}
		
		for (double d : y) {
			m2+=d;
		}
		
		m1 = m1/x.length;
		m2 = m2/y.length;
		
		
		double std1 = 0.0;
		double std2 = 0.0;
		
		for (double d : x) {
			std1 += Math.pow((d-m1), 2);
		}
		
		for (double d : y) {
			std2 += Math.pow((d-m2), 2);
		}
		
		std1 = std1/x.length;
		std2 = std2/y.length;
		double std = (std1+std2)/2;
		std = Math.pow(std, 0.5);
		
		return Math.abs(m2-m1)/std;
		
	}
	
	private static void printCDF(String path, Map<String, List<Double>> map, String v, String[] keys){
		List<Double> list = new ArrayList<Double>();
		Set <Double> set = new HashSet <Double>();
		int i = 0;
		for(String key : keys) {
			set.addAll(map.get(key));
			//list.addAll(map.get(key));
			i += map.get(key).size(); 
			//System.out.print("size " + i + "\n");
		}
		
		list.addAll(set);
		Collections.sort(list);
		
		
		for(String key : keys) {
			//System.out.print(key + "\n");
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
				//System.out.print("("+d+","+p+")\n");
				pred = d;
			}
			
			try {
				File f = new File(path+"/cdf/");
				if(!f.exists()) {
					f.mkdirs();
				}
				BufferedWriter bw = new BufferedWriter(new FileWriter(path+"/cdf/"+key+"-"+v.replace(" ", "")+".txt", true));

				
				//System.out.print(data.toString() + "\n");
				//bw.write(data);
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void read(File f4, Map<String, Vector> result1, Map<String, List<Double>> map1, Map<String, List<Double>> less_map1){
		for (File f5 : f4.listFiles()) {
			
			if(!f5.getName().equals(c1) && !f5.getName().equals(c2)) {
				continue;
			}
			
			
			
			List<Double> list = new ArrayList<Double>();
			//System.out.print("*********Doing " + f5.getName() + "\n");
			for (File f6 : f5.listFiles()) {
				
				if(/**!f6.getName().endsWith("_data.rtf")**/ f6.getName().endsWith("_nano_time.rtf") ||
						f6.getName().endsWith("_data.rtf") ) {
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
							list.remove(k);
							list.add(k, value);
						} else {
							list.add(Double.parseDouble(line));
						}
						
						
						 k++;
						 
						 if(print_trace && k == 90) {
							 break;
						 }
					}

					reader.close();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				
			}
			
//			// Remove 
			if(print_trace && list.size() < 90) {
				continue;
			}
			
			double total = 0.0;
		//	System.out.print(f5 +" list size " + list.size() + "\n");
			for (Double d : list) {
				double avg = d / 10.0;
				
				// Remove non-sensed data
				if("_data.rtf".equals(file) && d > 100000.0) {
					System.out.print(f5 + " " + d + " is greater than others\n");
					continue;
				}
				
				if("_time.rtf".equals(file)) {
					avg = avg / 1000000.0;
				}
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
//			
//			if(total > 100000) {
//				System.out.print(f5 + " " + total + " is greater than others\n");
//				for (double d : list) {
//					System.out.print((d/10.0) + "\n");
//				}
//			}
		
		}
		
	}
	
	private void readOneLevel(File f4, Map<String, Vector> result1, Map<String, List<Double>> map1, Map<String, List<Double>> less_map1){
		List<Double> list = new ArrayList<Double>();
		if(!f4.getName().equals(c1) && !f4.getName().equals(c2)) {
		return;
	}
		//System.out.print("*********Doing " + f4.getName() + "\n");
		for (File f5 : f4.listFiles()) {
		

			
			//for (File f6 : f5.listFiles()) {
				
				if( /**!f5.getName().endsWith("_data.rtf")**/f5.getName().endsWith("_nano_time.rtf") ||
						f5.getName().endsWith("_data.rtf") ) {
					continue;
				}
				//System.out.print("*********Doing " + f5.getName() + "\n");
				//System.out.print("Doing " + f5.getName() + "\n");
				try {

					int k = 0;
					String line = null;
					// System.out.print("Read " + subFile.getAbsolutePath() + "\n");
					BufferedReader reader = new BufferedReader(new FileReader(f5));
					while ((line = reader.readLine()) != null) {

						if(k < list.size()) {
							double value = list.get(k) + Double.parseDouble(line);
							list.remove(k);
							list.add(k, value);
						} else {
							list.add(Double.parseDouble(line));
						}
						
						 if(print_trace && k == 890) {
							 break;
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
		
		if(print_trace && list.size() < 10) {
			return;
		}
		
		double total = 0.0;
		System.out.print("list size " + list.size() + "\n");
		for (Double d : list) {
			double avg = d / 10.0;
			if("_time.rtf".equals(file)) {
				avg = avg / 1000000.0;
			}
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
	
	public static void main2(String[] args) {
		copy(new File("/Users/tao/research/experiments-data/on-off/femosaa/1st-completed_results"), 
				new File("/Users/tao/research/experiments-data/on-off/femosaa/completed_results"));
	}
	
	private static void copy(File f, File dest){
		
		System.out.print(f+"\n");
		//read-2
		for (File f1 : f.listFiles()) {
		
			if(f1.getName().equals("cdf") || !f1.isDirectory()) {
				continue;
			}
		
		
			//bursty
			for (File f2 : f1.listFiles()) {
				
				//Resposne Time
				for (File f3 : f2.listFiles()) {
					
					// onLR
					for (File f4 : f3.listFiles()) {
						
					
                         
                         if(f4.getName().equals("onSVM")) {
 							

 							System.out.print("copy " + f4.getName() + "\n");
 							copyFolder(f4, new File(dest+"/"+f1.getName()+"/"+f2.getName()+"/"+f3.getName()+"/"+f4.getName()));
 							
 						}
                          
                         
                         
						
					}
					
					
				}
				
				
			}
		}
	}
	
	public static void copyFolder(File source, File destination)
	{
	    if (source.isDirectory())
	    {
	        if (!destination.exists())
	        {
	            destination.mkdirs();
	        }

	        String files[] = source.list();

	        for (String file : files)
	        {
	            File srcFile = new File(source, file);
	            File destFile = new File(destination, file);

	            copyFolder(srcFile, destFile);
	        }
	    }
	    else
	    {
	        InputStream in = null;
	        OutputStream out = null;

	        try
	        {
	            in = new FileInputStream(source);
	            out = new FileOutputStream(destination);

	            byte[] buffer = new byte[1024];

	            int length;
	            while ((length = in.read(buffer)) > 0)
	            {
	                out.write(buffer, 0, length);
	            }
	        }
	        catch (Exception e)
	        {
	            try
	            {
	                in.close();
	            }
	            catch (IOException e1)
	            {
	                e1.printStackTrace();
	            }

	            try
	            {
	                out.close();
	            }
	            catch (IOException e1)
	            {
	                e1.printStackTrace();
	            }
	        }
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
