package org.ssase.objective.optimization.bb;

import java.util.ArrayList;
import java.util.List;

import org.ssase.objective.optimization.bb.BranchAndBoundRegion.Node;
import org.ssase.primitive.ControlPrimitive;

public class BranchAndBoundwithDRegion extends BranchAndBoundRegion{
	protected double[] getValueVector(Node node, List<ControlPrimitive> list){
		
		if(list.get(node.index).getName().equals("Connection")) {
			for (int i = 0 ; i < node.index; i++) {
				if(list.get(i).getName().equals("maxThread")){
					double threshold = node.decision[i];
					
					List<Double> rList = new ArrayList<Double>();
					for (double d : list.get(node.index).getValueVector()) {
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
		
		if(list.get(node.index).getName().equals("maxThread")) {
			for (int i = 0 ; i < node.index; i++) {
				if(list.get(i).getName().equals("minSpareThreads")){
					double threshold = node.decision[i];
					
					List<Double> rList = new ArrayList<Double>();
					for (double d : list.get(node.index).getValueVector()) {
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
		
		if(list.get(node.index).getName().equals("query_cache_size")) {
			for (int i = 0 ; i < node.index; i++) {
				if(list.get(i).getName().equals("Memory")){
					double threshold = node.decision[i];
					
					List<Double> rList = new ArrayList<Double>();
					for (double d : list.get(node.index).getValueVector()) {
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
		
		if(list.get(node.index).getName().equals("cacheMode")) {
			double[] r = null; 
			for (int i = 0 ; i < node.index; i++) {
				

				if(list.get(i).getName().equals("cache") && node.decision[i] == 0.0) {
					return new double[]{0.0};
				}
				
				if(list.get(i).getName().equals("Compression") && node.decision[i] == 1.0) {
					if(r != null) {
						return new double[]{0.0}; 
					}
					r = new double[]{0.0, 2.0};
				}
//				
//				if(list.get(i).getName().equals("maxBytesLocalDisk") && node.decision[i]  != 0.0 && node.decision[i] < 13631488) {
//					
//					if(r != null && r[1] == 2.0) {
//						return new double[]{0.0}; 
//					} else {;
//					   r = new double[]{0.0, 1.0};
//					}
//				}
//				
//				if(list.get(i).getName().equals("maxBytesLocalHeap") && node.decision[i]  != 0.0 && node.decision[i] < 13631488) {
//					if(r != null && r[1] == 2.0) {
//						return new double[]{0.0}; 
//					} else {;
//					   r = new double[]{0.0, 1.0};
//					}
//				}
				
				if(r !=null) {
					return r;
				}
			}
		}
		
		if(list.get(node.index).getName().equals("maxBytesLocalHeap")) {
			double[] r = null; 
			for (int i = 0 ; i < node.index; i++) {
				Double[] pre = null;
				if(list.get(i).getName().equals("cacheMode") && node.decision[i] == 0.0) {
					return new double[]{0.0};
				}
				
				if(list.get(i).getName().equals("cacheMode") && node.decision[i] == 2.0) {
					List<Double> l = new ArrayList<Double>();
					l.add(0d);
					for (int j = 0 ; j < list.get(node.index).getValueVector().length; j++) {
						if(list.get(node.index).getValueVector()[j] > 13631488) {
							l.add(list.get(node.index).getValueVector()[j]);
						} 
					}
					
					pre = l.toArray(new Double[l.size()]);
				}
				
				if(list.get(i).getName().equals("maxBytesLocalDisk") && node.decision[i]  == 0.0) {
					if(pre != null) {
						double[] d = new double[pre.length-1];
						for (int j = 1 ; j < pre.length; j++) {
							d[j-1] = pre[j]; 
						}
						
						r = d;
					} else {
					double[] d = new double[list.get(node.index).getValueVector().length - 1];
					System.arraycopy(list.get(node.index).getValueVector(), 1, d, 0, d.length);
					r = d;
					}
				}
			}
			
			if(r != null) {
				return r;
			}
			
			
		}
		
	    if(list.get(node.index).getName().equals("maxBytesLocalDisk")) {
	    	double[] r = null; 
			for (int i = 0 ; i < node.index; i++) {
				Double[] pre = null;
				if(list.get(i).getName().equals("cacheMode") && node.decision[i] == 0.0) {
					return new double[]{0.0};
				}
				
				if(list.get(i).getName().equals("cacheMode") && node.decision[i] == 2.0) {
					List<Double> l = new ArrayList<Double>();
					l.add(0d);
					for (int j = 0 ; j < list.get(node.index).getValueVector().length; j++) {
						if(list.get(node.index).getValueVector()[j] > 13631488) {
							l.add(list.get(node.index).getValueVector()[j]);
						} 
					}
					
					pre = l.toArray(new Double[l.size()]);
				}
				
				if(list.get(i).getName().equals("maxBytesLocalHeap") && node.decision[i]  == 0.0) {
					if(pre != null) {
						double[] d = new double[pre.length-1];
						for (int j = 1 ; j < pre.length; j++) {
							d[j-1] = pre[j]; 
						}
						
						r = d;
					} else {
					double[] d = new double[list.get(node.index).getValueVector().length - 1];
					System.arraycopy(list.get(node.index).getValueVector(), 1, d, 0, d.length);
					r = d;
					}
				}
			}
			
			if(r != null) {
				return r;
			}
			
		}
		
		return list.get(node.index).getValueVector();
	}
}
