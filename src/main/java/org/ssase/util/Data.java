package org.ssase.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.*;

public class Data {

	static int startIndex = 0;
	static String prefix = "/Users/tao/research/analysis/results/";
	
	static String[] compare = new String[]{
		"moead/sas/rubis_software/",
//		"moead-plain/sas/rubis_software/",
//		"nsgaii/sas/rubis_software/",
//		"gp/sas/rubis_software/",
//		"bb/sas/rubis_software/"
		
		//"debt-aware/htree-0.01-rt-0.05-3.5-ec-5-0.5/sas/rubis_software/",
		//"debt-aware/nb-0.01-rt-0.05-3.5-ec-5-0.5/sas/rubis_software/",
		//"debt-aware/svm-0.01-rt-0.05-3.5-ec-5-0.5/sas/rubis_software/",
		"debt-aware/random-10/sas/rubis_software/",
		//"debt-aware/rt-0.05-ec-5/sas/rubis_software/"
		
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
	
	static Map<String, List<Double>> Vmap = new HashMap<String, List<Double>> ();
	
	
	static double RTmax = 0;
	static double RTmin = 100000;
	
	static double Emax = 0;
	static double Emin = 100000;
	
	
	static Map<String, List<Double>> AdaMap = new HashMap<String, List<Double>> ();
	
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
//				double t = read(prefix+n+o,o);
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
		
		System.out.print("\n");
		
		for (String n : compare) {
			
			try {
				double t = readTime(prefix+n);
				System.out.print("Execution Time: " + n +"="+t+"\n");
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	    for (String n : compare) {
			
			try {
				double t = 	readAdaptation(prefix+n);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	    compareAdaptationStep(prefix+compare[0], prefix+compare[1], compare[0], compare[1], obj[0]);
	
		
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
	
	
	private static double read(String name, String obj) throws Throwable {
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
				if(obj.equals("Response Time.rtf")) {
					list.add(Double.parseDouble(line)* 1000);
					total += Double.parseDouble(line)* 1000 ;
				} else {
					list.add(Double.parseDouble(line)* 1);
					total += Double.parseDouble(line)* 1 ;
				}
			
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
	
	
	private static void compareAdaptationStep(String name1, String name2, String a1, String a2, String obj)  {
		
		List<Double> list1 = AdaMap.get(name1);
		List<Double> list2 = AdaMap.get(name2);
		
		
		List<Double> missingFrom1 = new ArrayList<Double>();
		List<Double> missingFrom2 = new ArrayList<Double>();
		
		
		for(Double d : list1) {
			if(!list2.contains(d)) {
				missingFrom2.add(d);
				//System.out.print("missingFrom2 " + d + "\n");
			}
		}
		
		for(Double d : list2) {
			if(!list1.contains(d)) {
				missingFrom1.add(d);
				//System.out.print("missingFrom1 " + d + "\n");
			}
		}
		
		int f = 0;
		
		System.out.print(missingFrom1.size()+" In " + name2 + " but not in " + name1 + "\n");
		System.out.print(a1  + "\n");
//		for(Double d : missingFrom1) {
//			
//			if((int)(d  + f - 122) < Vmap.get(a1+obj).size()) {
//				System.out.print(Vmap.get(a1+obj).get((int)(d + f - 122))+"\n");
//			}
//		}
//		
//		System.out.print(a2  + "\n");
//		for(Double d : missingFrom1) {
//			if((int)(d + f - 122) < Vmap.get(a2+obj).size()) {
//				System.out.print(Vmap.get(a2+obj).get((int)(d + f  - 122))+"\n");
//			}
//		}
		
		Set<Double> filter = new HashSet<Double>();
		
		for(Double d : missingFrom1) {
			int l = -1;
			for (int k = 0; k < AdaMap.get(name1).size();k++) {
				//System.out.print("AdaMap.get(name1).get(k) " + AdaMap.get(name1).get(k) + " d " + d + "\n");	
				if(AdaMap.get(name1).get(k) > d ) {
				
				
				    l = (int) (AdaMap.get(name1).get(k) - d);
					
					
					
					break;
				}
			}
			
			if (l == -1 && missingFrom1.indexOf(d)+1 < missingFrom1.size()) {
				l = (int) (missingFrom1.get(missingFrom1.indexOf(d)+1) - d);
			}
			
			
	    	if(missingFrom1.indexOf(d) == missingFrom1.size() - 1 && (d - 122) < Vmap.get(a1+obj).size()) {
				l = (int) (Vmap.get(a1+obj).size() + 122 - d);
			}
			
			for (int i = 0; i < l; i++) {
				if(filter.contains(d + i)) {
					continue;
				}
				System.out.print( Vmap.get(a1+obj).get((int)(d + i - 122))+"\n");
				filter.add(d + i);
			}
			
		}
		
		filter.clear();
		
		System.out.print(a2  + "\n");
	    for(Double d : missingFrom1) {
	    	int l = -1;
	    	for (int k = 0; k < AdaMap.get(name2).size();k++) {
				//System.out.print("AdaMap.get(name1).get(k) " + AdaMap.get(name1).get(k) + " d " + d + "\n");	
				if(AdaMap.get(name2).get(k) > d ) {
				
				
				    l = (int) (AdaMap.get(name2).get(k) - d);
					
					
					
					break;
				}
			}
	    	
	    	if (l == -1 && missingFrom1.indexOf(d)+1 < missingFrom1.size()) {
				l = (int) (missingFrom1.get(missingFrom1.indexOf(d)+1) - d);
			}
	    	
	    	if(missingFrom1.indexOf(d) == missingFrom1.size() - 1 && (d - 122) < Vmap.get(a2+obj).size()) {
				l = (int) (Vmap.get(a2+obj).size() + 122 - d);
			}
			
			for (int i = 0; i < l; i++) {
				if(filter.contains(d + i)) {
					continue;
				}
				System.out.print(Vmap.get(a2+obj).get((int)(d + i - 122))+"\n");
				filter.add(d + i);
			}
			
			
		}
		
		System.out.print(missingFrom2.size()+" In " + name1 + " but not in " + name2 + "\n");
		System.out.print(a1   + "\n");
//		for(Double d : missingFrom2) {
//			
//			if((int)(d + f - 122) < Vmap.get(a1+obj).size()) {
//				System.out.print(Vmap.get(a1+obj).get((int)(d+ f  - 122))+"\n");
//			}
//		}
//		
//		System.out.print(a2  + "\n");
//		for(Double d : missingFrom2) {
//			if((int)(d + f - 122) < Vmap.get(a2+obj).size()) {
//				System.out.print(Vmap.get(a2+obj).get((int)(d + f - 122))+"\n");
//			}
//		}
		
		filter.clear();
		for(Double d : missingFrom2) {
			int l = -1;
			for (int k = 0; k < AdaMap.get(name1).size();k++) {
				//System.out.print("AdaMap.get(name1).get(k) " + AdaMap.get(name1).get(k) + " d " + d + "\n");	
				if(AdaMap.get(name1).get(k) > d ) {
				
				
				    l = (int) (AdaMap.get(name1).get(k) - d);
					
					
					
					break;
				}
			}
			
			if (l == -1 && missingFrom2.indexOf(d)+1 < missingFrom2.size()) {
				l = (int) (missingFrom2.get(missingFrom2.indexOf(d)+1) - d);
			}
			
			if(missingFrom2.indexOf(d) == missingFrom2.size() - 1 && (d - 122) < Vmap.get(a1+obj).size()) {
				l = (int) (Vmap.get(a1+obj).size() + 122 - d);
			}
			
			for (int i = 0; i < l; i++) {
				if(filter.contains(d + i)) {
					continue;
				}
				System.out.print( Vmap.get(a1+obj).get((int)(d + i - 122))+"\n");
				filter.add(d + i);
			}
			
		}
		
		filter.clear();
		System.out.print(a2  + "\n");
	    for(Double d : missingFrom2) {
	    	int l = -1;
	    	for (int k = 0; k < AdaMap.get(name2).size();k++) {
				//System.out.print("AdaMap.get(name1).get(k) " + AdaMap.get(name1).get(k) + " d " + d + "\n");	
				if(AdaMap.get(name2).get(k) > d ) {
				
				
				    l = (int) (AdaMap.get(name2).get(k) - d);
					
				    //System.out.print(AdaMap.get(name2).get(k) + ":" + d +"start\n");
					
					break;
				}
			}
	    	
	    	if (l == -1 && missingFrom2.indexOf(d)+1 < missingFrom2.size()) {
				l = (int) (missingFrom2.get(missingFrom2.indexOf(d)+1) - d);
			} 
	    	
	    	if(missingFrom2.indexOf(d) == missingFrom2.size() - 1 && (d - 122) < Vmap.get(a2+obj).size()) {
				l = (int) (Vmap.get(a2+obj).size() + 122 - d);
			}
	    
			for (int i = 0; i < l; i++) {
				if(filter.contains(d + i)) {
					continue;
				}
				System.out.print( Vmap.get(a2+obj).get((int)(d + i - 122))+"\n");
				filter.add(d + i);
			}
			
			
		}
		
	}
	
	private static double readAdaptation(String name) throws Throwable {
		BufferedReader reader = new BufferedReader(new FileReader(new File(name.replace("rubis_software/", "Executions.rtf"))));
		String line = null;
		double total = 0;
		int i = 0;
		int no = 0;
		List<Double> list = new ArrayList<Double>();
		
		if(!AdaMap.containsKey(name)) {
			AdaMap.put(name, list);
		}
		
		while((line = reader.readLine()) != null) {
			
			if(line.startsWith("-----------")) {
				list.add(Double.parseDouble(line.split("-----------")[1]));
				System.out.print("Adaptation Step: " + name +"="+Double.parseDouble(line.split("-----------")[1])+"\n");
			}
			
		}
		reader.close();
		
		
		return total/no;
	}
	
	
	private static double readTime(String name) throws Throwable {
		BufferedReader reader = new BufferedReader(new FileReader(new File(name.replace("rubis_software/", "Execution-Time.rtf"))));
		String line = null;
		double total = 0;
		int i = 0;
		int no = 0;
		List<Double> list = new ArrayList<Double>();
		while((line = reader.readLine()) != null) {
			if(i < startIndex) {
				i++;
				continue;
			} 
			//if(Double.parseDouble(line) != 0) {
				total += Double.parseDouble(line)  ;
				i++;
				no++;
			//}
			
		}
		reader.close();
		
		System.out.print("Adaptation Time: " + name +"="+no+"\n");
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
			if(i < startIndex) {
				i++;
				continue;
			} 
			//if(Double.parseDouble(line) != 0) {
				total += Double.parseDouble(line)  ;
				i++;
				no++;
			//}
			
		}
		reader.close();
		return total/no;
	}
	
	private static double readGSD(Double[] values, double geoMean) {
		double gsd = 0.0;
		for (int i = 0; i < values.length; i++) {
			//System.out.print(Math.log10(values[i])+"\n");
			//if (values[i] > 0 && geoMean > 0) {
				gsd = gsd + (Math.log10(values[i]) - Math.log10(geoMean) 
						* (Math.log10(values[i]) - Math.log10(geoMean)));
			//}
		}
		gsd = gsd / (values.length);
		gsd = Math.sqrt(gsd);
		gsd = Math.exp(gsd);
		return gsd;
	}
	
	
	private static void log(Double[] values) {
		double gsd = 0.0;
		String d = "";
		String n = "";
		Double[] newValues = new Double[values.length];
		for (int i = 0; i < values.length; i++) {
			newValues[i] = Math.log10(values[i]);
		}
		
		for (int i = 0; i < newValues.length; i++) {
			System.out.print(newValues[i] + "\n");
			//System.out.print("("+i+","+newValues[i] + ")\n");
			d = d + newValues[i] + ",";
			n = n + (i + 1) + ",";
			gsd += newValues[i];
		}
		
		System.out.print("Mean " + gsd/newValues.length + "\n");
		
		double mean = gsd/newValues.length;
		double var = 0.0;
		for (int i = 0; i < newValues.length; i++) {
			var += Math.pow((newValues[i] - mean), 2);
		}
		
		System.out.print("Var "+var/newValues.length + "\n");
		System.out.print(d + "\n");
		System.out.print(n + "\n");
	}
	
	
	private static double readGmean(String name, String approach, String obj) throws Throwable {
		BufferedReader reader = new BufferedReader(new FileReader(new File(name)));
		String line = null;
		double total = 1;
		int i = 0;
		int no = 0;
		BigDecimal bd = new BigDecimal(1);
		double htotal = 0;
		
		List<Double> list = new ArrayList<Double>();
		
		Vmap.put(approach+obj, new ArrayList<Double>());
		
		while((line = reader.readLine()) != null) {
			if(Double.parseDouble(line) != 0 && i < startIndex) {
				i++;
				continue;
			} 
			if(Double.parseDouble(line) != 0) {
				if(obj.equals("Response Time.rtf")) {

					list.add(Double.parseDouble(line)*1000);		
					bd = bd.multiply(new BigDecimal(Double.parseDouble(line))).multiply(new BigDecimal(1000));
					Vmap.get(approach+obj).add(Double.parseDouble(line));
				} else {
					list.add(Double.parseDouble(line)*1);		
					bd = bd.multiply(new BigDecimal(Double.parseDouble(line))).multiply(new BigDecimal(1));
					Vmap.get(approach+obj).add(Double.parseDouble(line));
				}
				
				total = total * (Double.parseDouble(line)) ;
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
		
	
			double gsd = readGSD(list.toArray(new Double[list.size()]),Math.pow(bd.doubleValue(), 1.0/no));
			System.out.print("New Gmean: " + approach +", "+obj+"="+Math.pow(bd.doubleValue(), 1.0/no)+ ", G-SD="+gsd+"\n");
		
		
		//System.out.print("GSD: " + approach +", "+obj+"="+gsd+"\n");
		//System.out.print("CI: " + approach +", "+obj+"=["+(Math.pow(total, 1.0/no)-1.96*gsd/Math.sqrt(list.size())) + 
			//	", " + (Math.pow(total, 1.0/no)+1.96*gsd/Math.sqrt(list.size())) +"]\n");
		//System.out.print("GVAR: " + approach +", "+obj+"="+(gsd*gsd)+"\n");
		
		if(obj.equals("Response Time.rtf")) 
		log(list.toArray(new Double[list.size()]));
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
	private static final int SCALE = 10;
	  private static final int ROUNDING_MODE = BigDecimal.ROUND_HALF_DOWN;
	
	  private static BigDecimal nthRoot(final int n, final BigDecimal a, final BigDecimal p) {
		    if (a.compareTo(BigDecimal.ZERO) < 0) {
		      throw new IllegalArgumentException("nth root can only be calculated for positive numbers");
		    }
		    if (a.equals(BigDecimal.ZERO)) {
		        return BigDecimal.ZERO;
		    }
		    BigDecimal xPrev = a;
		    BigDecimal x = a.divide(new BigDecimal(n), SCALE, ROUNDING_MODE);  // starting "guessed" value...
		    while (x.subtract(xPrev).abs().compareTo(p) > 0) {
		        xPrev = x;
		        x = BigDecimal.valueOf(n - 1.0)
		              .multiply(x)
		              .add(a.divide(x.pow(n - 1), SCALE, ROUNDING_MODE))
		              .divide(new BigDecimal(n), SCALE, ROUNDING_MODE);
		    }
		    return x;
		  }
}
