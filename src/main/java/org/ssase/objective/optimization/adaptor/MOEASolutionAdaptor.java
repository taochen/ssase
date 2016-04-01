package org.ssase.objective.optimization.adaptor;

import java.util.LinkedHashMap;
import java.util.List;

import jmetal.core.Solution;

import org.ssase.objective.Objective;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.util.Repository;


public class MOEASolutionAdaptor {

	
	private static final MOEASolutionAdaptor adaptor = null;
	
	public static MOEASolutionAdaptor getInstance(){
		return adaptor;
	}
	
	public int[][] convertInitialLimits(Objective obj){
		List<ControlPrimitive> list = Repository.getSortedControlPrimitives(obj);
		int[][] vars = new int[list.size()][];
		
		for (int i = 0; i < vars.length; i++) {
			vars[i] = new int[]{0, list.get(i).getValueVector().length - 1};
		}
		
		return vars;
	}
	
	public LinkedHashMap<ControlPrimitive, Double> convertSolution(Solution solution, Objective obj) {
		
		List<ControlPrimitive> list = Repository.getSortedControlPrimitives(obj);
		LinkedHashMap<ControlPrimitive, Double> result = new LinkedHashMap<ControlPrimitive, Double>();
		
		
		MOEASolution sol = (MOEASolution)solution;
		
		for (int i = 0; i < list.size(); i++) {
			result.put(list.get(i), sol.getVariableValueFromIndex(i));
		}
			
		return result;
	}
}
