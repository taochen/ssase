package org.ssase.region;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssase.objective.Objective;
import org.ssase.objective.optimization.bb.BranchAndBoundRegion;
import org.ssase.objective.optimization.bb.BranchAndBoundwithDRegion;
import org.ssase.objective.optimization.femosaa.*;
import org.ssase.objective.optimization.gp.*;
import org.ssase.objective.optimization.moaco.MOACORegion;
import org.ssase.objective.optimization.moga.MOGARegion;
import org.ssase.objective.optimization.nsgaii.*;
import org.ssase.objective.optimization.random.HillClimbingRegion;
import org.ssase.objective.optimization.random.HillClimbingwithDRegion;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.util.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.problems.SASSolution;
import jmetal.util.JMException;


/**
 * Create new instance whenever region distribution change.
 * @author tao
 *
 */
public abstract class Region {

	protected static final Logger logger = LoggerFactory
	.getLogger(Region.class);
	// private Map<Service, List<Objective>> serviceMap;

	protected List<Objective> objectives;

	protected Object lock = new Object();

	protected boolean isLocked = false;

	protected int waitingUpdateCounter = 0;

	protected int finishedUpdateCounter = 0;
	
	public static OptimizationType selected;

	public static void setSelectedOptimizationType(String type){
		if(type == null) throw new RuntimeException("No proper OptimizationType found!");
		
		type = type.trim();
		
		if("init".equals(type)) {
			selected = OptimizationType.INIT;
		} else if("random".equals(type)) {
			selected = OptimizationType.RANDOM;
		} else if("moaco".equals(type)) {
			selected = OptimizationType.MOACO;
		} else if("moga".equals(type)) {
			selected = OptimizationType.MOGA;
		} else if("femosaa".equals(type)) {
			selected = OptimizationType.FEMOSAA;
		} else if("femosaa-k".equals(type)) {
			selected = OptimizationType.FEMOSAAk;
		} else if("femosaa-d".equals(type)) {
			selected = OptimizationType.FEMOSAAd;
		} else if("femosaa-nothing".equals(type)) {
			selected = OptimizationType.FEMOSAAnothing;
		} else if("femosaa-01".equals(type)) {
			selected = OptimizationType.FEMOSAA01;
		} else if("nsgaii".equals(type)) {
			selected = OptimizationType.NSGAII;
		} else if("nsgaii-k-d".equals(type)) {
			selected = OptimizationType.NSGAIIkd;
		}  else if("gp".equals(type)) {
			selected = OptimizationType.GP;
		} else if("gp-k-d".equals(type)) {
			selected = OptimizationType.GPkd;
		} else if("random-d".equals(type)) {
			selected = OptimizationType.RANDOMd;
		} else if("bb".equals(type)) {
			selected = OptimizationType.BB;
		} else if("bb-d".equals(type)) {
			selected = OptimizationType.BBd;
		}
		
		
		if(selected == null) throw new RuntimeException("Can not find region for type " + type);
		
		Repository.centralizedOptimizationConfiguration(selected);
	}

	public static Region getNewRegionInstanceByType (OptimizationType type) {
		
		if (type == null) throw new RuntimeException("No proper OptimizationType found!");
		
		if(OptimizationType.INIT.equals(type)) {
			return new InitRegion();
		} else if(OptimizationType.RANDOM.equals(type)) {
			return new HillClimbingRegion();
		} else if(OptimizationType.MOACO.equals(type)) {
			return new MOACORegion();
		} else if(OptimizationType.MOGA.equals(type)) {
			return new MOGARegion();
		} else if(OptimizationType.FEMOSAA.equals(type)) {
			return new FEMOSAARegion();
		}  else if(OptimizationType.FEMOSAAk.equals(type)) {
			return new FEMOSAAwithKRegion();
		}  else if(OptimizationType.FEMOSAAd.equals(type)) {
			return new FEMOSAAwithDRegion();
		}  else if(OptimizationType.FEMOSAAnothing.equals(type)) {
			return new FEMOSAAwithNothingRegion();
		} else if(OptimizationType.FEMOSAA01.equals(type)) {
			return new FEMOSAAwithZeroAndOneRegion();
		} else if(OptimizationType.NSGAII.equals(type)) {
			return new NSGAIIRegion();
		} else if(OptimizationType.NSGAIIkd.equals(type)) {
			return new NSGAIIwithKAndDRegion();
		} else if(OptimizationType.GP.equals(type)) {
			return new GPRegion();
		} else if(OptimizationType.GPkd.equals(type)) {
			return new GPwithDRegion();
		} else if(OptimizationType.RANDOMd.equals(type)) {
			return new HillClimbingwithDRegion();
		} else if(OptimizationType.BB.equals(type)) {
			return new BranchAndBoundRegion();
		} else if(OptimizationType.BBd.equals(type)) {
			return new BranchAndBoundwithDRegion();
		}
		
		throw new RuntimeException("Can not find region for type " + type);
		
	}
	
	
	
