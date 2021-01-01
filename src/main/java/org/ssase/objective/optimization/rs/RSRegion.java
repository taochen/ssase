package org.ssase.objective.optimization.rs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.hc.HC_SAS_main;
import jmetal.metaheuristics.moead.MOEAD_STM_SAS_main;
import jmetal.metaheuristics.nsgaII.NSGA2_SAS_main;
import jmetal.metaheuristics.rs.RS_SAS_main;

import org.femosaa.core.SASAlgorithmAdaptor;
import org.femosaa.core.SASSolution;
import org.ssase.objective.optimization.femosaa.FEMOSAASolutionAdaptor;
import org.ssase.objective.optimization.femosaa.FEMOSAASolutionInstantiator;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.region.Region;


/**
 * This is a random search, uniformly searching over the variable vector.
 * @author tao
 *
 */
public class RSRegion extends Region {

	protected int[][] vars = null;
	protected double[] weights;
	protected double[][] fixed_bounds;
	
	public RSRegion() {
		super();		
	}
	
	public RSRegion(double[] weights) {
		this.weights = weights;	
	}
	
	
	public RSRegion(double[] weights, double[][] fixed_bounds) {
		this.weights = weights;	
		this.fixed_bounds = fixed_bounds;
	}
	


	protected void init(){
		if(vars == null) {
			vars = FEMOSAASolutionAdaptor.getInstance().convertInitialLimits(objectives.get(0));
			// This is needed for approach that do not consider categorical/numeric dependency
			// in the optimization process.
			SASSolution.clearAndStoreForValidationOnly();
		}
	}
	
	
	public LinkedHashMap<ControlPrimitive, Double> optimize() {
		
		init();
		System.out.print("Algorithm entering *******\n");
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
			inst.setWeights(weights);
			inst.setFixedBounds(fixed_bounds);
			System.out.print("Algorithm start *******\n");
            SASAlgorithmAdaptor algorithm = getAlgorithm();
			Solution solution = null;
			try {
				solution = algorithm.execute(inst, vars, objectives.size(), 0);		
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.print("Algorithm end *******\n");
			System.out.print("Convertion start *******\n");
			result = FEMOSAASolutionAdaptor.getInstance().convertSolution(solution/*Use the first one, as the list should all be knee points*/
					,objectives.get(0));
			System.out.print("Convertion end *******\n");
			print(result);

			isLocked = false;
			lock.notifyAll();
		}
		System.out.print("================= Finish optimization ! =================\n");
		// TODO optimization.
		return result;
	}
	
	protected SASAlgorithmAdaptor getAlgorithm(){
		return new RS_SAS_main(){
			protected SolutionSet filterRequirementsAfterEvolution(SolutionSet pareto_front){
		
				return Region.filterRequirementsAfterEvolution(pareto_front, objectives);
				//return pareto_front;
			}
			protected SolutionSet correctDependencyAfterEvolution(
					SolutionSet pareto_front) {
				return Region.correctDependencyAfterEvolution(pareto_front);
			}
			protected void printParetoFront(SolutionSet pareto_front) {
				Region.printParetoFront(pareto_front, objectives);
		    }
		};
	}

}