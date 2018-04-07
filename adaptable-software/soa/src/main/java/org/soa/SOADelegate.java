package org.soa;

import org.ssase.model.Delegate;

public class SOADelegate implements Delegate{

	
	private Workflow workflow;
	
	private int index;
	
	public  SOADelegate (int index, Workflow workflow) {
		this.index = index;
		this.workflow = workflow;
	}
	
	public double predict(double[] xValue) {
		double v = workflow.getObjectiveValues(index, xValue);
		
		if(index == 0 && v == 0) {
			String o = "";
			for (double d : xValue) {
				o += d + ", ";
			}
			System.out.print("*** Zero " + o + "\n");
			//throw new RuntimeException("this is error");
			
		}
		
		// 0 is throughput
		return index == 0? (v == 0? Double.MAX_VALUE : (1/v)*100) : v*100;
	}
}
