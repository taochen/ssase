package org.ssase.objective.optimization.random;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.ssase.primitive.ControlPrimitive;

public class HillClimbingwithDRegion extends HillClimbingRegion{
	
	protected double[] getValueVector(ControlPrimitive cp, int k, List<ControlPrimitive> list, LinkedHashMap<ControlPrimitive, Double> current){
		
		if(cp.getName().equals("Connection")) {
			for (int i = 0 ; i < k; i++) {
				if(list.get(i).getName().equals("maxThread")){
					double threshold = current.get(list.get(i));
					
					List<Double> rList = new ArrayList<Double>();
					for (double d : cp.getValueVector()) {
						if(d <= threshold) {
							rList.add(d);
						} else {
							break;
						}
					}
					
					double[] result = new double[rList.size()];
					for (int j = 0 ; j < rList.size(); j++) {
						result[j] = rList.get(j);
					}
					
					return result;
				}
			}
			
			
		}
		
		if(cp.getName().equals("maxThread")) {
			for (int i = 0 ; i < k; i++) {
				if(list.get(i).getName().equals("minSpareThreads")){
					double threshold = current.get(list.get(i));
					
					List<Double> rList = new ArrayList<Double>();
					for (double d : cp.getValueVector()) {
						if(d >= threshold) {
							rList.add(d);
						} else {
							break;
						}
					}
					
					double[] result = new double[rList.size()];
					for (int j = 0 ; j < rList.size(); j++) {
						result[j] = rList.get(j);
					}
					
					return result;
				}
			}
			
			
		}
		
		if(cp.getName().equals("query_cache_size")) {
			for (int i = 0 ; i < k; i++) {
				if(list.get(i).getName().equals("Memory")){
					double threshold = current.get(list.get(i));
					
					List<Double> rList = new ArrayList<Double>();
					for (double d : cp.getValueVector()) {
						if((d/1048576) < threshold) {
							rList.add(d);
						} else {
							break;
						}
					}
					
					double[] result = new double[rList.size()];
					for (int j = 0 ; j < rList.size(); j++) {
						result[j] = rList.get(j);
					}
					
					return result;
				}
			}
			
			
		}
		
		if(cp.getName().equals("cacheMode")) {
			for (int i = 0 ; i < k; i++) {
				if(list.get(i).getName().equals("Compression") && current.get(list.get(i)) == 1.0) {
					return new double[]{0.0, 2.0};
				}
				
			}
		}
		
		if(cp.getName().equals("maxBytesLocalHeap")) {
			
			for (int i = 0 ; i < k; i++) {
				if(list.get(i).getName().equals("cacheMode") && current.get(list.get(i)) == 0.0) {
					return new double[]{0.0};
				}
				
				if(list.get(i).getName().equals("maxBytesLocalDisk") && current.get(list.get(i)) == 0.0) {
					double[] d = new double[cp.getValueVector().length - 1];
					System.arraycopy(cp.getValueVector(), 1, d, 0, d.length);
					return d;
				}
			}
			
		}
		
	    if(cp.getName().equals("maxBytesLocalDisk")) {
			
			for (int i = 0 ; i < k; i++) {
				if(list.get(i).getName().equals("cacheMode") && current.get(list.get(i)) == 0.0) {
					return new double[]{0.0};
				}
				
				if(list.get(i).getName().equals("maxBytesLocalHeap") && current.get(list.get(i)) == 0.0) {
					double[] d = new double[cp.getValueVector().length - 1];
					System.arraycopy(cp.getValueVector(), 1, d, 0, d.length);
					return d;
				}
			}
			
		}
		
		return cp.getValueVector();
	}
}
