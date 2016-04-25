package org.ssase.region;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssase.objective.Objective;
import org.ssase.objective.optimization.femosaa.FEMOSAARegion;
import org.ssase.objective.optimization.moaco.MOACORegion;
import org.ssase.objective.optimization.moga.MOGARegion;
import org.ssase.objective.optimization.nsgaii.NSGAIIRegion;
import org.ssase.objective.optimization.random.HillClimbingRegion;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.problems.SASSolution;


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
	
	public static OptimizationType selected = OptimizationType.INIT;

	public static void setSelectedOptimizationType(String type){
		if(type == null) return;
		
		
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
		} else if("nsgaii".equals(type)) {
			selected = OptimizationType.NSGAII;
		}
	}

	public static Region getNewRegionInstanceByType (OptimizationType type) {
		
		if (type == null) return new InitRegion();
		
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
		} else if(OptimizationType.NSGAII.equals(type)) {
			return new NSGAIIRegion();
		}
		
		return new InitRegion();
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
		
		double score = count / total;
		org.ssase.util.Logger.logDependencyEnforcement(null, String.valueOf(score));

		SolutionSet set = new SolutionSet(list.size());
		for(Solution s : list) {
			set.add(s);
		}
		
		return set;
	}

	
	public static SolutionSet filterRequirementsAfterEvolution(
			SolutionSet pareto_front,  List<Objective> objectives) {
		Iterator<Solution> itr = pareto_front.iterator();
		
		
		List<Solution> list = new ArrayList<Solution>();
		
		
		while(itr.hasNext()) {
			Solution s = itr.next();
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
		

		
		// If no satisfied solutions, return all as default.
		if(list.size() == 0) {
			return pareto_front;
		}
		
		SolutionSet set = new SolutionSet(list.size());
		for(Solution s : list) {
			set.add(s);
		}
		
		return set;
	}
	
	public void print(){
		for (Objective obj : objectives) {
			
			
			
			logger.debug("It has " + objectives.size() + " objectives, contain "+ obj.getName() + "\n");
			logger.debug(" ========= Contain CP start ========== \n");
			for (Primitive p : obj.getPrimitivesInput()) {
				logger.debug("CP "+ p.getAlias() + " : " + p.getName() + "\n");
			}
			
			logger.debug(" ========= Contain CP end ========== \n");
		}
	}
	
	protected void print(LinkedHashMap<ControlPrimitive, Double> result){
		if (result == null) {
			return;
		}
		
		
		for (Map.Entry<ControlPrimitive, Double> e : result.entrySet()) {
			   logger.debug(e.getKey().getAlias() + ", " + e.getKey().getName() + ", value: " + e.getValue() +  "\n");
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
			
			logger.debug(out + obj.getName() + " current value: " + obj.getCurrentPrediction() + " - after adaptation: " + adapt + "\n");
		}
		logger.debug("Total number of objectives: " + objectives.size() + "\n");
		logger.debug("Total number of violated objectives: " + violated + "\n");
		
	}

}
