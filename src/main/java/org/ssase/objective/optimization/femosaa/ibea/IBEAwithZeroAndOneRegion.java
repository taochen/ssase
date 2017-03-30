package org.ssase.objective.optimization.femosaa.ibea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.core.Variable;
import jmetal.encodings.variable.Int;
import jmetal.metaheuristics.ibea.IBEA_SAS_main;
import jmetal.util.JMException;

import org.femosaa.core.SASAlgorithmAdaptor;
import org.femosaa.core.SASSolution;
import org.femosaa.core.SASSolutionInstantiator;
import org.ssase.objective.Objective;
import org.ssase.objective.optimization.femosaa.FEMOSAASolution;
import org.ssase.objective.optimization.femosaa.FEMOSAASolutionInstantiator;
import org.ssase.objective.optimization.femosaa.variability.fm.FeatureModel;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.region.Region;
import org.ssase.util.Repository;
import org.ssase.util.Tuple;


/**
 * implement the 0/1 representation of features
 * @author tao
 *
 */

public class IBEAwithZeroAndOneRegion extends IBEARegion {

	public LinkedHashMap<ControlPrimitive, Double> optimize() {
		
		
		
		
		LinkedHashMap<ControlPrimitive, Double> result = null;
		synchronized (lock) {
			while (waitingUpdateCounter != 0) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

			isLocked = true;
			
			
			FeatureModel model = Repository.getFeatureModel(objectives.get(0));
			
			List<Integer[]> list = model.getZeroAndOneOptionalValueList();
			
			vars = new int[list.size()][];
			double[][] optionalVariables = new double[list.size()][];
			for (int i = 0; i < vars.length; i++) {
				// These are not used for fetch values, but used to count
				// the boundary.
				vars[i] = new int[]{0, 1};
				optionalVariables[i] = new double[]{0d, 1d};
			}
			
			// This is a static method
			SASSolution.init(optionalVariables);
			SASSolution.clearAndStoreForValidationOnly();
			

			ZeroAndOneFEMOSAASolutionInstantiator inst = new ZeroAndOneFEMOSAASolutionInstantiator(objectives);
			
            SASAlgorithmAdaptor algorithm = getAlgorithm();
			Solution solution = null;
			try {
				solution = algorithm.execute(inst, vars, objectives.size(), 0);		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			List<ControlPrimitive> rList = Repository.getSortedControlPrimitives(objectives.get(0));
			result = new LinkedHashMap<ControlPrimitive, Double>();
			
			FEMOSAASolution sol = (FEMOSAASolution)solution;
			
			for (int i = 0; i < rList.size(); i++) {
				result.put(rList.get(i), sol.getVariableValueFromIndex(i));
			}
		
			
			print(result);

			isLocked = false;
			lock.notifyAll();
		}
		System.out.print("================= Finish optimization ! =================\n");
		
		return result;
	}
	
	protected SASAlgorithmAdaptor getAlgorithm(){
		return new IBEA_SAS_main(){
			
			
			public Solution execute(SASSolutionInstantiator factory, int[][] vars,
					int numberOfObjectives_, int numberOfConstraints_)
					throws JMException, SecurityException, IOException,
					ClassNotFoundException {
				SolutionSet pareto_front = findParetoFront(factory, vars,
						numberOfObjectives_, numberOfConstraints_);
				
				//logDependencyAfterEvolution(pareto_front);
				
				SolutionSet result = correctDependencyAfterEvolution(pareto_front);
				if(result.size() == 0) {
					
					FEMOSAASolutionInstantiator solInt = new FEMOSAASolutionInstantiator(objectives);
					
					List<ControlPrimitive> list = Repository.getSortedControlPrimitives(objectives.get(0));
					SolutionSet set = new SolutionSet(pareto_front.size());
					for (int k = 0; k < pareto_front.size(); k++) {
						Solution s = pareto_front.get(k);
		                FEMOSAASolution dummy = (FEMOSAASolution)solInt.getSolution(objectives.size());
		               
						Variable[] variables = new Variable[list.size()];
						for (int i = 0; i < list.size(); i ++) {
							variables[i] = new Int(0, list.get(i).getValueVector().length-1);		
						}
						
					
						
					
						dummy.setDecisionVariables(variables);
						
						//boolean isValid = true;
						for (int i = 0; i < list.size(); i ++) {
							int index = ((ZeroAndOneFEMOSAASolution)s).translateToIndexWhenDepdencyInjection(i, true);
							//int sub = ((ZeroAndOneFEMOSAASolution)s).translateToIndexWhenDepdencyInjection(i, false);
							
//							if(sub == -1) {
//								isValid = false;
//							}
							
							try {
								dummy.getDecisionVariables()[i].setValue(index);
							} catch (JMException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
					
						dummy.correctDependency();
						 double[] f = dummy.getObjectiveValuesFromIndexValue();
							
							for (int l = 0; l < f.length ; l ++) {
								dummy.setObjective(l, f[l]);
							}
						set.add(dummy);
					}
					
					pareto_front = set;
					
				} else {
					pareto_front = result;
				}
				
				pareto_front = filterRequirementsAfterEvolution(pareto_front);
				pareto_front = doRanking(pareto_front);
				Region.printParetoFront(pareto_front, objectives);
				return findSoleSolutionAfterEvolution(pareto_front);
			}

			
			protected SolutionSet filterRequirementsAfterEvolution(SolutionSet pareto_front){			
				Iterator<Solution> itr = pareto_front.iterator();
				
				
				List<Solution> list = new ArrayList<Solution>();
				
				logger.debug("Decisions for checking requirements: " + pareto_front.size());
				while(itr.hasNext()) {
					Solution s = itr.next();
					
//					if (logger.isDebugEnabled() && s instanceof FEMOSAASolution) {
//
//						List<ControlPrimitive> cps = Repository
//								.getSortedControlPrimitives(objectives.get(0));
//						String r = "";
//						for (int i = 0; i < cps.size(); i++) {
//							
//								r = r
//										+ cps.get(i).getName()
//										+ "="
//										+ ((FEMOSAASolution)s).getVariableValueFromIndex(i)
//										+ " ";
//							
//						}
//						logger.debug("Decision: " + r);
//					}
					boolean isAdd = true;
					for (int i = 0; i < s.numberOfObjectives(); i++) {
						if(objectives.get(i).isMin()? objectives.get(i).getConstraint() < s.getObjective(i): 
							objectives.get(i).getConstraint() > s.getObjective(i)) {
							isAdd = false;
							break;
						}
					}
					
					if(isAdd) {
						list.add(s);
					}
				}
				

				logger.debug("Number of decisions that satisfies all requirements: " + list.size());
				// If no satisfied solutions, return all as default.
				if(list.size() == 0) {
					logger.debug("No decision that satisfies all requirements, thus return all decisions found");
					return pareto_front;
				}
				
				SolutionSet set = new SolutionSet(list.size());
				for(Solution s : list) {
					set.add(s);
				}
				
				return set;
			}
			protected SolutionSet correctDependencyAfterEvolution(
					SolutionSet pareto_front) {
				Iterator<Solution> itr = pareto_front.iterator();
				double count = 0;
				double total = pareto_front.size();
				
				List<Solution> finalList = new ArrayList<Solution>();
				

				List<ControlPrimitive> list = Repository.getSortedControlPrimitives(objectives.get(0));
				
				while(itr.hasNext()) {
					Solution s = itr.next();
					
					FEMOSAASolution dummy = new FEMOSAASolution();
					
					Variable[] variables = new Variable[list.size()];
					for (int i = 0; i < list.size(); i ++) {
						variables[i] = new Int(0, list.get(i).getValueVector().length-1);		
					}
					
					dummy.setDecisionVariables(variables);
					boolean isValid = true;
					for (int i = 0; i < list.size(); i ++) {
						int index = ((ZeroAndOneFEMOSAASolution)s).translateToIndexWhenDepdencyInjection(i, false);
						if(index == -1) {
							isValid = false;
							break;
						}
						
						try {
							dummy.getDecisionVariables()[i].setValue(index);
						} catch (JMException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					if(isValid && ((SASSolution)dummy).isSolutionValid()) {
						count++;
						finalList.add(s);
					}
				}
				
				
				System.out.print("before checking dependency size: " + total);
				System.out.print("after checking dependency size: " + count);
				
				double score = count / total;
				org.ssase.util.Logger.logDependencyEnforcement(null, String.valueOf(score));

				if(finalList.size() == 0) {
					System.out.print("No decision that satisfies all dependency, thus use all for requirements check");
				    // We do not return here as we need to give the other class an indication
					// about if there are decisions that satisfy all dependency, hence that they
					// can mutate the final decision to a valid one.
					return new SolutionSet(0);
				}
				
				SolutionSet set = new SolutionSet(finalList.size());
				for(Solution s : finalList) {
					set.add(s);
				}
				
				return set;
			}
			protected void logDependencyAfterEvolution(SolutionSet pareto_front_without_ranking){
				Iterator<Solution> itr = pareto_front_without_ranking.iterator();
				double count = 0;
				double total = pareto_front_without_ranking.size();
				
				
				List<ControlPrimitive> list = Repository.getSortedControlPrimitives(objectives.get(0));
				
				while(itr.hasNext()) {
					Solution s = itr.next();
					
					FEMOSAASolution dummy = new FEMOSAASolution();
					
					Variable[] variables = new Variable[list.size()];
					for (int i = 0; i < list.size(); i ++) {
						variables[i] = new Int(0, list.get(i).getValueVector().length-1);		
					}
					
					dummy.setDecisionVariables(variables);
					boolean isValid = true;
					for (int i = 0; i < list.size(); i ++) {
						int index = ((ZeroAndOneFEMOSAASolution)s).translateToIndexWhenDepdencyInjection(i, false);
						if(index == -1) {
							isValid = false;
							break;
						}
						
						try {
							dummy.getDecisionVariables()[i].setValue(index);
						} catch (JMException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					if(isValid && ((SASSolution)dummy).isSolutionValid()) {
						count++;
					}
				}
				
				logger.debug("All solutions (including dominated ones), before dependency check size: " + total);
				logger.debug("All solutions (including dominated ones), after dependency check size: " + count);
				
				double score = count / total;
				org.ssase.util.Logger.logDependencyEnforcement(null, String.valueOf(score));

			}
		};
	}
	
	protected class ZeroAndOneFEMOSAASolutionInstantiator extends FEMOSAASolutionInstantiator{

		public ZeroAndOneFEMOSAASolutionInstantiator(List<Objective> objectives) {
			super(objectives);
		}
		
		@Override
		public Solution getSolution(Problem problem) {
			FEMOSAASolution sol = null;
			try {
				sol = new ZeroAndOneFEMOSAASolution(problem);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sol.init(objectives, map);
			return sol;
		}

		@Override
		public Solution getSolution(Solution solution) {
			FEMOSAASolution sol = new ZeroAndOneFEMOSAASolution(solution);
			sol.init(objectives, map);
			return sol;
		}

		@Override
		public Solution getSolution() {
			FEMOSAASolution sol = new ZeroAndOneFEMOSAASolution();
			sol.init(objectives, map);
			return sol;
		}

		@Override
		public Solution getSolution(Problem problem, Variable[] variables) {
			FEMOSAASolution sol = null;
			try {
				sol = new ZeroAndOneFEMOSAASolution(problem, variables);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sol.init(objectives, map);
			return sol;
		}

	}
	
	protected class ZeroAndOneFEMOSAASolution extends FEMOSAASolution{
		public ZeroAndOneFEMOSAASolution(Problem problem) throws ClassNotFoundException {
			super(problem);
		}

		public ZeroAndOneFEMOSAASolution(Problem problem, Variable[] variables) throws ClassNotFoundException {
			super(problem, variables);
		}


		public ZeroAndOneFEMOSAASolution(Solution solution) {
			super(solution);
		}

		public ZeroAndOneFEMOSAASolution() {
			super();
		}
		
		
		public double getVariableValueFromIndex(int index) {
			try {
				ControlPrimitive cp = Repository.getSortedControlPrimitives(objectives.get(0)).get(index);
				Map<ControlPrimitive, Tuple<Integer, Integer>> zeroAndOneTupleMap =  Repository.getFeatureModel(objectives.get(0)).getZeroAndOneTupleMap();
				
				Tuple<Integer, Integer> tuple = zeroAndOneTupleMap.get(cp);
				
				List<Integer> list = new ArrayList<Integer>();
				for (int i = tuple.getVal1(); i <= tuple.getVal2(); i++) {
					// Use the first selected one (==1)
					if(super.getDecisionVariables()[i].getValue() == 1) {
						list.add(i - tuple.getVal1());
						//return cp.getValueVector()[ i - tuple.getVal1()];
					}
				}
				
				if(list.size() != 0) {	
			       	 return cp.getValueVector()[new Random().nextInt(list.size())];
					//return cp.getValueVector()[(int)Math.round(list.size() * 0.1)];
				}
				
				// this means no one has been selected so simply return the first one.
				// this could still violate dependency if the first one is not switch off.
				return cp.getValueVector()[0];
				
			//	.getValueVector()[(int)super.getDecisionVariables()[index].getValue()];
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			throw new RuntimeException("Index " + index + " has no entry in the getSortedControlPrimitives");
			
		}
		
		public int translateToIndexWhenDepdencyInjection(int index, boolean isEnsureValidReturn) {
			try {
				ControlPrimitive cp = Repository.getSortedControlPrimitives(objectives.get(0)).get(index);
				Map<ControlPrimitive, Tuple<Integer, Integer>> zeroAndOneTupleMap =  Repository.getFeatureModel(objectives.get(0)).getZeroAndOneTupleMap();
				
				Tuple<Integer, Integer> tuple = zeroAndOneTupleMap.get(cp);
				
				int result = -1;
				List<Integer> list = new ArrayList<Integer>();
				for (int i = tuple.getVal1(); i <= tuple.getVal2(); i++) {
					
					if(super.getDecisionVariables()[i].getValue() == 1) {
						
						if(isEnsureValidReturn) {
							// Use the first selected one (==1)
							//return result = i - tuple.getVal1();
							list.add(i - tuple.getVal1());
						} else {
							// Means more than one has been selected, return -1
							// indicate invalid.
							if(result != -1) {
								return -1;
							}
							
							result = i - tuple.getVal1();
						}
						
					}
				}
				
				if(isEnsureValidReturn && list.size() != 0) {			
					result = list.get(new Random().nextInt(list.size()));
					//result = list.get((int)Math.round(list.size() * 0.1));
				}
				
				
				if(isEnsureValidReturn && result == -1) {
					// return a random one as protented that it is selected.
					// this could still violate dependency if the first one is not switch off.
					
					return 0;//new Random().nextInt(tuple.getVal2() - tuple.getVal1() + 1);
				} else {
					// This might still be -1 if no feature has been selected.
					return result;
				}
			
			//	.getValueVector()[(int)super.getDecisionVariables()[index].getValue()];
			} catch (JMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			throw new RuntimeException("Index " + index + " has no entry in the getSortedControlPrimitives");
			
		}
	}
}
