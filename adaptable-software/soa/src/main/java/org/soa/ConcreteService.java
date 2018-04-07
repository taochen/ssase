package org.soa;

public class ConcreteService{

	public static String[] objectives={"Throughput","Cost"};
	private int throughput_counter;
	private int cost_counter;
	private int as;
	private double[] objectiveValues;
	
	private String name;
	public ConcreteService(String name, double[] objectiveValues, int throughput_counter, int cost_counter) {
		super();
		this.name = name;
		this.throughput_counter = throughput_counter;
		this.cost_counter = cost_counter;
		this.objectiveValues = objectiveValues;
	}
	
	

	public ConcreteService(double[] objectiveValues,  int as, int throughput_counter, int cost_counter) {
		super();
		this.throughput_counter = throughput_counter;
		this.cost_counter = cost_counter;
		this.as = as;
		this.objectiveValues = objectiveValues;
	}



	public double[] getObjectiveValues() {
		return objectiveValues;
	}
	
	public void change(){
		if(throughput_counter == cost_counter) {
			throughput_counter++;
			if(Workflow.throughput_value[as].length <= throughput_counter) {
				throughput_counter = 0;
			}
			double[] d = new double[]{Workflow.throughput_value[as][throughput_counter], objectiveValues[1]};
			objectiveValues = d;
		} else {
			cost_counter++;
			if(Workflow.cost_value[as].length <= cost_counter) {
				cost_counter = 0;
			}
			double[] d = new double[]{objectiveValues[0], Workflow.cost_value[as][cost_counter]};
			objectiveValues = d;
		}
	}
	
	public ConcreteService clone(){
		
		
		ConcreteService cs = new ConcreteService(objectiveValues.clone(), as, throughput_counter, cost_counter);
		return cs;
	}
}
