package org.ssascaling.region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ssascaling.objective.Objective;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.primitive.EnvironmentalPrimitive;

public class NaiveRegion extends Region {

	protected NaiveRegion () {
		this.objectives = new ArrayList<Objective>();
	}

	/**
	 * Using a random or random hill-climbing algorithm based on equal weight-summed approach. 
	 */
	public LinkedHashMap<ControlPrimitive, Double> optimize() {

		synchronized (lock) {
			while (waitingUpdateCounter != 0) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

			isLocked = true;
		

		// TODO reduction 
		
		// TODO initilize AntColony
			
		//TODO add listener.

		
			isLocked = false;
			lock.notifyAll();
		}
		
		return randomHillClimbing();
	}
	
	private LinkedHashMap<ControlPrimitive, Double> randomHillClimbing(){
		// Random optimization.
		long interation = 5;
		long count = 0;
		
		//double bestValue = 0;
		//LinkedHashMap<ControlPrimitive, Double> best = new LinkedHashMap<ControlPrimitive, Double>();
		LinkedHashMap<ControlPrimitive, Double> current = new LinkedHashMap<ControlPrimitive, Double>();
		LinkedHashMap<ControlPrimitive, Double> copy = new LinkedHashMap<ControlPrimitive, Double>();
		List<ControlPrimitive> list = new ArrayList<ControlPrimitive>();
		
	
		LinkedHashMap<Double[], LinkedHashMap<ControlPrimitive, Double>> results = new LinkedHashMap<Double[], LinkedHashMap<ControlPrimitive, Double>>();
		
		Map<Objective, Double> map = getBasis();
		
		for (Objective obj : objectives) {
			for(Primitive p : obj.getPrimitivesInput()){
				if (p instanceof ControlPrimitive) {
					current.put((ControlPrimitive)p, (double)p.getProvision());
					copy.put((ControlPrimitive)p, (double)p.getProvision());
					list.add((ControlPrimitive)p);
				}
			}
		}
		
		
		//Random random = new Random();
		
		do {
			//System.out.print("Run " + count + "\n");
			count ++;
			current.clear();
			current.putAll(copy);
			double localBestValue = doWeightSum(current, map)[0];
			Collections.shuffle(list);
			
			for (ControlPrimitive cp : list) {
			
				for (double d : cp.getValueVector()) {
					double temp = current.get(cp);
					current.put(cp, d);
					if (localBestValue < doWeightSum(current, map)[0]) {
						localBestValue = doWeightSum(current, map)[0];
						//break;
					} else {
						current.put(cp, temp);
					}
					
				}
				
			}
			
			Double[] result = null;
			
			try {
			
				// Assume equal weight on all objectives
			   result = doWeightSum(current, map);
			} catch (RuntimeException re) {
				//re.printStackTrace();
				//System.out.print( "Not satisfied\n");
				continue;
			}
			
			//System.out.print("current: " + result + ", best: " + bestValue + "\n");
			/*if (result > bestValue || count == 1) {
				//System.out.print("Find a new best decision!\n");
				best.clear();
				best.putAll(current);
				bestValue = result;
			}*/
			
			results.put(result, copy(current));
			
		} while (count<interation);
		
		
		/*for (Map.Entry<ControlPrimitive, Double> entry : current.entrySet()) {
			System.out.print(entry.getKey().getAlias() + "-" + entry.getKey().getName() + " : " + entry.getValue() + "\n");
		}*/
		
		//System.out.print("Final result is: " + bestValue + "\n");
		
		LinkedHashMap<ControlPrimitive, Double> satisfied = null;
		LinkedHashMap<ControlPrimitive, Double> unsatisfied = null;
		double satisfiedValue = -1;
		double unsatisfiedValue = -1;
		for (Double[] d : results.keySet()) {
			
			if (d[1] == 1) {
				
				if (satisfiedValue == -1 || d[0] > satisfiedValue) {
					satisfied = results.get(d);
					satisfiedValue = d[0];
				}
				
				
			} else {
				
				if (unsatisfiedValue == -1 || d[0] > unsatisfiedValue) {
					unsatisfied = results.get(d);
					unsatisfiedValue = d[0];
				}
				
			}
		}
		
		if (satisfied == null) {
			System.out.print("We have satisfied solution!\n");
		} else {
			System.out.print("We do not have satisfied solution\n");
		}
		results.clear();
		return satisfied == null? unsatisfied : satisfied;
	}
	
