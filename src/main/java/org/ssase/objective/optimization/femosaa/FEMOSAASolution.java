package org.ssase.objective.optimization.femosaa;

import java.util.List;
import java.util.Map;

import org.femosaa.core.SASSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssase.objective.Objective;
import org.ssase.objective.QualityOfService;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.region.OptimizationType;
import org.ssase.region.Region;
import org.ssase.util.Repository;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.variable.Int;
import jmetal.util.JMException;

public class FEMOSAASolution extends SASSolution {

	protected static final Logger logger = LoggerFactory
	.getLogger(FEMOSAASolution.class);
	// This should be the same instance as the one in Region.
	protected List<Objective> objectives;
	
	// Key = objective, Value = the index of variable in FEMOSAASolution, this order
	// is consistent with the CP in QoS object.
	protected Map<Objective, Integer[]> modelMap;
	
	// To allow copy of the same fuzzy objective.
	protected int fuzzyID = 0;

	public int getFuzzyID() {
		return fuzzyID;
	}

	public void setFuzzyID(int fuzzyID) {
		this.fuzzyID = fuzzyID;
	}

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
			
			
			boolean isValid = true;
			if(QualityOfService.isDelegate()) {		
				if(Region.selected == OptimizationType.FEMOSAA01 ||
						Region.selected == OptimizationType.NSGAII01 ||
						Region.selected == OptimizationType.IBEA01) {
					

					FEMOSAASolutionInstantiator solInt = new FEMOSAASolutionInstantiator(objectives);
					
					List<ControlPrimitive> list = Repository.getSortedControlPrimitives(objectives.get(0));
					
					FEMOSAASolution s = (FEMOSAASolution)solInt.getSolution(objectives.size());
		               
					Variable[] variables = new Variable[list.size()];
					for (int l = 0; l < list.size(); l ++) {
						variables[l] = new Int(0, list.get(l).getValueVector().length-1);		
					}
					
				
					
				
					s.setDecisionVariables(variables);
					for(int k = 0; k < s.getDecisionVariables().length; k++) {
						
						for (int j = 0; j < Repository.getSortedControlPrimitives(objectives.get(0)).get(k).getValueVector().length; j++) {
							if(Repository.getSortedControlPrimitives(objectives.get(0)).get(k).getValueVector()[j] == xValue[k]) {
								try {
									s.getDecisionVariables()[k].setValue(j);
								} catch (JMException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								break;
							}
						}
						
						
					}
					//System.out.print("Insiding manual check validiy\n");
					isValid = s.isSolutionValid();	
					
				} else {
				   isValid = this.isSolutionValid();	
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
			
			
			
			if(QualityOfService.isDelegate()) {
				try{
				if(isValid) {
					result[i] = objectives.get(i).predict(xValue);
				} else {
					// Assume minimizing
					result[i] = Double.MAX_VALUE;
				}
				}catch(Throwable t) {
					System.err.print("This solution is " + isValid + "\n");
					String o = "";
					for(int k = 0; k < this.getDecisionVariables().length; k++) {
						try {
							o += this.getDecisionVariables()[k].getValue() + ", ";
							
						} catch (JMException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					System.err.print(o + "\n");
					t.printStackTrace();
				}
				
			} else {
			   result[i] = objectives.get(i).predict(xValue);
			}
			//result[i] = objectives.get(i).predict(xValue) * 100 / objectives.get(i).getMax();
			 
			 if(logger.isDebugEnabled() && Region.selected != OptimizationType.FEMOSAA01
					 && Region.selected != OptimizationType.NSGAII01
					 && Region.selected != OptimizationType.IBEA01) {
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

	@Override
	public void updateNormalizationBounds(double[] f) {
		for (int i = 0; i < f.length; i++) {
			org.ssase.requirement.RequirementProposition rp = Repository.getRequirementProposition(objectives.get(i).getName());
			if(rp == null) {
				continue;
			}
			rp.updateNormalizationBounds(f[i]);
		}
		
	}
}
