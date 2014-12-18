package org.ssascaling.region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ssascaling.objective.Objective;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;

public class NaiveRegion extends Region {

	public NaiveRegion (List<Objective> objectives) {
		super.objectives = objectives;
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
		long interation = 3000;
		long count = 0;
		
		double bestValue = 0;
		LinkedHashMap<ControlPrimitive, Double> best = new LinkedHashMap<ControlPrimitive, Double>();
		LinkedHashMap<ControlPrimitive, Double> current = new LinkedHashMap<ControlPrimitive, Double>();
		LinkedHashMap<ControlPrimitive, Double> copy = new LinkedHashMap<ControlPrimitive, Double>();
		List<ControlPrimitive> list = new ArrayList<ControlPrimitive>();
		
	
		
		for (Objective obj : objectives) {
			for(Primitive p : obj.getPrimitivesInput()){
				if (p instanceof ControlPrimitive) {
					current.put((ControlPrimitive)p, (double)p.getProvision());
					copy.put((ControlPrimitive)p, (double)p.getProvision());
					list.add((ControlPrimitive)p);
				}
			}
		}
		
		
		Random random = new Random();
		
		do {
			count ++;
			current.clear();
			current.putAll(copy);
			double localBestValue = doWeightSum(current);
			Collections.shuffle(list);
			
			for (ControlPrimitive cp : list) {
			
				for (double d : cp.getValueVector()) {
					double temp = current.get(cp);
					current.put(cp, d);
					if (localBestValue < doWeightSum(current)) {
						localBestValue = doWeightSum(current);
					} else {
						current.put(cp, temp);
					}
					
				}
				
			}
			
			double result = 0;
			
			try {
			
				// Assume equal weight on all objectives
			   result = doWeightSum(current);
			} catch (RuntimeException re) {
				//re.printStackTrace();
				//System.out.print( "Not satisfied\n");
				continue;
			}
			
			System.out.print("current: " + result + ", best: " + bestValue + "\n");
			if (result > bestValue || count == 1) {
				System.out.print("Find a new best decision!\n");
				best.clear();
				best.putAll(current);
				bestValue = result;
			}
			
		} while (count<interation);
		
		
		for (Map.Entry<ControlPrimitive, Double> entry : current.entrySet()) {
			System.out.print(entry.getKey().getAlias() + "-" + entry.getKey().getName() + " : " + entry.getValue() + "\n");
		}
		
		System.out.print("Final result is: " + bestValue + "\n");
		return best;
	}
	
	private LinkedHashMap<ControlPrimitive, Double> random(){
		// Random optimization.
		long interation = 3000;
		long count = 0;
		
		double bestValue = 0;
		LinkedHashMap<ControlPrimitive, Double> best = new LinkedHashMap<ControlPrimitive, Double>();
		LinkedHashMap<ControlPrimitive, Double> current = new LinkedHashMap<ControlPrimitive, Double>();
		
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
			
			double result = 0;
			
			try {
			
				// Assume equal weight on all objectives
			   result = doWeightSum(current);
			} catch (RuntimeException re) {
				//re.printStackTrace();
				//System.out.print( "Not satisfied\n");
				continue;
			}
			
			System.out.print("current: " + result + ", best: " + bestValue + "\n");
			if (result > bestValue || count == 1) {
				System.out.print("Find a new best decision!\n");
				best.clear();
				best.putAll(current);
				bestValue = result;
			}
			
		} while (count<interation);
		
		
		for (Map.Entry<ControlPrimitive, Double> entry : current.entrySet()) {
			System.out.print(entry.getKey().getAlias() + "-" + entry.getKey().getName() + " : " + entry.getValue() + "\n");
		}
		
		System.out.print("Final result is: " + bestValue + "\n");
		return best;
	}
	
	private double doWeightSum(LinkedHashMap<ControlPrimitive, Double> decision ) throws RuntimeException{
		double result = 0;
		double[] xValue;
		for (Objective obj : objectives) {
			xValue = new double[ obj.getPrimitivesInput().size()];
			//System.out.print(obj.getPrimitivesInput().size()+"\n");
			for(int i = 0; i < obj.getPrimitivesInput().size();i++){
				if (obj.getPrimitivesInput().get(i) instanceof ControlPrimitive) {
					xValue[i] = decision.get(obj.getPrimitivesInput().get(i));
				} else {
					xValue[i] = 0;
				}
			}
			
			if (!obj.isSatisfied(xValue)) {
				//System.out.print(obj.getName() + " is not satisfied " + obj.predict(xValue) + "\n");
				throw new RuntimeException();
			}
			
			result = obj.isMin()? result - obj.predict(xValue) : result + obj.predict(xValue) ;
		}
		
		return result;
	}
}
