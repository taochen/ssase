package org.ssase.objective.optimization.bb;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jmetal.core.Variable;
import jmetal.encodings.variable.Int;
import jmetal.problems.SASSolution;
import jmetal.util.JMException;

import org.ssase.objective.Objective;
import org.ssase.objective.optimization.femosaa.FEMOSAASolution;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.region.Region;
import org.ssase.util.Repository;

/**
 * This is for the integer non-/linear programming approach.
 * @author tao
 *
 */
public class BranchAndBoundRegion extends Region {

	
	protected static final long EXECUTION_TIME = 40000;
	
	@Override
	public LinkedHashMap<ControlPrimitive, Double> optimize() {
		List<ControlPrimitive> list = Repository.getSortedControlPrimitives(objectives.get(0));
		
		
		LinkedHashMap<ControlPrimitive, Double> current = new LinkedHashMap<ControlPrimitive, Double>();
		Map<Objective, Double> map = getBasis();
		
		double[] currentDecision = new double[list.size()];
		
		Node best = null;
		Double[] bestResult = null;
		
		for (Objective obj : objectives) {
			for(Primitive p : obj.getPrimitivesInput()){
				if (p instanceof ControlPrimitive) {
					current.put((ControlPrimitive)p, (double)p.getProvision());
					currentDecision[list.indexOf((ControlPrimitive)p)] = (double)p.getProvision();
				}
			}
		}
		
		
		LinkedBlockingQueue <Node> q = new LinkedBlockingQueue <Node>();
		
		 
		 for (double d : list.get(0).getValueVector()) {
			 q.offer(new Node(0, d, currentDecision));
		 }
		 
		 long start = System.currentTimeMillis();
		 int count = 0;
		 do {
			 
			 // To avoid a extremly long runtime
			 if (( System.currentTimeMillis() - start) > EXECUTION_TIME) {
				 break;
			 }
			 
			 count++;
			 //System.out.print("Rune " + count + "\n");
			 
			 Node node = q.poll();
			 Double[] v = doWeightSum(list, node.decision, map);
			 if (bestResult == null || v[0] > bestResult[0]) {
				 best = node;
				 bestResult = v;
			 }
			 
			 if (node.index + 1 < list.size()) {
				 for (double d : list.get(node.index + 1).getValueVector()) {
					 q.offer(new Node(node.index + 1, d, node.decision));
				 }
			 }
			 
			 
		 } while (!q.isEmpty());
		 
		 q.clear();
		 
		 for (int i = 0; i < best.decision.length; i++) {
			 current.put(list.get(i), best.decision[i]);
		 }
		 
		 // Starting the dependency check and log
		 
		 double[][] optionalVariables = new double[list.size()][];
			for (int i = 0; i < optionalVariables.length; i++) {
				optionalVariables[i] = list.get(i).getValueVector();
			}
			
			// This is a static method
			SASSolution.init(optionalVariables);
			
	        FEMOSAASolution dummy = new FEMOSAASolution();
	        dummy.init(objectives, null);
			Variable[] variables = new Variable[list.size()];
			for (int i = 0; i < list.size(); i ++) {
				variables[i] = new Int(0, list.get(i).getValueVector().length-1);		
			}
			
			dummy.setDecisionVariables(variables);
			
			for (int i = 0; i < list.size(); i ++) {
				
				double v = current.get(list.get(i));
				double value = 0;
				
				for (int j = 0; j < list.get(i).getValueVector().length; j++) {
					if(list.get(i).getValueVector()[j] == v) {
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
			
			Region.logDependencyForFinalSolution(dummy);
			
			if (!dummy.isSolutionValid()) {
				try {
					dummy.correctDependency();
					
					for (int i = 0; i < list.size(); i ++) {
						current.clear();
						
						current.put(list.get(i), dummy.getDecisionVariables()[i].getValue());
					}
					
					System.out.print("The final result does not satisfy all dependency, thus correct it\n");
					
				} catch (JMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				
			print(current);
		 
		return current;
	}
	
	private Double[] doWeightSum(List<ControlPrimitive> list, double[] decision, Map<Objective, Double> map ) {
		double result = 0;
		double satisfied = 1;
		double[] xValue;
		double w = 1 / objectives.size();
		for (Objective obj : objectives) {
			xValue = new double[ obj.getPrimitivesInput().size()];
			//System.out.print(obj.getPrimitivesInput().size()+"\n");
			for(int i = 0; i < obj.getPrimitivesInput().size();i++){
				if (obj.getPrimitivesInput().get(i) instanceof ControlPrimitive) {
					xValue[i] = decision[list.indexOf(obj.getPrimitivesInput().get(i))];
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
	
	protected double[] getValueVector(Node node, List<ControlPrimitive> list){
		
		if(list.get(node.index).getName().equals("maxBytesLocalHeap")) {
			
			for (int i = 0 ; i < node.index; i++) {
				if(list.get(i).getName().equals("cacheMode") && node.decision[i] == 0.0) {
					return new double[]{0.0};
				}
				
				if(list.get(i).getName().equals("maxBytesLocalDisk") && node.decision[i]  == 0.0) {
					double[] d = new double[list.get(node.index).getValueVector().length - 1];
					System.arraycopy(list.get(node.index).getValueVector(), 1, d, 0, d.length);
					return d;
				}
			}
			
		}
		
	    if(list.get(node.index).getName().equals("maxBytesLocalDisk")) {
			
			for (int i = 0 ; i < node.index; i++) {
				if(list.get(i).getName().equals("cacheMode") && node.decision[i] == 0.0) {
					return new double[]{0.0};
				}
				
				if(list.get(i).getName().equals("maxBytesLocalHeap") && node.decision[i] == 0.0) {
					double[] d = new double[list.get(node.index).getValueVector().length - 1];
					System.arraycopy(list.get(node.index).getValueVector(), 1, d, 0, d.length);
					return d;
				}
			}
			
		}
		
		return list.get(node.index).getValueVector();
	}
	
	
	 protected class Node  {

		 
		 protected int index;
		 protected double[] decision;
		
		public Node (int index, double value, double[] given) {
			this.index = index;
			decision = new double[given.length];
			for (int i = 0 ; i < given.length; i++) {
				decision[i] = given[i];
			}
			
			decision[index] = value;
		
		}
		
		
		 
	 }

}
