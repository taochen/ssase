package org.ssase.objective.optimization.random;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jmetal.core.Variable;
import jmetal.encodings.variable.Int;
import jmetal.problems.SASSolution;
import jmetal.util.Configuration;
import jmetal.util.JMException;

import org.ssase.objective.Objective;
import org.ssase.objective.optimization.femosaa.FEMOSAASolution;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.region.Region;
import org.ssase.util.Repository;

public class HillClimbingRegion extends Region {

	public HillClimbingRegion () {
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
		long maxEvaluation = 3000;
		long count = 0;
		
		
		SASSolution.clearAndStoreForValidationOnly();
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
			
			
			current.clear();
			current.putAll(copy);
			double localBestValue = doWeightSum(current, map)[0];
			Collections.shuffle(list);
			
			for (int i = 0; i < list.size(); i++) {
				ControlPrimitive cp = list.get(i);
				double[] values = getValueVector(cp, i, list, current);
				for (double d : values) {
					//System.out.print("Run " + count + "\n");
					count ++;
					
					double temp = current.get(cp);
					current.put(cp, d);
					Double[] result = doWeightSum(current, map);
					if (localBestValue < result[0]) {
						localBestValue = result[0];
						//break;
					} else {
						current.put(cp, temp);
					}
					
					
					if(count>maxEvaluation) {
						
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
						break;
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
			
		} while (count<=maxEvaluation);
		
		
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
		
		if (satisfied != null) {
			System.out.print("We have satisfied solution!\n");
		} else {
			System.out.print("We do not have satisfied solution\n");
		}
		results.clear();
		
		LinkedHashMap<ControlPrimitive, Double> finalResult = satisfied == null? unsatisfied : satisfied;
		
		
		List<ControlPrimitive> rList = Repository.getSortedControlPrimitives(objectives.get(0));
		
		
		double[][] optionalVariables = new double[rList.size()][];
		for (int i = 0; i < optionalVariables.length; i++) {
			optionalVariables[i] = rList.get(i).getValueVector();
		}
		
		// This is a static method
		SASSolution.init(optionalVariables);
		
        FEMOSAASolution dummy = new FEMOSAASolution();
        dummy.init(objectives, null);
		Variable[] variables = new Variable[rList.size()];
		for (int i = 0; i < rList.size(); i ++) {
			variables[i] = new Int(0, rList.get(i).getValueVector().length-1);		
		}
		
		dummy.setDecisionVariables(variables);
		
		for (int i = 0; i < rList.size(); i ++) {
			
			double v = finalResult.get(rList.get(i));
			double value = 0;
			
			for (int j = 0; j < rList.get(i).getValueVector().length; j++) {
				if(rList.get(i).getValueVector()[j] == v) {
					value = j;
					break;					
				}
			}
			
			try {
				dummy.getDecisionVariables()[i].setValue(value);
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Region.logDependencyForFinalSolution(dummy);
		
		if (!dummy.isSolutionValid()) {
			try {
				dummy.correctDependency();
				
				for (int i = 0; i < rList.size(); i ++) {
					finalResult.clear();
					
					finalResult.put(rList.get(i), dummy.getDecisionVariables()[i].getValue());
				}
				
				System.out.print("The final result does not satisfy all dependency, thus correct it\n");
				
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		print(finalResult);
		return finalResult;
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
		
		if (satisfied != null) {
			System.out.print("We have satisfied solution!\n");
		} else {
			System.out.print("We do not have satisfied solution\n");
		}
		results.clear();
		return satisfied == null? unsatisfied : satisfied;
	}
	
	private LinkedHashMap<ControlPrimitive, Double> pureRandom(){
		// Random optimization.
		long interation = 600;
		long count = 0;
		
		//double bestValue = 0;
		//LinkedHashMap<ControlPrimitive, Double> best = new LinkedHashMap<ControlPrimitive, Double>();
		LinkedHashMap<ControlPrimitive, Double> current = new LinkedHashMap<ControlPrimitive, Double>();
		
		List<LinkedHashMap<ControlPrimitive, Double>> results = new ArrayList<LinkedHashMap<ControlPrimitive, Double>>();
		
		
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
			
			
			//System.out.print("current: " + result + ", best: " + bestValue + "\n");
			/*if (result > bestValue || count == 1) {
				//System.out.print("Find a new best decision!\n");
				best.clear();
				best.putAll(current);
				bestValue = result;
			}*/
			
			results.add(copy(current));
			
		} while (count<interation);
		
		
		/*for (Map.Entry<ControlPrimitive, Double> entry : current.entrySet()) {
			System.out.print(entry.getKey().getAlias() + "-" + entry.getKey().getName() + " : " + entry.getValue() + "\n");
		}*/
		
		//
		
		String global_output = "";
		for (LinkedHashMap<ControlPrimitive, Double> decision : results) {
			double[] xValue = null;
			List<Double> l = null;
			String output = "";
			for (Objective obj : objectives) {
				l = new ArrayList<Double>();
				xValue = new double[ obj.getPrimitivesInput().size()];
				//System.out.print(obj.getPrimitivesInput().size()+"\n");
				for(int i = 0; i < obj.getPrimitivesInput().size();i++){
					if (obj.getPrimitivesInput().get(i) instanceof ControlPrimitive) {
						xValue[i] = decision.get(obj.getPrimitivesInput().get(i));
						l.add(xValue[i] );
					} else {
						xValue[i] = ((EnvironmentalPrimitive)obj.getPrimitivesInput().get(i)).getLatest();
					}
				}
				
				output = output +  obj.predict(xValue) + " ";
				global_output = global_output + obj.predict(xValue) + " ";
				//result = obj.isMin()? result - (obj.predict(xValue)/(1+map.get(obj))) : result + (obj.predict(xValue)/(1+map.get(obj))) ;
			}
			global_output = global_output + "\n";
			String o = "";
			for (double d : l) {
				o = o + d + ", ";
			}
			
			System.out.print(output + " " + o + "\n");
		}
		
	    try {
	    	new File("data/RANDOM/results.dat").delete();
	        /* Open the file */
	        FileOutputStream fos   = new FileOutputStream("data/RANDOM/results.dat")     ;
	        OutputStreamWriter osw = new OutputStreamWriter(fos)    ;
	        BufferedWriter bw      = new BufferedWriter(osw)        ;
	                          
	        //for (int i = 0; i < solutionsList_.size(); i++) {
	          //if (this.vector[i].getFitness()<1.0) {
	          bw.write(global_output);
	          bw.newLine();
	          //}
	        //}
	        
	        /* Close the file */
	        bw.close();
	      }catch (IOException e) {
	        e.printStackTrace();
	      }
		
		return current;
	}
	
	private LinkedHashMap<ControlPrimitive, Double> copy (LinkedHashMap<ControlPrimitive, Double> current) {
		LinkedHashMap<ControlPrimitive, Double> result = new LinkedHashMap<ControlPrimitive, Double>();
		for (ControlPrimitive p : current.keySet()) {
			result.put(p, current.get(p));
		}
		
		return result;
	}
	
	
	private Double[] doWeightSum(LinkedHashMap<ControlPrimitive, Double> decision, Map<Objective, Double> map ) {
		double result = 0;
		double satisfied = 1;
		double[] xValue;
		double w = 1 / objectives.size();
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
			
			result = obj.isMin()? result - w * (obj.predict(xValue)/(1+map.get(obj))) : result + w * (obj.predict(xValue)/(1+map.get(obj))) ;
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
	
	protected double[] getValueVector(ControlPrimitive cp, int k, List<ControlPrimitive> list, LinkedHashMap<ControlPrimitive, Double> current){
		
		if(cp.getName().equals("maxBytesLocalHeap")) {
			
			for (int i = 0 ; i < k; i++) {
				if(list.get(i).getName().equals("cacheMode") && current.get(list.get(i)) == 0.0) {
					return new double[]{0.0};
				}
				
				if(list.get(i).getName().equals("maxBytesLocalDisk") && current.get(list.get(i)) == 0.0) {
					double[] d = new double[cp.getValueVector().length - 1];
					System.arraycopy(cp.getValueVector(), 1, d, 0, d.length);
					return d;
				}
			}
			
		}
		
	    if(cp.getName().equals("maxBytesLocalDisk")) {
			
			for (int i = 0 ; i < k; i++) {
				if(list.get(i).getName().equals("cacheMode") && current.get(list.get(i)) == 0.0) {
					return new double[]{0.0};
				}
				
				if(list.get(i).getName().equals("maxBytesLocalHeap") && current.get(list.get(i)) == 0.0) {
					double[] d = new double[cp.getValueVector().length - 1];
					System.arraycopy(cp.getValueVector(), 1, d, 0, d.length);
					return d;
				}
			}
			
		}
		
		return cp.getValueVector();
	}
}
