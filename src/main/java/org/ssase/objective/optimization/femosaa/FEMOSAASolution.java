package org.ssase.objective.optimization.femosaa;

import java.util.List;
import java.util.Map;

import org.ssase.objective.Objective;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.util.Repository;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.problems.SASSolution;
import jmetal.util.JMException;

public class FEMOSAASolution extends SASSolution {

	// This should be the same instance as the one in Region.
	private List<Objective> objectives;
	
	// Key = objective, Value = the index of variable in FEMOSAASolution, this order
	// is consistent with the CP in QoS object.
	private Map<Objective, Integer[]> modelMap;

	public FEMOSAASolution(Problem problem) throws ClassNotFoundException {
		super(problem);
		init();
	}

	public FEMOSAASolution(Problem problem, Variable[] variables) throws ClassNotFoundException {
		super(problem, variables);
		init();
	}


	public FEMOSAASolution(Solution solution) {
		super(solution);
		init();
	}

	public FEMOSAASolution() {
		super();
		init();
	}
	
	/**
	 * Initialize dependency for mutation and crossover.
	 */
	public void init(){
		
	}
	
	/**
	 * Note that the elements of objective.getPrimitivesInput()
	 * and Repository.getSortedControlPrimitives(objective) might be different.
	 * Because we consider the control primitives in optimization even they are
	 * removed as QoS model input. So getSortedControlPrimitives implies all
	 * possible control primitives for modeling. 
	 * 
	 * But Repository.getSortedControlPrimitives(objective) should be the same
	 * as the variables that are considered in optimization.
	 */
	@Override
	public double[] getObjectiveValuesFromIndexValue() {
		double[] result = new double[objectives.size()];
		
		for (int i = 0; i < objectives.size(); i++) {
			Integer[] values = modelMap.get(objectives.get(i));
			double[] xValue = new double[values.length];
			// Use the length of actual inputs of model to avoid some primitives being 
			// detect as irrelevant.
			for (int j = 0; j < values.length; j++) {
				if (values[j] >= 0) {
					xValue[j] = getVariableValueFromIndex(values[j]);
				} else {
					// Means this is an EP.
					xValue[j] = ((EnvironmentalPrimitive)objectives.get(i)
							.getPrimitivesInput().get(j)).getLatest();
				}
			}
			
//			result[i] = org.ssase.util.test.FEMOSAATester.objectives.size() > 0?
//					org.ssase.util.test.FEMOSAATester.objectives.get(i).predict(xValue) :
//					objectives.get(i).predict(xValue);
			result[i] = objectives.get(i).predict(xValue);
			//result[i] = objectives.get(i).predict(xValue) * 100 / objectives.get(i).getMax();
		}
		
		
		return result;
	}

	@Override
	public double getVariableValueFromIndex(int index) {
		try {
			return Repository.getSortedControlPrimitives(objectives.get(0)).get(index)
			.getValueVector()[(int)super.getDecisionVariables()[index].getValue()];
		} catch (JMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	 
		return 0;
	}

	public void init(List<Objective> objectives, Map<Objective, Integer[]> modelMap) {
		this.objectives = objectives;
		this.modelMap = modelMap;
	}
}
