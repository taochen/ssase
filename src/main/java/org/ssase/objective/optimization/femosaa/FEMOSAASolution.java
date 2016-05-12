package org.ssase.objective.optimization.femosaa;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssase.objective.Objective;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.region.OptimizationType;
import org.ssase.region.Region;
import org.ssase.util.Repository;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.problems.SASSolution;
import jmetal.util.JMException;

public class FEMOSAASolution extends SASSolution {

	protected static final Logger logger = LoggerFactory
	.getLogger(FEMOSAASolution.class);
	// This should be the same instance as the one in Region.
	protected List<Objective> objectives;
	
	// Key = objective, Value = the index of variable in FEMOSAASolution, this order
	// is consistent with the CP in QoS object.
	protected Map<Objective, Integer[]> modelMap;

	public FEMOSAASolution(Problem problem) throws ClassNotFoundException {
		super(problem);
	}

	public FEMOSAASolution(Problem problem, Variable[] variables) throws ClassNotFoundException {
		super(problem, variables);
	}


	public FEMOSAASolution(Solution solution) {
		super(solution);
	}

	public FEMOSAASolution(int numberOfObjectives) {
		super(numberOfObjectives);
	}
	
	public FEMOSAASolution() {
		super();
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
		logger.debug(" ");
		for (int i = 0; i < objectives.size(); i++) {
			
			logger.debug(objectives.get(i).getName() + " start prediction");
			
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
			
//			// Some optimization algorithm need only the normalized value, as
//			// they calculate fitness as weighted-sum.
//			if(Region.selected == OptimizationType.GP) {
//				// We temporarly use the max value to normalize.
//				  result[i] = objectives.get(i).predict(xValue)/objectives.get(i).getMax();
//			} else {
//			      result[i] = objectives.get(i).predict(xValue);
//			}
			 result[i] = objectives.get(i).predict(xValue);
			//result[i] = objectives.get(i).predict(xValue) * 100 / objectives.get(i).getMax();
			 
			 if(logger.isDebugEnabled() && Region.selected != OptimizationType.FEMOSAA01) {
				 try {
				 List<ControlPrimitive> list = Repository.getSortedControlPrimitives(objectives.get(i));
				 String r = "";
				 for (int k = 0; k < list.size(); k++ ){
					 r = r + list.get(k).getName() + "=" + super.getDecisionVariables()[k].getValue() + " (" + 
					 list.get(k).getValueVector()[(int)super.getDecisionVariables()[k].getValue()]+ ") ";
				 }
				 
				 logger.debug("In optimization: " + r);
				 logger.debug("In modeling: ");
				
				 for (int k = 0; k < objectives.get(i).getPrimitivesInput().size(); k++) {
					 logger.debug(k + "=" + objectives.get(i).getPrimitivesInput().get(k).getName());
				 }
				 
				 logger.debug("The xValue: ");
				
					
				 for (int k = 0; k < xValue.length;k++) {
					 logger.debug(k + "=" + xValue[k]);
				 }
					 
				 
				 } catch (Exception e) {
					 
				 }
			 }
			 
			
			 
			 
			 
			 logger.debug(objectives.get(i).getName() + " end prediction");
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
		
		throw new RuntimeException("Index " + index + " has no entry in the getSortedControlPrimitives");
		
	}

	public void init(List<Objective> objectives, Map<Objective, Integer[]> modelMap) {
		this.objectives = objectives;
		this.modelMap = modelMap;
	}
}
