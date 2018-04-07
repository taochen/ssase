package org.soa.wsdream;

import java.util.ArrayList;
import java.util.List;

import org.soa.Connector;


public class WSAbstractService{

	// Only one if there is no parallel
	private List<WSAbstractService> next;
	// Avoid doing 'next' more than once
	private List<WSAbstractService> otherParallelOrConditional;

	private Connector connector;

	// This can be multiple, duplicated CS
	private WSConcreteService selected;
	private List<WSConcreteService> options;

	
	public List<WSConcreteService>  getOption(){
		return options;
	}
	
	public List<Double> getObjectiveValueForEachOption(int i) {
		List<Double> list = new ArrayList<Double> ();
		for (WSConcreteService cs : options) {
			list.add(cs.getObjectiveValues()[i]);
		}
		return list;
	}
	
	public WSAbstractService (List<WSConcreteService> options){
		this.options = options;
	}
	
	public void addConnector(Connector connector){
		
	}
	
	public void addAbstractService(WSAbstractService as){
		if(next == null) {
			next = new ArrayList<WSAbstractService>();
		}
		next.add(as);
	}
	
	public void addOtherAbstractService(List<WSAbstractService> otherParallelOrConditional){
		this.otherParallelOrConditional =  otherParallelOrConditional;
	}
	
	
	public void resortSelected(double[] xValue) {
        /*build select depends on the xValue*/
		
		selected = null;
		int i =  WSWorkflow.map.get(this);
		double index = xValue[i];
		// We need to minus one here as the xValue is 1 to size rather than starting from 0
		selected = options.get((int)(index - 1));
		
		
		
	    
		
		/*build select depends on the xValue*/
	}
	
	public double getObjectiveValuesPort(int i) {
		return getObjectiveValues(i);
	}

	public double getObjectiveValues(int i) { 
		double[] array = getInternalObjectiveValues(i);
		if ("Latency".equals(WSConcreteService.objectives[i]) && array[1] > 0) {
			return array[0] + array[1];
		} else {
			return array[0];
		}
	}
	
	private double[] getInternalObjectiveValues(int i) {
		double v = 0.0;
		double after = -1.0;
		if ("Throughput".equals(WSConcreteService.objectives[i])) {
			v = Double.MAX_VALUE;
			v = v > getOwnObjectiveValues(i) ? getOwnObjectiveValues(i) : v;

			if (otherParallelOrConditional == null
					|| (otherParallelOrConditional != null && otherParallelOrConditional
							.get(0).equals(this))) {

				
				if (Connector.SEQUENTIAL.equals(connector) && next != null) {
					v = v > next.get(0).getObjectiveValues(i) ? next.get(0)
							.getObjectiveValues(i) : v;

				} else if (Connector.PARALLEL.equals(connector) && next != null) {

					for (WSAbstractService as : next) {
						v = v > as.getObjectiveValues(i) ? as
								.getObjectiveValues(i) : v;
					}
				}

			}

		} else if ("Cost".equals(WSConcreteService.objectives[i])) {
			v += getOwnObjectiveValues(i);

			if (otherParallelOrConditional == null
					|| (otherParallelOrConditional != null && otherParallelOrConditional
							.get(0).equals(this))) {

				if (next != null) {

					for (WSAbstractService as : next) {
						v += as.getObjectiveValues(i);
					}

				}

			}
		} else if ("Latency".equals(WSConcreteService.objectives[i])) {
			v += getOwnObjectiveValues(i);

			if (otherParallelOrConditional == null
					|| (otherParallelOrConditional != null && otherParallelOrConditional
							.get(0).equals(this))) {

				if (Connector.SEQUENTIAL.equals(connector) && next != null) {
					after += next.get(0).getObjectiveValues(i);

				} else if (Connector.PARALLEL.equals(connector) && next != null) {

					double temp = Double.MIN_VALUE;
					for (WSAbstractService as : next) {
						double[] array = as.getInternalObjectiveValues(i);
						temp = temp < array[0] ? array[0] : temp;
						if(array[1] > 0) {
							after += array[1];
						}
					}
					
					after += temp;
					//v += after + temp;
				}

			}

		}

		return new double[] {v,after};

	}

	public double getOwnObjectiveValues(int i) {
		return selected.getObjectiveValues()[i];

	}

}
