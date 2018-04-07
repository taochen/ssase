package org.soa;

import java.util.ArrayList;
import java.util.List;

public class AbstractService {

	// Only one if there is no parallel
	private List<AbstractService> next;
	// Avoid doing 'next' more than once
	private List<AbstractService> otherParallelOrConditional;

	private Connector connector;

	// This can be multiple, duplicated CS
	private List<ConcreteService> selected  = new ArrayList<ConcreteService>();
	private List<ConcreteService> options;

	
	public List<ConcreteService>  getOption(){
		return options;
	}
	
	public List<Double> getObjectiveValueForEachOption(int i) {
		List<Double> list = new ArrayList<Double> ();
		for (ConcreteService cs : options) {
			list.add(cs.getObjectiveValues()[i]);
		}
		return list;
	}
	
	public AbstractService (List<ConcreteService> options){
		this.options = options;
	}
	
	public void addConnector(Connector connector){
		
	}
	
	public void addAbstractService(AbstractService as){
		if(next == null) {
			next = new ArrayList<AbstractService>();
		}
		next.add(as);
	}
	
	public void addOtherAbstractService(AbstractService as){
		if(otherParallelOrConditional == null) {
			 otherParallelOrConditional = new ArrayList<AbstractService>();
		}
		otherParallelOrConditional.add(as);
	}
	
	
	public void resortSelected(double[] xValue) {
        /*build select depends on the xValue*/
		
		selected.clear();
		for (int i = 0; i < Workflow.map.get(this).length; i++) {
			double no = xValue[Workflow.map.get(this)[i]];
			//System.out.print(xValue[Workflow.map.get(this)[i]] + " : " + Workflow.map.get(this)[i]+ " : " + no + "\n");
			for (int k = 0; k < no; k++) {				
				selected.add(Workflow.cs_map.get(this)[i]);
			}
			
			
		}
		
		
	    
		
		/*build select depends on the xValue*/
	}
	
	public double getObjectiveValuesPort(int i) {

	
		return getObjectiveValues(i);
	}

	public double getObjectiveValues(int i) {
		double v = 0.0;
		if ("Throughput".equals(ConcreteService.objectives[i])) {
			v = Double.MAX_VALUE;
			v = v > getOwnObjectiveValues(i) ? getOwnObjectiveValues(i) : v;

			if (otherParallelOrConditional == null
					|| (otherParallelOrConditional != null && otherParallelOrConditional
							.get(0).equals(this))) {

				if (Connector.SEQUENTIAL.equals(connector) && next != null) {
					v = v > next.get(0).getObjectiveValues(i) ? next.get(0)
							.getObjectiveValues(i) : v;

				} else if (Connector.PARALLEL.equals(connector) && next != null) {

					for (AbstractService as : next) {
						v = v > as.getObjectiveValues(i) ? as
								.getObjectiveValues(i) : v;
					}
				}

			}

		} else if ("Cost".equals(ConcreteService.objectives[i])) {
			v += getOwnObjectiveValues(i);

			if (otherParallelOrConditional == null
					|| (otherParallelOrConditional != null && otherParallelOrConditional
							.get(0).equals(this))) {

				if (next != null) {

					for (AbstractService as : next) {
						v += as.getObjectiveValues(i);
					}

				}

			}
		}

		return v;

	}

	public double getOwnObjectiveValues(int i) {

		double v = 0.0;
		if ("Throughput".equals(ConcreteService.objectives[i])
				|| "Cost".equals(ConcreteService.objectives[i])) {

			for (ConcreteService cs : selected) {
				v += cs.getObjectiveValues()[i];
			}

		}

		return v;

	}
}
