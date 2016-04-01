package org.ssase.region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jmetal.core.SolutionSet;
import jmetal.metaheuristics.moead.MOEAD_SAS_main;
import jmetal.problems.SASAlgorithmAdaptor;
import jmetal.problems.test.DummySASSolution;
import jmetal.problems.test.DummySASSolutionInstantiator;
import jmetal.util.JMException;

import org.ssase.objective.Objective;
import org.ssase.objective.optimization.BasicAntColony;
import org.ssase.objective.optimization.adaptor.MOEASolutionAdaptor;
import org.ssase.objective.optimization.adaptor.MOEASolutionInstantiator;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.util.Repository;
import org.ssase.util.Tuple;

public class MOEARegion extends Region {

	// The order is the same as Repository.getSortedControlPrimitives(obj)
	private int[][] vars = null;
	
	public MOEARegion() {
		super();
		vars = MOEASolutionAdaptor.getInstance().convertInitialLimits(objectives.get(0));
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


			MOEASolutionInstantiator inst = new MOEASolutionInstantiator(objectives);
			
            SASAlgorithmAdaptor algorithm = getAlgorithm();
			SolutionSet set = null;
			try {
				set = algorithm.execute(inst, vars, objectives.size(), 0);		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			result = MOEASolutionAdaptor.getInstance().convertSolution(set.get(0)/*Use the first one, as the list should all be knee points*/
					,objectives.get(0));
			print(result);

			isLocked = false;
			lock.notifyAll();
		}
		System.out.print("================= Finish optimization ! =================\n");
		// TODO optimization.
		return result;
	}
	
	private SASAlgorithmAdaptor getAlgorithm(){
		return new MOEAD_SAS_main();
	}
}
