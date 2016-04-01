package org.ssase.objective.optimization.adaptor;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.ssase.objective.Objective;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.util.Repository;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.problems.SASSolutionInstantiator;

public class MOEASolutionInstantiator implements SASSolutionInstantiator {

	private List<Objective> objectives;
	// Key = objective, Value = the index of control variable in MOEASolution, the order
	// is consistent with the CP in QoS object. This include EP, in which case the 
	// -1 value would be used.
	private Map<Objective, Integer[]> map;
	
	private MOEASolutionInstantiator(){
		
	}
	
	public MOEASolutionInstantiator(List<Objective> objectives) {
		super();
		this.objectives = objectives;
		map = new HashMap<Objective, Integer[]>();
		for (Objective obj : objectives) {
			List<ControlPrimitive> cp = Repository.getSortedControlPrimitives(obj);
			Integer[] input = new Integer[obj.getPrimitivesInput().size()];
			// Use the length of actual inputs of model to avoid some primitives being 
			// detect as irrelevant.
			for (int i = 0; i < input.length; i++) {
				
				if (obj.getPrimitivesInput().get(i) instanceof ControlPrimitive) {
					input[i] = cp.indexOf(obj.getPrimitivesInput().get(i));
				} else {
					input[i] = -1;
				}
				
				
			}
			map.put(obj, input);
		}
	}

	@Override
	public Solution getSolution(Problem problem) {
		MOEASolution sol = null;
		try {
			sol = new MOEASolution(problem);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sol.init(objectives, map);
		return sol;
	}

	@Override
	public Solution getSolution(Solution solution) {
		MOEASolution sol = new MOEASolution(solution);
		sol.init(objectives, map);
		return sol;
	}

	@Override
	public Solution getSolution() {
		MOEASolution sol = new MOEASolution();
		sol.init(objectives, map);
		return sol;
	}

	@Override
	public Solution getSolution(Problem problem, Variable[] variables) {
		MOEASolution sol = null;
		try {
			sol = new MOEASolution(problem, variables);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sol.init(objectives, map);
		return sol;
	}


}
