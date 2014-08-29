package org.ssascaling.region;

import org.ssascaling.Service;
import org.ssascaling.objective.Cost;
import org.ssascaling.objective.Objective;
import org.ssascaling.objective.correlation.Spearmans;
import org.ssascaling.objective.optimization.BasicAntColony;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.EnvironmentalPrimitive;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.util.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


/**
 * Create new instance whenever region distribution change.
 * @author tao
 *
 */
public class Region {

	// private Map<Service, List<Objective>> serviceMap;

	protected List<Objective> objectives;

	protected Object lock = new Object();

	protected boolean isLocked = false;

	protected int waitingUpdateCounter = 0;

	protected int finishedUpdateCounter = 0;
	
	public Region () {
		this.objectives = new ArrayList<Objective>();
	}
	
	
	/**
	 * This should only be used when initializing the region.
	 * @param obj
	 */
	public void addObjective (Objective obj) {
		objectives.add(obj);
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
			
			for (Primitive p : primitives) {
				   System.out.print("The CP for optimization: " + p.getAlias() + " : " + p.getName() + "\n");
			}
			
			
			BasicAntColony aco = new BasicAntColony(new Random().nextInt(),
					primitives, objectiveMap, objectiveMap);
			result = aco.doOptimization();

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
	
	private void print(LinkedHashMap<ControlPrimitive, Double> result){
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
			
			EnvironmentalPrimitive ep = obj instanceof org.ssascaling.qos.QualityOfService? 
					((org.ssascaling.qos.QualityOfService)obj).getEP() : null;

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
