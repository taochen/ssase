package org.ssase.objective.optimization.moaco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ssase.objective.Objective;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.region.Region;
import org.ssase.util.Tuple;

public class MOACORegion extends Region{

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
	
}
