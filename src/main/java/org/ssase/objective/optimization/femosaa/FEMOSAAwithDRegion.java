package org.ssase.objective.optimization.femosaa;

import java.io.File;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.moead.MOEAD_SAS_PLAIN_main;
import jmetal.metaheuristics.moead.MOEAD_STM_SAS;
import jmetal.metaheuristics.moead.MOEAD_STM_SAS_STATIC;
import jmetal.metaheuristics.moead.Utils;
import jmetal.problems.SAS;
import jmetal.problems.SASAlgorithmAdaptor;
import jmetal.util.PseudoRandom;

import org.ssase.region.Region;

public class FEMOSAAwithDRegion extends FEMOSAARegion {
	protected SASAlgorithmAdaptor getAlgorithm(){
		return new MOEAD_SAS_PLAIN_main(){
			protected SolutionSet filterRequirementsAfterEvolution(SolutionSet pareto_front){			
				//Region.correctDependencyAfterEvolution(pareto_front);
				return Region.filterRequirementsAfterEvolution(pareto_front, objectives);
			}
			protected void printParetoFront(SolutionSet pareto_front) {
				Region.printParetoFront(pareto_front, objectives);
		    }
		};
	}
}
