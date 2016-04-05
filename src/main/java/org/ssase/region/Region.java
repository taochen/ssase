package org.ssase.region;

import org.ssase.objective.Objective;
import org.ssase.objective.optimization.femosaa.FEMOSAARegion;
import org.ssase.objective.optimization.moaco.MOACORegion;
import org.ssase.objective.optimization.moga.MOGARegion;
import org.ssase.objective.optimization.random.HillClimbingRegion;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Create new instance whenever region distribution change.
 * @author tao
 *
 */
public abstract class Region {

	// private Map<Service, List<Objective>> serviceMap;

	protected List<Objective> objectives;

	protected Object lock = new Object();

	protected boolean isLocked = false;

	protected int waitingUpdateCounter = 0;

	protected int finishedUpdateCounter = 0;
	
	public static final OptimizationType selected = OptimizationType.INIT;


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