	private LinkedHashMap<ControlPrimitive, Double> random(){
		// Random optimization.
		long interation = 5000;
		long count = 0;
		
		//double bestValue = 0;
		//LinkedHashMap<ControlPrimitive, Double> best = new LinkedHashMap<ControlPrimitive, Double>();
		LinkedHashMap<ControlPrimitive, Double> current = new LinkedHashMap<ControlPrimitive, Double>();
		
		LinkedHashMap<Double[], LinkedHashMap<ControlPrimitive, Double>> results = new LinkedHashMap<Double[], LinkedHashMap<ControlPrimitive, Double>>();
		
		
		Map<Objective, Double> map = getBasis();
		
		for (Objective obj : objectives) {
			for(Primitive p : obj.getPrimitivesInput()){
				if (p instanceof ControlPrimitive) {
					current.put((ControlPrimitive)p, (double)p.getProvision());
				}
			}
		}
		
		Random random = new Random();
		
		do {
			count ++;
			for (Map.Entry<ControlPrimitive, Double> entry : current.entrySet()) {
			
				entry.setValue(entry.getKey().getValueVector()[
						random.nextInt(entry.getKey().getValueVector().length)]);
			}
			

			Double[] result = null;
			
			try {
			
				// Assume equal weight on all objectives
			   result = doWeightSum(current, map);
			} catch (RuntimeException re) {
				//re.printStackTrace();
				//System.out.print( "Not satisfied\n");
				continue;
			}
			
			//System.out.print("current: " + result + ", best: " + bestValue + "\n");
			/*if (result > bestValue || count == 1) {
				//System.out.print("Find a new best decision!\n");
				best.clear();
				best.putAll(current);
				bestValue = result;
			}*/
			
			results.put(result, copy(current));
			
		} while (count<interation);
		
		
		/*for (Map.Entry<ControlPrimitive, Double> entry : current.entrySet()) {
			System.out.print(entry.getKey().getAlias() + "-" + entry.getKey().getName() + " : " + entry.getValue() + "\n");
		}*/
		
		//System.out.print("Final result is: " + bestValue + "\n");
		LinkedHashMap<ControlPrimitive, Double> satisfied = null;
		LinkedHashMap<ControlPrimitive, Double> unsatisfied = null;
		double satisfiedValue = -1;
		double unsatisfiedValue = -1;
		for (Double[] d : results.keySet()) {
			
			if (d[1] == 1) {
				
				if (satisfiedValue == -1 || d[0] > satisfiedValue) {
					satisfied = results.get(d);
					satisfiedValue = d[0];
				}
				
				
			} else {
				
				if (unsatisfiedValue == -1 || d[0] > unsatisfiedValue) {
					unsatisfied = results.get(d);
					unsatisfiedValue = d[0];
				}
				
			}
		}
		
		if (satisfied == null) {
			System.out.print("We have satisfied solution!\n");
		} else {
			System.out.print("We do not have satisfied solution\n");
		}
		results.clear();
		return satisfied == null? unsatisfied : satisfied;
	}
	
	private LinkedHashMap<ControlPrimitive, Double> copy (LinkedHashMap<ControlPrimitive, Double> current) {
		LinkedHashMap<ControlPrimitive, Double> result = new LinkedHashMap<ControlPrimitive, Double>();
		for (ControlPrimitive p : current.keySet()) {
			result.put(p, current.get(p));
		}
		
		return result;
	}
	
	
	private Double[] doWeightSum(LinkedHashMap<ControlPrimitive, Double> decision, Map<Objective, Double> map ) throws RuntimeException{
		double result = 0;
		double satisfied = 1;
		double[] xValue;
		for (Objective obj : objectives) {
			xValue = new double[ obj.getPrimitivesInput().size()];
			//System.out.print(obj.getPrimitivesInput().size()+"\n");
			for(int i = 0; i < obj.getPrimitivesInput().size();i++){
				if (obj.getPrimitivesInput().get(i) instanceof ControlPrimitive) {
					xValue[i] = decision.get(obj.getPrimitivesInput().get(i));
				} else {
					xValue[i] = ((EnvironmentalPrimitive)obj.getPrimitivesInput().get(i)).getLatest();
				}
			}
			
			if (!obj.isSatisfied(xValue)) {
				//System.out.print(obj.getName() + " is not satisfied " + obj.predict(xValue) + "\n");
				//throw new RuntimeException();
				satisfied = -1;
			}
			
			result = obj.isMin()? result - (obj.predict(xValue)/(1+map.get(obj))) : result + (obj.predict(xValue)/(1+map.get(obj))) ;
		}
		
		return new Double[]{result, satisfied};
	}
	
	private Map<Objective, Double> getBasis() throws RuntimeException{
		Map<Objective, Double> map = new HashMap<Objective, Double>();
		double[] xValue;
		for (Objective obj : objectives) {
			xValue = new double[ obj.getPrimitivesInput().size()];
			//System.out.print(obj.getPrimitivesInput().size()+"\n");
			for(int i = 0; i < obj.getPrimitivesInput().size();i++){
				if (obj.getPrimitivesInput().get(i) instanceof ControlPrimitive) {
					xValue[i] = obj.getPrimitivesInput().get(i).getProvision();
				} else {
					xValue[i] = ((EnvironmentalPrimitive)obj.getPrimitivesInput().get(i)).getLatest();
				}
			}
			
			
			map.put(obj,obj.predict(xValue)) ;
		}
		
		return map;
	}
}