	protected Region () {
		this.objectives = new ArrayList<Objective>();
	}
	
	
	/**
	 * This should only be used when initializing the region.
	 * @param obj
	 */
	public void addObjective (Objective obj) {
		//if(objectives.contains(obj)) {
			//return;
		//}
		objectives.add(obj);
	}
	
	
	public void addObjectives (Collection<Objective> objs) {
		objectives.addAll(objs);
	}

	/**
	 * In case it is doing objective reduction, update should not be allowed.
	 * 
	 * 
	 
	 * Used by the monitors, before the model training.
	 * 
	 * @return
	 */
	@Deprecated
	public void isCanUpdateQoSMeasurement() {

		synchronized (lock) {
			while (isLocked) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
			waitingUpdateCounter++;
			finishedUpdateCounter++;
		}
	}

	/**
	 * Used by the monitors, after the model training.
	 */
	@Deprecated
	public void updateCounter() {
		synchronized (lock) {
			finishedUpdateCounter--;
			if (waitingUpdateCounter == objectives.size()
					&& finishedUpdateCounter == 0) {
				waitingUpdateCounter = 0;
				lock.notifyAll();
			}
		}

	}

	public abstract LinkedHashMap<ControlPrimitive, Double> optimize();
	
	public static SolutionSet correctDependencyAfterEvolution(
			SolutionSet pareto_front) {
		Iterator<Solution> itr = pareto_front.iterator();
		double count = 0;
		double total = pareto_front.size();
		
		List<Solution> list = new ArrayList<Solution>();
		
		
		while(itr.hasNext()) {
			Solution s = itr.next();
			if(((SASSolution)s).isSolutionValid()) {
				count++;
				list.add(s);
			}
		}
		
		System.out.print("before checking dependency size: " + total + "\n");
		System.out.print("after checking dependency size: " + count + "\n");
		
		double score = count / total;
		org.ssase.util.Logger.logDependencyEnforcement(null, String.valueOf(score));

		if(list.size() == 0) {
			System.out.print("No decision that satisfies all dependency, thus use all for requirements check\n");
		    // We do not return here as we need to give the other class an indication
			// about if there are decisions that satisfy all dependency, hence that they
			// can mutate the final decision to a valid one.
			return new SolutionSet(0);
		}
		
		SolutionSet set = new SolutionSet(list.size());
		for(Solution s : list) {
			set.add(s);
		}
		
		return set;
	}

//	public static void logDependencyAfterEvolution(
//			SolutionSet pareto_front_without_ranking) {
//		Iterator<Solution> itr = pareto_front_without_ranking.iterator();
//		double count = 0;
//		double total = pareto_front_without_ranking.size();
//		
//		
//		while(itr.hasNext()) {
//			Solution s = itr.next();
//			if(((SASSolution)s).isSolutionValid()) {
//				count++;
//			}
//		}
//		
//		System.out.print("All solutions (including dominated ones), before dependency check size: " + total);
//		System.out.print("All solutions (including dominated ones), after dependency check size: " + count);
//		
//		double score = count / total;
//		org.ssase.util.Logger.logDependencyEnforcement(null, String.valueOf(score));
//
//		
//	}
	
	public static void logDependencyForFinalSolution(
			Solution solution) {		
		
		if(((SASSolution)solution).isSolutionValid()) {
		    org.ssase.util.Logger.logDependencyEnforcementFinal(null, String.valueOf(1));
		} else {
			org.ssase.util.Logger.logDependencyEnforcementFinal(null, String.valueOf(0));
		}

		
	}
	
