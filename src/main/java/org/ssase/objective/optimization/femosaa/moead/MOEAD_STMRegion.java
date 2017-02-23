package org.ssase.objective.optimization.femosaa.moead;

import java.io.File;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.moead.MOEAD_SAS_PLAIN_main;
import jmetal.metaheuristics.moead.Utils;
import jmetal.problems.SAS;
import jmetal.problems.SASAlgorithmAdaptor;
import jmetal.problems.SASSolution;
import jmetal.util.PseudoRandom;

import org.ssase.objective.optimization.femosaa.FEMOSAASolutionAdaptor;
import org.ssase.region.Region;

public class MOEAD_STMRegion extends MOEAD_STMwithKAndDRegion {
	
	protected void init(){
		if(vars == null) {
			vars = FEMOSAASolutionAdaptor.getInstance().convertInitialLimits(objectives.get(0));
			// This is needed for approach that do not consider categorical/numeric dependency
			// in the optimization process.
			SASSolution.clearAndStoreForValidationOnly();
		}
	}
	
	protected SASAlgorithmAdaptor getAlgorithm(){
		return new MOEAD_SAS_PLAIN_main(){
			protected SolutionSet filterRequirementsAfterEvolution(SolutionSet pareto_front){			
				//Region.correctDependencyAfterEvolution(pareto_front);
				return Region.filterRequirementsAfterEvolution(pareto_front, objectives);
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
