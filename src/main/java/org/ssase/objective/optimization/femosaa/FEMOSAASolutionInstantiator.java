package org.ssase.objective.optimization.femosaa;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import org.femosaa.core.EAConfigure;
import org.femosaa.core.SASSolutionInstantiator;
import org.ssase.objective.Objective;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.util.Repository;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.util.JMException;

public class FEMOSAASolutionInstantiator implements SASSolutionInstantiator {

	protected List<Objective> objectives;
	// Key = objective, Value = the index of control variable in MOEASolution, the order
	// is consistent with the CP in QoS object. This include EP, in which case the 
	// -1 value would be used.
	protected Map<Objective, Integer[]> map;
	// The two below only needed for specific work, e.g., comparing pareto and weighted optimization
	protected double[] weights;
	// Bounds of weighted sum normalization
	protected double[][] fixed_bounds;
	
	private Set<String> measurement_record;
	
	private FEMOSAASolutionInstantiator(){
		
	}
	
	public FEMOSAASolutionInstantiator(List<Objective> objectives) {
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
		//System.out.print("Obj " +objectives.get(0).getName() + "\n");
		FEMOSAASolution sol = null;
		try {
			sol = new FEMOSAASolution(problem);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sol.init(objectives, map);
		return sol;
	}

	@Override
	public Solution getSolution(Solution solution) {
		FEMOSAASolution sol = new FEMOSAASolution(solution);
		sol.init(objectives, map);
		if(solution instanceof FEMOSAASolution) {
			sol.setFuzzyID(((FEMOSAASolution)solution).getFuzzyID());
		}
		
		return sol;
	}

	@Override
	public Solution getSolution() {
		FEMOSAASolution sol = new FEMOSAASolution();
		sol.init(objectives, map);
		return sol;
	}

	@Override
	public Solution getSolution(Problem problem, Variable[] variables) {
		FEMOSAASolution sol = null;
		try {
			sol = new FEMOSAASolution(problem, variables);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sol.init(objectives, map);
		return sol;
	}

	@Override
	public Solution getSolution(int objective_number) {
		FEMOSAASolution sol = new FEMOSAASolution(objective_number);
		sol.init(objectives, map);
		return sol;
	}

	@Override
	public double[][] getLambda() {
		return Repository.lambda_;
	}

	@Override
	public SolutionSet fuzzilize(SolutionSet set) {
		SolutionSet newSet = new SolutionSet();
		for (int i = 0; i < set.size(); i++) {
			Solution newS = getSolution(set.get(i));
			((FEMOSAASolution)newS).setFuzzyID(i);
			newSet.add(newS);
		}
		for (int i = 0; i < objectives.size(); i++) {		
			for (int j = 0; j < newSet.size(); j++) {
				Repository.getRequirementProposition(objectives.get(i).getName()).fuzzilize(newSet.get(j), i);
			}
		}
		return newSet;
	}
	
	@Override
	public SolutionSet fuzzilize(SolutionSet set, double increment) {
		SolutionSet newSet = new SolutionSet();
		for (int i = 0; i < set.size(); i++) {
			Solution newS = getSolution(set.get(i));
			((FEMOSAASolution)newS).setFuzzyID(i);
			newSet.add(newS);
		}
		for (int i = 0; i < objectives.size(); i++) {		
			for (int j = 0; j < newSet.size(); j++) {
				Repository.getRequirementProposition(objectives.get(i).getName()).fuzzilize(newSet.get(j), i, increment);
			}
		}
		return newSet;
	}

	@Override
	public Solution defuzzilize(int i, SolutionSet newPopulation,
			SolutionSet oldPopulation) {
		return oldPopulation.get(((FEMOSAASolution)newPopulation.get(i)).getFuzzyID());
	}
	
	public Solution defuzzilize(Solution s,
			SolutionSet oldPopulation) {
		return oldPopulation.get(((FEMOSAASolution)s).getFuzzyID());
	}
	
	public void defuzzilizeAndRemove(Solution s,
			SolutionSet oldPopulation) {
		oldPopulation.remove(((FEMOSAASolution)s).getFuzzyID());
	}

	@Override
	public double[] getWeights() {
		return weights;
	}
	
	public void setWeights(double[] weights) {
		this.weights = weights;
	}
	
	@Override
	public double[][] getFixedBounds() {
		return fixed_bounds;
	}
	
	public void setFixedBounds(double[][] fixed_bounds) {
		this.fixed_bounds = fixed_bounds;
	}

	public int record(SolutionSet set) {
		if(EAConfigure.getInstance().measurement == -1) {
			return 0;
		}
		
		if (measurement_record == null) {
			measurement_record = new HashSet<String>();
		}
		
		int r = 0;
		for (int i = 0; i < set.size(); i++) {
			r += record(set.get(i));
		}
		
		return r;
	}
	
	public int record(Solution s) {
		if(EAConfigure.getInstance().measurement == -1) {
			return 0;
		}
		
		if (measurement_record == null) {
			measurement_record = new HashSet<String>();
		}
		
		String str = "";
		for (int i = 0; i < s.getDecisionVariables().length; i++) {
			try {
				str += String.valueOf(s.getDecisionVariables()[i].getValue()) + ",";
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//System.out.print("size: " + measurement_record.size() + "---\n");
		if (measurement_record.contains(str)) {
			return 0;
		} else {
			measurement_record.add(str);
			return 1;
		}
	}

	
	


}
