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
		
// ###################### read write ######################	

// ====================== moead-stm ====================== 
//	
//		"moead/sas/rubis_software/",
//		"moead-plain/sas/rubis_software/",
//		"moead-k/sas/rubis_software/",
//		"moead-d/sas/rubis_software/",
//		"moead-01/sas/rubis_software/",
		
// ====================== moead-stm ====================== 
		
// ====================== nsgaii ====================== 
		
//		"femosaa-nsgaii/sas/rubis_software/",
//		"nsgaii/sas/rubis_software/",
//		"nsgaii-k/sas/rubis_software/",
//		"nsgaii-d/sas/rubis_software/",
//		"nsgaii-01/sas/rubis_software/",
		
// ====================== nsgaii ====================== 
		
// ====================== ibea ====================== 
		
//		"femosaa-ibea/sas/rubis_software/",
//		"ibea/sas/rubis_software/",
//		"ibea-k/sas/rubis_software/",
//		"ibea-d/sas/rubis_software/",
//		"ibea-01/sas/rubis_software/",
		
// ====================== ibea ====================== 

// ====================== to state-of-the-art ====================== 	

		"moead/sas/rubis_software/",
//		"femosaa-nsgaii/sas/rubis_software/",
//		"femosaa-ibea/sas/rubis_software/",
//		"nsgaii/sas/rubis_software/",
//		"gp/sas/rubis_software/",
//		"bb/sas/rubis_software/"
		

// ====================== to state-of-the-art ====================== 		
		
// ###################### read write ######################	
		
		
// ###################### read only ######################	
		
		
// ====================== moead-stm ====================== 

//		"read/fifa-read-12-w-r/moead/sas/rubis_software/",
//		//"read/fifa-read-12-w-r/moead-k/sas/rubis_software/",
//		"read/fifa-read-12-w-r/moead-k/run1/sas/rubis_software/",
//		"read/fifa-read-12-w-r/moead-d/sas/rubis_software/",
//		"read/fifa-read-12-w-r/moead-nothing/sas/rubis_software/",
//		"read/fifa-read-12-w-r/moead-01/sas/rubis_software/",
		
// ====================== moead-stm ====================== 

		
// ====================== nsgaii ====================== 
		
//		"read/fifa-read-12-w-r/femosaa-nsgaii/sas/rubis_software/",
//		"read/fifa-read-12-w-r/nsgaii/sas/rubis_software/",
//		"read/fifa-read-12-w-r/nsgaii-k/sas/rubis_software/",
//		"read/fifa-read-12-w-r/nsgaii-d/sas/rubis_software/",
//		"read/fifa-read-12-w-r/nsgaii-01/sas/rubis_software/",
		
// ====================== nsgaii ====================== 
		
// ====================== ibea ====================== 
		
//		"read/fifa-read-12-w-r/femosaa-ibea/sas/rubis_software/",
//		"read/fifa-read-12-w-r/ibea/run1/sas/rubis_software/",
//		"read/fifa-read-12-w-r/ibea-k/sas/rubis_software/",
//		"read/fifa-read-12-w-r/ibea-d/sas/rubis_software/",
//		"read/fifa-read-12-w-r/ibea-01/sas/rubis_software/",
		
// ====================== ibea ====================== 
		
		
// ====================== to state-of-the-art ======================
		
//		"read/fifa-read-12-w-r/moead/sas/rubis_software/",	
//		
//		"read/fifa-read-12-w-r/femosaa-nsgaii/sas/rubis_software/",
//		"read/fifa-read-12-w-r/femosaa-ibea/sas/rubis_software/",
//		"read/fifa-read-12-w-r/nsgaii/sas/rubis_software/",
//		"read/fifa-read-12-w-r/gp/sas/rubis_software/",
//		"read/fifa-read-12-w-r/bb/sas/rubis_software/",
		
// ====================== to state-of-the-art ======================
		
// ###################### read only ######################
	
		

		
// ###################### DDA ######################
		
		"debt-aware/femosaa/htree-0.01-rt-0.05-3.5-ec-5-0.5/sas/rubis_software/",
		"debt-aware/femosaa/nb-0.01-rt-0.05-3.5-ec-5-0.5/sas/rubis_software/",
		"debt-aware/femosaa/svm-0.01-rt-0.05-3.5-ec-5-0.5/sas/rubis_software/",
		"debt-aware/femosaa/knn-0.01-rt-0.05-3.5-ec-5-0.5/all/sas/rubis_software/",
		"debt-aware/femosaa/mlp-0.01-rt-0.05-3.5-ec-5-0.5/all/sas/rubis_software/",
		"debt-aware/femosaa/random-10/sas/rubis_software/",
		"debt-aware/femosaa/rt-0.05-ec-5/sas/rubis_software/"
		
