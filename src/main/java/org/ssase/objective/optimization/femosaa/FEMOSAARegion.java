package org.ssase.objective.optimization.femosaa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.moead.MOEAD_SAS_main;
import jmetal.problems.SASAlgorithmAdaptor;
import jmetal.problems.SASSolution;
import jmetal.problems.test.DummySASSolution;
import jmetal.problems.test.DummySASSolutionInstantiator;
import jmetal.util.JMException;

import org.ssase.objective.Objective;
import org.ssase.objective.optimization.moaco.BasicAntColony;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.EnvironmentalPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.region.Region;
import org.ssase.util.Repository;
import org.ssase.util.Tuple;

public class FEMOSAARegion extends Region {

	// The order is the same as Repository.getSortedControlPrimitives(obj)
	protected int[][] vars = null;
	
	public FEMOSAARegion() {
		super();		
	}

	protected void init(){
		if(vars == null) {
			vars = FEMOSAASolutionAdaptor.getInstance().convertInitialLimits(objectives.get(0));
		}
		
	}
	
	
	public LinkedHashMap<ControlPrimitive, Double> optimize() {
		
		init();
		
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


			FEMOSAASolutionInstantiator inst = new FEMOSAASolutionInstantiator(objectives);
			
            SASAlgorithmAdaptor algorithm = getAlgorithm();
			Solution solution = null;
			try {
				solution = algorithm.execute(inst, vars, objectives.size(), 0);		
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			result = FEMOSAASolutionAdaptor.getInstance().convertSolution(solution/*Use the first one, as the list should all be knee points*/
					,objectives.get(0));
			print(result);

			isLocked = false;
			lock.notifyAll();
		}
		System.out.print("================= Finish optimization ! =================\n");
		// TODO optimization.
		return result;
	}
	
	protected SASAlgorithmAdaptor getAlgorithm(){
		return new MOEAD_SAS_main(){
			protected SolutionSet filterRequirementsAfterEvolution(SolutionSet pareto_front){			
				//Region.correctDependencyAfterEvolution(pareto_front);
				//return Region.filterRequirementsAfterEvolution(pareto_front, objectives);
				return pareto_front;
			}
			protected SolutionSet correctDependencyAfterEvolution(
					SolutionSet pareto_front) {
				return Region.correctDependencyAfterEvolution(pareto_front);
			}
			protected void printParetoFront(SolutionSet pareto_front) {
				Region.printParetoFront(pareto_front, objectives);
		    }
			
//			protected void logDependencyAfterEvolution(SolutionSet pareto_front_without_ranking){
//				Region.logDependencyAfterEvolution(pareto_front_without_ranking);
//			}
		};
	}
}
