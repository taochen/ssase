package org.ssascaling.util.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ssascaling.objective.optimization.Ant;



public class ACOConfiguration {

	private static Map<String, Config> map = new HashMap<String, Config>();
	
	private static List<List<Ant>> list = new ArrayList<List<Ant>>();
	private static List<String> names = new ArrayList<String>();
	public static void put(String uuid, double VALUE_EVAPORATION, double ALPHA,
			double BETA, boolean isMakeDifferentTrail, boolean isUseGlobalBest, boolean isLocalUpdate){
		map.put(uuid, new ACOConfiguration().new Config( VALUE_EVAPORATION,  ALPHA,
			 BETA,  isMakeDifferentTrail,  isUseGlobalBest, isLocalUpdate));
	}
	
	public static void add (int no, String name, Ant ant) {
		if (no != list.size()) {
			list.add(new ArrayList<Ant>());
		} 
		list.get(no-1).add(ant);
		
		
		if (names.size() < 2) {
		  names.add(name);
		}
	}
	
	public static double getVALUE_EVAPORATION(String name){
		return map.get(name).VALUE_EVAPORATION;
	}
	
	public static double getALPHA(String name){
		return map.get(name).ALPHA;
	}
	
	public static double getBETA(String name){
		return map.get(name).BETA;
	}
	
	public static boolean isMakeDifferentTrail(String name){
		return map.get(name).isMakeDifferentTrail;
	}
	
	public static boolean isUseGlobalBest(String name){
		return map.get(name).isUseGlobalBest;
	}
	
	public static boolean isLocalUpdate(String name){
		return map.get(name).isLocalUpdate;
	}
	
	public class Config {
		
		
		
		public Config(double vALUE_EVAPORATION, double aLPHA, double bETA,
				boolean isMakeDifferentTrail, boolean isUseGlobalBest, boolean isLocalUpdate) {
			super();
			VALUE_EVAPORATION = vALUE_EVAPORATION;
			ALPHA = aLPHA;
			BETA = bETA;
			this.isMakeDifferentTrail = isMakeDifferentTrail;
			this.isUseGlobalBest = isUseGlobalBest;
			this.isLocalUpdate = isLocalUpdate;
		}

		public double VALUE_EVAPORATION = 0.1;

		// Weight for mu
		public double ALPHA = 1;

		// Weight for tau
		public double BETA = 3;
		
		public boolean isMakeDifferentTrail = false;
		
		public boolean isUseGlobalBest = false;
		
		public boolean isLocalUpdate = false;
	}

	public static void print() {
		int no = 0;
		for (List<Ant> sub : list) {
			no++;
			System.out.print("----------" + no + "-----------\n");
			for (int i = 0; i < sub.size(); i++) {
				for (int j = i + 1; j < sub.size(); j++) {
					printCMeasure(names.get(i), names.get(j), sub.get(i),
							sub.get(j));
				}
			}

		}
	}
	
	private static void printCMeasure(String a, String b, Ant antA, Ant antB){
		int[] r = antA.c_measure(antB);
		String out = null;
		if(r[0] > r[1]) {
			out = ", " + a +" is better";
		} else if(r[0] < r[1]) {
			out = ", " + b +" is better";
		} else {
			out = ", they are equal";
		}
		System.out.print("C-measure:" + a + " to " + b + " is " + r[0] + ", " + b + " to " + a + " is " + r[1] + out +  "\n");
	}
}
