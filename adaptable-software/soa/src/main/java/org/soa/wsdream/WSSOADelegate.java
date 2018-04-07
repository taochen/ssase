package org.soa.wsdream;

import org.ssase.model.Delegate;


public class WSSOADelegate implements Delegate{

	private WSWorkflow workflow;
	
	private int index;
	
	public  WSSOADelegate (int index, WSWorkflow workflow) {
		this.index = index;
		this.workflow = workflow;
	}
	
	public double predict(double[] xValue) {
		double v = workflow.getObjectiveValues(index, xValue);
		
		if(index == 1 && v == 0) {
			String o = "";
			for (double d : xValue) {
				o += d + ", ";
			}
			System.out.print("*** Zero " + o + "\n");
			//throw new RuntimeException("this is error");
			
		}
		

		if(Double.isInfinite(v)) {
			String o = "";
			for (double d : xValue) {
				o += d + ", ";
			}
			System.out.print("*** Infinity " + o + "\n");
		}
		
		// 1 is throughput
		return index == 1? (v == 0? Double.MAX_VALUE : (1/v)*100) : v*100;
	}
}