//		"debt-aware/plato/htree/sas/rubis_software/",
//		"debt-aware/plato/nb/sas/rubis_software/",
//		"debt-aware/plato/svm/sas/rubis_software/",
//		"debt-aware/plato/knn/sas/rubis_software/",
//		"debt-aware/plato/mlp/sas/rubis_software/",
//		"debt-aware/plato/random/sas/rubis_software/",
//		"debt-aware/plato/rt-0.05-ec-5/sas/rubis_software/"
		
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
	
	static double[] requirements = new double[]{
		0.05,
		5,
	};
	
	static double[] price = new double[]{
		3.5,
		0.5,
	};
	
	static double adaptCost = 0.01;
	
	static Map<String, Map<Integer, Double>> debt = new HashMap<String, Map<Integer, Double>>();
	
	static Map<String, Double> RTmap = new HashMap<String, Double> ();
	static Map<String, Double> Emap = new HashMap<String, Double> ();
	
	static Map<String, List<Double>> Vmap = new HashMap<String, List<Double>> ();
	
	
	static double RTmax = 0;
	static double RTmin = 100000;
	
	static double Emax = 0;
	static double Emin = 100000;
	
	static Map<String, List<Double>> surface = new HashMap<String, List<Double>> ();
	
	static Map<String, List<Double>> AdaMap = new HashMap<String, List<Double>> ();
	static Map<String, List<Double>> AdaTimeMap = new HashMap<String, List<Double>> ();
	
	
	static double[] workload = new double[]{
		38.54367256463304,
		78.00051667097227,
		49.998600039998834,
		73.49827920774206,
		106.00031666930559,
		147.99772503631885,
		125.99685007874801,
		108.49643345096833,
		154.99587094503167,
		182.99772503145786,
		224.9917087017167,
		276.49363767197497,
		512.9914501424976,
		463.48455051498286,
		384.500716672639,
		85.99910417711787,
		579.9855003624909,
		436.9913126798919,
		552.9871919699233,
		140.9967750756232,
		478.9851254685267,
		488.9845546601577,
		474.49209179846997,
		214.9924044377333,
		174.9995750035416,
		1333.4632635317416,
		813.9820920736712,
		133.4969250724983,
		149.9973167159713,
		1336.4668924892853,
		2214.418957169297,
		741.989741820067,
		428.4898377448898,
		545.9863503412414,
		590.9889293816972,
		370.4877462393962,
		265.99527925253307,
		353.4923585044405,
		399.4866837772074,
		319.49201269968245,
		224.9963958921865,
		337.4918918638841,
		324.4916627159319,
		1981.9575759418535,
		638.4862044754443,
		300.9899670010999,
		349.4958833877075,
		289.4925835247173,
		188.47460342398256,
		250.4985000124999,
		2164.945876353091,
		1185.470363240919,
		113.49716257093571,
		136.49515434051457,
		162.49624175555343,
		169.49836251739563,
		104.49660011124632,
		115.99806669888838,
		171.4959625967685,
		145.49637925690743,
		222.99450013624664,
		301.4832009405721,
		347.992021020794,
		287.99280017999547,
		271.99108362679584,
		320.4989666752777,
		475.9769594537648,
		2694.9662629691243,
		970.4793712884965,
		1102.972425689358,
		479.48801279967995,
		319.4973375221873,
		176.99928750593747,
		538.9891627269741,
		3100.9332931540634,
		1770.455738606535,
		161.99595010124744,
		575.9856003599909,
		653.4860419759652,
		4306.923276402264,
		2240.9082954321375,
		154.4972792156588,
		208.9965167247213,
		201.49664172263795,
		138.99073395106993,
		127.993600319984,
		184.99383353888203,
		174.49540012291337,
		125.99813752906202,
		1892.4526886827832,
		1858.4583134625814,
		612.483850431655,
		417.9914793475307,
		444.988875278118,
		653.9836504087398,
		759.4846669890902,
		543.4860295287058,
		161.4957209483649,
		221.49630839486002,
		950.9789671467944,
		600.980596464389,
		33.4900936202264,
			
	};
	
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
		
	    compareAdaptationStep(prefix+compare[0], prefix+compare[1], compare[0], compare[1], obj[1]);
	
		
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
	    
	    
       for (String n : compare) {
    	   int k = 0;
    	   double adapCost = 0.0;
			for(Double d : AdaMap.get(prefix+n)) {
				int i = (int)(d - 122);
				
				//System.out.print(AdaMap.get(prefix+n).size() + " " + i + " " + debt.get(n).size() + "\n");
				if(i == debt.get(n).size()) {
					break;
				}
				
				double original = debt.get(n).get(i);
				original += (30/*mean training time*/ + AdaTimeMap.get(prefix+n).get(k)/1000) * adaptCost;
				//4.0464566409062
				debt.get(n).put(i, original);
				adapCost += (30/*mean training time*/ + AdaTimeMap.get(prefix+n).get(k)/1000) * adaptCost;
				k++;
			}
			System.out.print(n + " adaptation cost: " + adapCost+"\n");
		}
       System.out.print("Debt\n\n");
       List<Double> allDebt = new ArrayList<Double>();
       Map<String, List<Double>> tempMap = new HashMap<String, List<Double>>();
	   for (String app : debt.keySet()) {
	    	System.out.print(app + "\n");
	    	tempMap.put(app, new ArrayList<Double>());
	    	double total = 0.0;
	    	for(Map.Entry<Integer, Double> e : debt.get(app).entrySet()) {
	    		tempMap.get(app).add(e.getValue());
	    		allDebt.add(e.getValue());
	    		total += e.getValue();
	    		System.out.print("(" +e.getKey() + "," + total + ")\n");
	    		//System.out.print("(" + e.getKey() + "," + (Math.log10(e.getValue()+250)) + ")\n");
	    	}
	   }
	    
	   System.out.print("Debt CDF\n\n");
	   Collections.sort(allDebt);
	   System.out.print(allDebt.size()+"\n");
	    
	    for (String app : debt.keySet()) {
	    	System.out.print(app + "\n");
	    	
	    	Collections.sort(tempMap.get(app));
	    	for (int i = 0; i <  allDebt.size(); i++) {
	    		double total = 0.0;
	    		for(Double d  : tempMap.get(app)) {
	    			total += (d <= allDebt.get(i))? (1.0/tempMap.get(app).size()) : 0;
		    		
		    		//System.out.print("(" + e.getKey() + "," + (Math.log10(e.getValue()+250)) + ")\n");
		    	}
	    		System.out.print("(" +allDebt.get(i) + "," + total + ")\n");
	    	}
	    	
	    	
	    }
	    
	    
	    String sur = "read/fifa-read-12-w-r/femosaa-ibea/sas/rubis_software/";
	    
	    List<Double> l1 = surface.get(sur+"Response Time.rtf");
	    List<Double> l2 = surface.get(sur+"Energy.rtf");
	    for(int i = 0; i < l1.size();i++) {
	    	//System.out.print(l2.get(i) + " " + i + " " + l1.get(i) + "\n");
	    }
	    
	    
	    File f = new File(prefix+sur);
	    List<Double> list = new ArrayList<Double>();
	    for(File fi : f.listFiles()){
	    	if(fi.getName().startsWith("Workload-")) {
	    		//System.out.print(fi.getName()+"\n");
	    		try {
					readWorkload(prefix+sur+fi.getName(),list);
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    }
	    
	    for(int i = 0; i < l1.size();i++) {
	    	//System.out.print(l2.get(i) + " " + (i >= workload.length? workload[workload.length-1] : workload[i]) + " " + l1.get(i) + "\n");
	      	System.out.print(l2.get(i) + " " + (i >= list.size()? list.get(list.size()-1)*60 : (list.get(i)*60))   + " " + l1.get(i) + "\n");
	 	   
	    }
	    for(int i = 0; i < l1.size();i++) {
	    	//System.out.print((list.get(i)*60) + ",\n");
	    }
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
	
	private static double readWorkload(String name,  List<Double> list) throws Throwable {
		BufferedReader reader = new BufferedReader(new FileReader(new File(name)));
		String line = null;
		double total = 0;
		int i = 0;
		int no = 0;
		
		while((line = reader.readLine()) != null) {
			if( i < 121) {
				i++;
				continue;
			} 
			
				if(list.size() <= no) {
					list.add(Double.parseDouble(line));
				} else {
					double n = list.get(no);
					//System.out.print(no + "=" +n + " : " + (n + Double.parseDouble(line)) + "\n");
					list.remove(no);
					list.add(no, (n + Double.parseDouble(line)));
				}
			
				i++;
				no++;
			
			
		}
		reader.close();
		
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
		
		double total = 1.0;
		int count = 0;
		
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
				//System.out.print( Vmap.get(a1+obj).get((int)(d + i - 122))+"\n");
				System.out.print(obj.equals("Response Time.rtf")? Math.log10(Vmap.get(a1+obj).get((int)(d + i - 122))*1000)+"\n" 
						:  Math.log10(Vmap.get(a1+obj).get((int)(d + i - 122)))+"\n");
				total *= Vmap.get(a1+obj).get((int)(d + i - 122));
				count++;
				filter.add(d + i);
			}
			
		}
		
		System.out.print("Total " + Math.pow(total, 1.0/count) + " : " + count + "\n");
		filter.clear();
		total = 1.0;
		count = 0;
		
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
				//System.out.print(Vmap.get(a2+obj).get((int)(d + i - 122))+"\n");
				System.out.print(obj.equals("Response Time.rtf")? Math.log10(Vmap.get(a2+obj).get((int)(d + i - 122))*1000) +"\n"
						:  Math.log10(Vmap.get(a2+obj).get((int)(d + i - 122)))+"\n");
				total *= Vmap.get(a2+obj).get((int)(d + i - 122));
				count++;
				filter.add(d + i);
			}
			
			
		}
	    System.out.print("Total " + Math.pow(total, 1.0/count) + " : " + count + "\n");
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
		total = 1.0;
		count = 0;
		
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
				//System.out.print( Vmap.get(a1+obj).get((int)(d + i - 122))+"\n");
				System.out.print(obj.equals("Response Time.rtf")? Math.log10(Vmap.get(a1+obj).get((int)(d + i - 122))*1000) +"\n"
						:  Math.log10(Vmap.get(a1+obj).get((int)(d + i - 122)))+"\n");
				total *= Vmap.get(a1+obj).get((int)(d + i - 122));
				count++;
				filter.add(d + i);
			}
			
		}
		
		System.out.print("Total " + Math.pow(total, 1.0/count) + " : " + count + "\n");
		filter.clear();
		total = 1.0;
		count = 0;
		
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
				
				if (Vmap.get(a2+obj).size() <= (int)(d + i - 122)) {
					continue;
				}
				
				//System.out.print( Vmap.get(a2+obj).get((int)(d + i - 122))+"\n");
				System.out.print(obj.equals("Response Time.rtf")? Math.log10(Vmap.get(a2+obj).get((int)(d + i - 122))*1000) +"\n"
						:  Math.log10(Vmap.get(a2+obj).get((int)(d + i - 122)))+"\n");
				total *= Vmap.get(a2+obj).get((int)(d + i - 122));
				count++;
				filter.add(d + i);
			}
			
			
		}
	    
	    System.out.print("Total " + Math.pow(total, 1.0/count) + " : " + count + "\n");
		
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
		
		if(!AdaTimeMap.containsKey(name)) {
			AdaTimeMap.put(name, list);
		}
		
		while((line = reader.readLine()) != null) {
			if(i < startIndex) {
				i++;
				continue;
			} 
			//if(Double.parseDouble(line) != 0) {
				total += Double.parseDouble(line);
				list.add(Double.parseDouble(line));
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
				
				double d = (obj.equals("Response Time.rtf")? price[0] : price[1]) * (Double.parseDouble(line) - (obj.equals("Response Time.rtf")? requirements[0] : requirements[1]));
				
				if(!debt.containsKey(approach)) {
					debt.put(approach, new LinkedHashMap<Integer, Double>());
				}
				
				if(!debt.get(approach).containsKey(i)) {
					debt.get(approach).put(i, d);
				} else {
					d = debt.get(approach).get(i) + d;
					debt.get(approach).put(i, d);
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
		
		if(obj.equals("Energy.rtf")) 
		log(list.toArray(new Double[list.size()]));
		htotal = no / htotal;
		
		
		if(!surface.containsKey(approach+obj)) {
			surface.put(approach+obj, list);
		}
		
		
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
