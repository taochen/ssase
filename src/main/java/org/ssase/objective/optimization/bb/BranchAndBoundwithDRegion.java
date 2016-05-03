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
			for (int i = 0 ; i < node.index; i++) {
				if(list.get(i).getName().equals("Compression") && node.decision[i] == 1.0) {
					return new double[]{0.0, 2.0};
				}
				
			}
		}
		
		if(list.get(node.index).getName().equals("maxBytesLocalHeap")) {
			
			for (int i = 0 ; i < node.index; i++) {
				if(list.get(i).getName().equals("cacheMode") && node.decision[i] == 0.0) {
					return new double[]{0.0};
				}
				
				if(list.get(i).getName().equals("maxBytesLocalDisk") && node.decision[i]  == 0.0) {
					double[] d = new double[list.get(node.index).getValueVector().length - 1];
					System.arraycopy(list.get(node.index).getValueVector(), 1, d, 0, d.length);
					return d;
				}
			}
			
		}
		
	    if(list.get(node.index).getName().equals("maxBytesLocalDisk")) {
			
			for (int i = 0 ; i < node.index; i++) {
				if(list.get(i).getName().equals("cacheMode") && node.decision[i] == 0.0) {
					return new double[]{0.0};
				}
				
				if(list.get(i).getName().equals("maxBytesLocalHeap") && node.decision[i] == 0.0) {
					double[] d = new double[list.get(node.index).getValueVector().length - 1];
					System.arraycopy(list.get(node.index).getValueVector(), 1, d, 0, d.length);
					return d;
				}
			}
			
		}
		
		return list.get(node.index).getValueVector();
	}
}
