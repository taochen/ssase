package org.ssascaling.objective.optimization.MOGA;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.ssascaling.objective.Objective;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.util.Tuple;
import org.ssascaling.util.Util;

public class Individual implements Comparable<Individual> {
	
	protected LinkedHashMap<ControlPrimitive,  Double> cpInput = new LinkedHashMap<ControlPrimitive,  Double>();
	
	
	protected double[] front;
	protected int unsatisfy = 0;
	
	protected LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> objectiveMap;
	
	protected double crowdValue = 0;
	
	public Individual (LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> objectiveMap) {
		this.objectiveMap = objectiveMap;
	}
	
	public Individual (LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> objectiveMap, boolean trigger) {
		this.objectiveMap = objectiveMap;
		
		for (Map.Entry<Objective, List<Tuple<Primitive, Double>>> entry : objectiveMap.entrySet()){
			
			for (Tuple<Primitive, Double> t : entry.getValue()) {
				
				if(t.getVal1() instanceof ControlPrimitive) {
					ControlPrimitive cp = (ControlPrimitive)t.getVal1();
					cpInput.put(cp, cp.getValueVector()[Util.randInt(0, cp.getValueVector().length-1)]);
					
				}
				
			}
			
			
		}
	}
	
	public boolean paretoDominates (Individual another) {
		// Should be the same as the size of objectives
		double[] front = getFront();
		int index = 0;

		for (Objective obj : objectiveMap.keySet()){
			if (!obj.isBetter(front[index], another.getFront()[index])){
				return false;
			}
			
			index++;
		}
		
		return true;
	}
	
	public int isSatisfy(){
		getFront();
		return unsatisfy;
	}
	
	public double[] getFront(){
		if(front == null) {
			front = calculateFront(cpInput);
		}
		
		return front;
	}
	
    private double[] calculateFront (LinkedHashMap<ControlPrimitive, Double> cpInput){
    	
    	int index = 0;
    	double[] front = new double[objectiveMap.size()];
    	for (Map.Entry<Objective, List<Tuple<Primitive, Double>>> entry : objectiveMap.entrySet()){
    		double[] xValue = new double[entry.getValue().size()];
    		
    		for (int i = 0; i < entry.getValue().size() ;i++) {
    			Primitive p = entry.getValue().get(i).getVal1();
    			
    			// Means it has to been a value in this ant's solution as it is CP.
    			if (p instanceof ControlPrimitive) {
    				xValue[i] = cpInput.get(p);
    			// Otherwise, if it is a EP then use its original value as we can not control EP.
    			} else {
    				xValue[i] = entry.getValue().get(i).getVal2();
    			}
    			
    		}
    		
    		front[index] = entry.getKey().predict(xValue);
    		if (!entry.getKey().isSatisfied(xValue)) {
    			unsatisfy++;
    		}
    		
    		
    		
    		index++;
    	}
    	
    	return front;
    }

    
    public void addCrowdingValue(double value){
    	crowdValue += value;
    }
    
    public double getObjectiveValue(int index) {
    	return front[index];
    }
    
   

	@Override
	public int compareTo(Individual o) {
		
		if (crowdValue > o.crowdValue) 
			return -1;
		else if (crowdValue < o.crowdValue) 
			return 1;
		else
		    return 0;
	}
	
	
	public Individual crossover(Individual another) {
		
		Individual child = new Individual(objectiveMap);
		
	    Random randomGenerator = new Random();
		for (ControlPrimitive cp : cpInput.keySet()) {
			
			int randomInt = randomGenerator.nextInt(100);
			
			if (randomInt < 50) {
				child.cpInput.put(cp,  cpInput.get(cp));
			} else {
				child.cpInput.put(cp, another.cpInput.get(cp));
			}
			
		}
		
		return child;
	}
	
	public void mutate(){
		
		List<Integer> number = new ArrayList<Integer>();
		
		int no = 0;
		int limit = Util.randInt(1, cpInput.size());
		for (int i = 0; i < limit; i++) {
			no = Util.randInt(0, cpInput.size()-1);
			if (!number.contains(no)) {
			   number.add(no);
			}
		} 
		
		int index = 0;
		
		for (ControlPrimitive cp : cpInput.keySet()) {
			
			
			if (number.contains(index)) {
				cpInput.put(cp,cp.getValueVector()[Util.randInt(0, cp.getValueVector().length-1)]);
			}
			
			index++;
		}
	}
	
	public LinkedHashMap<ControlPrimitive, Double> getFinalResult(){		
		return cpInput;
	} 
	
	public void resetCrowdingValue(){
		crowdValue = 0;
	}
	
	public double getCrowdValue(){
		return crowdValue;
	}
}
