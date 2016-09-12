package org.ssase.model.iapm;

import java.util.Map;

public class SampleCluster {

	// the old one
	private int numClasses;
	
	// Following are new ones
	private  Map<Double, Integer> map;
	private int instancesNo;
	
	private double[] newClassCount;
	
	
	
	public SampleCluster(int numClasses, Map<Double, Integer> map,
			int instancesNo, double[] newClassCount) {
		super();
		this.numClasses = numClasses;
		this.map = map;
		this.instancesNo = instancesNo;
		this.newClassCount = newClassCount;
	}

	public int convertValueToClassIndex(double value) {
		return map.get(value);
	}
	
	public int getInstancesNo(){
		return instancesNo;
	}
	
	public int getNumClasses(){
		return numClasses;
	}
	
	public double[] getNewClassCount(){
		return newClassCount;
	}
}
