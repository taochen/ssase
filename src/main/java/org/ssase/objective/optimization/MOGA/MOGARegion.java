package org.ssase.objective.optimization.moga;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ssase.objective.Objective;
import org.ssase.objective.optimization.moaco.BasicAntColony;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.region.Region;
import org.ssase.util.Tuple;

public class MOGARegion extends Region {
	// private Map<Service, List<Objective>> serviceMap;

	protected List<Objective> objectives;

	protected Object lock = new Object();

	protected boolean isLocked = false;

	protected int waitingUpdateCounter = 0;

	protected int finishedUpdateCounter = 0;
	
	
	public MOGARegion () {
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

			// TODO reduction

			List<ControlPrimitive> primitives = new ArrayList<ControlPrimitive>();
			LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> objectiveMap = new LinkedHashMap<Objective, List<Tuple<Primitive, Double>>>();

			Map<Primitive, Tuple<Primitive, Double>> set = new HashMap<Primitive, Tuple<Primitive, Double>>();

			for (Objective obj : objectives) {

				objectiveMap
						.put(obj, new ArrayList<Tuple<Primitive, Double>>());

				for (Primitive p : obj.getPrimitivesInput()) {

					if (!set.containsKey(p)) {

						set.put(p,
								new Tuple<Primitive, Double>(p,
										(p instanceof ControlPrimitive) ? p
												.getProvision()
												: ((EnvironmentalPrimitive) p)
														.getLatest()));
					}
					if (p instanceof ControlPrimitive) {
						if (!primitives.contains(p)) {
							primitives.add((ControlPrimitive) p);
						}
					}

					objectiveMap.get(obj).add(set.get(p));
				}
			}
			
			/*for (Primitive p : primitives) {
				   System.out.print("The CP for optimization: " + p.getAlias() + " : " + p.getName() + "\n");
			}*/
			
			
			NSGAII ga = new NSGAII(objectiveMap);
			result = ga.doOptimization();

			print(result);

			isLocked = false;
			lock.notifyAll();
		}
		System.out.print("================= Finish optimization ! =================\n");
		// TODO optimization.
		return result;
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
	
	

}
