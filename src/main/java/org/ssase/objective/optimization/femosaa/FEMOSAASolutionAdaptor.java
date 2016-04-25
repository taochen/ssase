package org.ssase.objective.optimization.femosaa;

import java.util.LinkedHashMap;
import java.util.List;

import jmetal.core.Solution;
import jmetal.problems.SASSolution;

import org.ssase.objective.Objective;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.util.Repository;


public class FEMOSAASolutionAdaptor {

	
	private static final FEMOSAASolutionAdaptor adaptor = new FEMOSAASolutionAdaptor();
	

	
	public static FEMOSAASolutionAdaptor getInstance(){
		return adaptor;
	}
	
	public int[][] convertInitialLimits(Objective obj){
		List<ControlPrimitive> list = Repository.getSortedControlPrimitives(obj);
		int[][] vars = new int[list.size()][];
		double[][] optionalVariables = new double[list.size()][];
		for (int i = 0; i < vars.length; i++) {
			vars[i] = new int[]{0, list.get(i).getValueVector().length - 1};
			optionalVariables[i] = list.get(i).getValueVector();
		}
		
		// This is a static method
		SASSolution.init(optionalVariables);
		return vars;
	}
	
	public LinkedHashMap<ControlPrimitive, Double> convertSolution(Solution solution, Objective obj) {
		
		List<ControlPrimitive> list = Repository.getSortedControlPrimitives(obj);
		LinkedHashMap<ControlPrimitive, Double> result = new LinkedHashMap<ControlPrimitive, Double>();
		
		
		FEMOSAASolution sol = (FEMOSAASolution)solution;
		
		for (int i = 0; i < list.size(); i++) {
			result.put(list.get(i), sol.getVariableValueFromIndex(i));
		}
			
		return result;
	}
}
