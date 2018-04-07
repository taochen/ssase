package org.soa.wsdream;

public class WSConcreteService {
	
	public static String[] objectives={"Latency","Throughput","Cost"};
	
	private double[] objectiveValues;
	public WSConcreteService(double latency, double throughput, double cost) {
		super();
		objectiveValues = new double[]{latency,throughput,cost};
	}
	
	public double getLatency() {
		return objectiveValues[0];
	}
	public double getThroughput() {
		return objectiveValues[1];
	}
	public double getCost() {
		return objectiveValues[2];
	}
	
	public double[] getObjectiveValues() {
		return objectiveValues;
	}
	
	
	

}