	public static SolutionSet filterRequirementsAfterEvolution(
			SolutionSet pareto_front,  List<Objective> objectives) {
		Iterator<Solution> itr = pareto_front.iterator();
		
		
		List<Solution> list = new ArrayList<Solution>();
		
		System.out.print("Decisions for checking requirements: " + pareto_front.size() + "\n");
		while(itr.hasNext()) {
			Solution s = itr.next();
			
//			if (logger.isDebugEnabled() && s instanceof FEMOSAASolution) {
//
//				List<ControlPrimitive> cps = Repository
//						.getSortedControlPrimitives(objectives.get(0));
//				String r = "";
//				for (int i = 0; i < s.getDecisionVariables().length; i++) {
//					try {
//						r = r
//								+ cps.get(i).getName()
//								+ "="
//								+ cps.get(i).getValueVector()[(int) s
//										.getDecisionVariables()[i].getValue()]
//								+ " ";
//					} catch (JMException e) {
//						e.printStackTrace();
//					}
//				}
//				System.out.print("Decision: " + r);
//			}
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
		

		System.out.print("Number of decisions that satisfies all requirements: " + list.size() + "\n");
		// If no satisfied solutions, return all as default.
		if(list.size() == 0) {
			System.out.print("No decision that satisfies all requirements, thus return all decisions found\n");
			return pareto_front;
		}
		
		SolutionSet set = new SolutionSet(list.size());
		for(Solution s : list) {
			set.add(s);
		}
		
		return set;
	}
	
	public static void printParetoFront(SolutionSet pareto_front,
			List<Objective> objectives) {
		System.out.print("Pareto front size: " + pareto_front.size() + "\n");

		for (int k = 0; k < pareto_front.size(); k++) {
			Solution s = pareto_front.get(k);

			if (/*logger.isDebugEnabled() && */s instanceof FEMOSAASolution) {
				List<ControlPrimitive> cps = Repository
						.getSortedControlPrimitives(objectives.get(0));
				String r = "";
				for (int i = 0; i < s.getDecisionVariables().length; i++) {
					try {
						r = r
								+ cps.get(i).getName()
								+ "="
								+ cps.get(i).getValueVector()[(int) s
										.getDecisionVariables()[i].getValue()]
								+ " ";
					} catch (JMException e) {
						e.printStackTrace();
					}
				}
				System.out.print("Decision: " + r  + "\n");
			}
		}
	}
	
	public void print(){
		for (Objective obj : objectives) {
			
			
			
			System.out.print("It has " + objectives.size() + " objectives, contain "+ obj.getName() + "\n");
			System.out.print(" ========= Contain CP start ========== \n");
			for (Primitive p : obj.getPrimitivesInput()) {
				System.out.print("CP "+ p.getAlias() + " : " + p.getName() + "\n");
			}
			
			System.out.print(" ========= Contain CP end ========== \n");
		}
	}
	
	protected void print(LinkedHashMap<ControlPrimitive, Double> result){
		if (result == null) {
			return;
		}
		
		
		for (Map.Entry<ControlPrimitive, Double> e : result.entrySet()) {
			   System.out.print(e.getKey().getAlias() + ", " + e.getKey().getName() + ", value: " + e.getValue() +  "\n");
		}
		int violated = 0;
		for (Objective obj : objectives) {
			double[] xValue = new double[obj.getPrimitivesInput().size()];
			for (int i = 0; i < xValue.length; i++) {
				
				if (obj.getPrimitivesInput().get(i) instanceof ControlPrimitive) {
					xValue[i] = result.get(obj.getPrimitivesInput().get(i));
				} else {
					xValue[i] = obj.getPrimitivesInput().get(i).getProvision();
				}
				
				 
			}
			
			double adapt = obj.predict(xValue);
			
			String out = "";
			
			EnvironmentalPrimitive ep = obj instanceof org.ssase.objective.QualityOfService? 
					((org.ssase.objective.QualityOfService)obj).getEP() : null;

			if (ep != null) {
				// If make no sense if the required throughput even larger than the
				// current workload.
				if (obj.isMin() ? obj.getConstraint() < ep.getLatest() : obj.getConstraint() > ep
						.getLatest()) {
					
				} else if (obj.isMin()? adapt > obj.getConstraint() : adapt < obj.getConstraint() )  {
					out += "!!!! Violated " + obj.getConstraint()  + ", EP: " + ep.getLatest() + " - ";
					violated++;
				}

			} else if (obj.isMin()? adapt > obj.getConstraint() : adapt < obj.getConstraint() ) {
				out += "!!!! Violated " + obj.getConstraint()  + " - ";
				violated++;
			}
			
			System.out.print(out + obj.getName() + " current value: " + obj.getCurrentPrediction() + " - after adaptation: " + adapt + "\n");
		}
		System.out.print("Total number of objectives: " + objectives.size() + "\n");
		System.out.print("Total number of violated objectives: " + violated + "\n");
		
	}

}
