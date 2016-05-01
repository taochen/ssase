package org.ssase.objective.optimization.femosaa;

import jmetal.core.SolutionSet;
import jmetal.metaheuristics.moead.MOEAD_SAS_PLAIN_main;
import jmetal.problems.SASAlgorithmAdaptor;

import org.ssase.region.Region;

public class FEMOSAAwithDRegion extends FEMOSAARegion {
	protected SASAlgorithmAdaptor getAlgorithm(){
		return new MOEAD_SAS_PLAIN_main(){
			protected SolutionSet filterRequirementsAfterEvolution(SolutionSet pareto_front){			
				//Region.correctDependencyAfterEvolution(pareto_front);
				return Region.filterRequirementsAfterEvolution(pareto_front, objectives);
			}
		};
	}
}
