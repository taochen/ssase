package org.ssase.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Data {

	static int startIndex = 0;
	static String prefix = "/Users/tao/research/analysis/results/";
	
	static String[] compare = new String[]{
		"moead/sas/rubis_software/",
		"moead-plain/sas/rubis_software/",
		"nsgaii/sas/rubis_software/",
		"gp/sas/rubis_software/",
		"bb/sas/rubis_software/"
	};
	
	static String[] compare1 = new String[]{
		"moead/sas/rubis_software/",
		"moead-plain/sas/rubis_software/",
		"moead-d/sas/rubis_software/",
		"moead-k/sas/rubis_software/",
		"moead-01/sas/rubis_software/"
	};
	
	static String[] obj = new String[]{
		"Response Time.rtf",
		"Energy.rtf",
	};
	
	static Map<String, Double> RTmap = new HashMap<String, Double> ();
	static Map<String, Double> Emap = new HashMap<String, Double> ();
	
	static double RTmax = 0;
	static double RTmin = 100000;
	
	static double Emax = 0;
	static double Emin = 100000;
	
	private static void test(){
		
		double sample = 102;
		
		double mean1 = 1.7;
		double var1 = 42;
		double mean2 = 1.9;
		double var2 = 25;
		
		double t = Math.abs(mean1 - mean2) / Math.pow(var1/sample + var2/sample, 0.5);
		
		double v = Math.pow(var1/sample + var2/sample, 2) / (var1*var1/(sample*sample*(sample-1)) + var2*var2/(sample*sample*(sample-1)) );
		System.out.print("t: " + t + ", v: " + v + "\n");
		System.out.print("m: " + Math.abs(mean1 - mean2) + ", v: " + Math.pow(var1/sample + var2/sample, 0.5) + "\n");
	}
	
	public static void main(String[] a) {
//		test();
//		
//		for (String n : compare) {
//		for (String o : obj) {
//			try {
//				double t = read(prefix+n+o);
//				System.out.print("Mean: " + n +", "+o+"="+t+"\n");
//			} catch (Throwable e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
		
		for (String n : compare) {
			for (String o : obj) {
				try {
					double t = readGmean(prefix+n+o,n,o);
					System.out.print("GMean: " + n +", "+o+"="+t+"\n");
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		System.out.print("\n");
		
		for (String n : compare) {
			double t = IGD(n);
			System.out.print("IGD: " + n +"="+t+"\n");
		}
		
		System.out.print("\n");
		
		for (String n : compare) {
			double t = HV(n);
			System.out.print("HV: " + n +"="+t+"\n");
		}
		
		System.out.print("\n");
		
		for (String n : compare) {
		
			try {
				double t = readDependency(prefix+n);
				System.out.print("Dependency: " + n +"="+t+"\n");
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		for (String n : compare) {
//			for (String o : obj) {
//				try {
//					double dp = 0.5;
//					double t = readP(prefix+n+o, dp);
//					System.out.print(dp+"P: " + n +", "+o+"="+t+"\n");
//				} catch (Throwable e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
	}
	
	
	private static double read(String name) throws Throwable {
		BufferedReader reader = new BufferedReader(new FileReader(new File(name)));
		String line = null;
		double total = 0;
		int i = 0;
		int no = 0;
		List<Double> list = new ArrayList<Double>();
		while((line = reader.readLine()) != null) {
			if(Double.parseDouble(line) != 0 && i < startIndex) {
				i++;
				continue;
			} 
			if(Double.parseDouble(line) != 0 ) {
				list.add(Double.parseDouble(line));
				total += Double.parseDouble(line) ;
				i++;
				no++;
			}
			
		}
		reader.close();
		
		
		double mean = total/no;
		double std = 0;
		for (int j = 0; j < list.size();j ++) {
			std += Math.pow(list.get(j) - mean, 2);
		}
		
		double v = std;
		std = Math.pow(std/no, 0.5);
		
		System.out.print(no+"STD: " + std + "\n");
		System.out.print(no+"Var: " + (v/no) + "\n");
		return total/no;
	}
	
	private static double readDependency(String name) throws Throwable {
		BufferedReader reader = new BufferedReader(new FileReader(new File(name.replace("rubis_software/", "Dependency.rtf"))));
		String line = null;
		double total = 0;
		int i = 0;
		int no = 0;
		List<Double> list = new ArrayList<Double>();
		while((line = reader.readLine()) != null) {
			if(Double.parseDouble(line) != 0 && i < startIndex) {
				i++;
				continue;
			} 
			if(Double.parseDouble(line) != 0) {
				total += Double.parseDouble(line) ;
				i++;
				no++;
			}
			
		}
		reader.close();
		return total/no;
	}
	
	private static double readGSD(Double[] values, double geoMean) {
		double gsd = 0.0;
		for (int i = 0; i < values.length; i++) {
			if (values[i] > 0 && geoMean > 0) {
				gsd = gsd + (Math.log(values[i]) - Math.log(geoMean))
						* (Math.log(values[i]) - Math.log(geoMean));
			}
		}
		gsd = gsd / (values.length);
		gsd = Math.sqrt(gsd);
		gsd = Math.exp(gsd);
		return gsd;
	}
	
	private static double readGmean(String name, String approach, String obj) throws Throwable {
		BufferedReader reader = new BufferedReader(new FileReader(new File(name)));
		String line = null;
		double total = 1;
		int i = 0;
		int no = 0;
		
		double htotal = 0;
		
		List<Double> list = new ArrayList<Double>();
		
		while((line = reader.readLine()) != null) {
			if(Double.parseDouble(line) != 0 && i < startIndex) {
				i++;
				continue;
			} 
			if(Double.parseDouble(line) != 0) {
				list.add(Double.parseDouble(line));
				
				total = total * Double.parseDouble(line) ;
				htotal = htotal + 1 / Double.parseDouble(line) ;
				i++;
				no++;
			}
			
		}
		reader.close();
		//System.out.print("t"+total+"\n");
		
//		if(obj.equals("Response Time.rtf")) {
//			return  (Math.pow(total, 1.0/no) - 0.05814646509491475) / (0.1185653677302746-0.05814646509491475);
//		} else {
//			return  (Math.pow(total, 1.0/no) - 4.129254192004044) / (4.808091886417567-4.129254192004044);
//			
//		}
		
		if(obj.equals("Response Time.rtf")) {
			
			if(Math.pow(total, 1.0/no) > RTmax) {
				RTmax = Math.pow(total, 1.0/no);
			}
			
            if(Math.pow(total, 1.0/no) < RTmin) {
				RTmin = Math.pow(total, 1.0/no);
			}
            
            RTmap.put(approach, Math.pow(total, 1.0/no));
			
		} else {
			
			if(Math.pow(total, 1.0/no) > Emax) {
				Emax = Math.pow(total, 1.0/no);
			}
			
            if(Math.pow(total, 1.0/no) < Emin) {
				Emin = Math.pow(total, 1.0/no);
			}
            
            Emap.put(approach, Math.pow(total, 1.0/no));
		}
		
		double gsd = readGSD(list.toArray(new Double[list.size()]),Math.pow(total, 1.0/no));
		//System.out.print("GSD: " + approach +", "+obj+"="+gsd+"\n");
		
		htotal = no / htotal;
		
		return Math.pow(total, 1.0/no);
	}
	
	private static double IGD(String approach) {
		
	
		
		double rt = (RTmap.get(approach) - RTmin) / (RTmax - RTmin);
		double e = (Emap.get(approach) - Emin) / (Emax - Emin);
		
//		double toN = Math.pow(Math.pow((rt - 1),2) + Math.pow((e - 1),2), 0.5)/2;
//		double toI = Math.pow(Math.pow((rt - 0),2) + Math.pow((e - 0),2), 0.5)/2;
//		return toN - toI;
		return Math.pow(Math.pow((rt - 0),2) + Math.pow((e - 0),2), 0.5)/2;
	}
	
	private static double HV(String approach) {
		
	
		
		double rt = (RTmap.get(approach) - RTmin) / (RTmax - RTmin);
		double e = (Emap.get(approach) - Emin) / (Emax - Emin);
		
		//System.out.print((1 - rt) + "*"+(1 - e)+"\n");
		return (1 - rt) * (1 - e);
	}
	
	private static double readP(String name, double dp) throws Throwable {
		BufferedReader reader = new BufferedReader(new FileReader(new File(name)));
		String line = null;
		
		int i = 0;
		int no = 0;
		List<Double> list = new ArrayList<Double>();
		
		while((line = reader.readLine()) != null) {
			if(Double.parseDouble(line) != 0 && i < startIndex) {
				i++;
				continue;
			} 
			if(Double.parseDouble(line) != 0) {
				list.add(Double.parseDouble(line)) ;
				i++;
				no++;
			}
			
		}
		
		int p = (int)Math.round(dp * no);
		
		reader.close();
		
		Collections.sort(list);
		
		return list.get(p);
		
	}
}
