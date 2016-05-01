package org.ssase.objective.optimization.femosaa;

import jmetal.core.SolutionSet;
import jmetal.metaheuristics.moead.MOEAD_SAS_main;
import jmetal.problems.SASAlgorithmAdaptor;
import jmetal.problems.SASSolution;

import org.ssase.region.Region;

public class FEMOSAAwithKRegion extends FEMOSAARegion {
	
	protected void init(){
		if(vars == null) {
			vars = FEMOSAASolutionAdaptor.getInstance().convertInitialLimits(objectives.get(0));
			// This is needed for approach that do not consider categorical/numeric dependency
			// in the optimization process.
			SASSolution.clearAndStoreForValidationOnly();
		}
	}
	
	protected SASAlgorithmAdaptor getAlgorithm(){
		return new MOEAD_SAS_main(){
			protected SolutionSet filterRequirementsAfterEvolution(SolutionSet pareto_front){			
				//Region.correctDependencyAfterEvolution(pareto_front);
				return Region.filterRequirementsAfterEvolution(pareto_front, objectives);
			}
			protected SolutionSet correctDependencyAfterEvolution(
					SolutionSet pareto_front) {
				return Region.correctDependencyAfterEvolution(pareto_front);
			}
			protected void logDependencyAfterEvolution(SolutionSet pareto_front_without_ranking){
				Region.logDependencyAfterEvolution(pareto_front_without_ranking);
			}
		};
	}
}
