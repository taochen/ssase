package org.ssase.objective.optimization.femosaa.moead;

import java.io.File;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.moead.MOEAD_main;
import jmetal.metaheuristics.moead.MOEAD_STM_SAS_main;
import jmetal.metaheuristics.moead.Utils;
import jmetal.util.PseudoRandom;

import org.femosaa.core.SAS;
import org.femosaa.core.SASAlgorithmAdaptor;
import org.femosaa.core.SASSolution;
import org.ssase.objective.optimization.femosaa.FEMOSAASolutionAdaptor;
import org.ssase.region.Region;

public class MOEADRegion extends MOEAD_STMwithKAndDRegion {
	

	
	protected SASAlgorithmAdaptor getAlgorithm(){
		return new MOEAD_main(){
			protected SolutionSet filterRequirementsAfterEvolution(SolutionSet pareto_front){			
				//Region.correctDependencyAfterEvolution(pareto_front);
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
			
//			protected void logDependencyAfterEvolution(SolutionSet pareto_front_without_ranking){
//				Region.logDependencyAfterEvolution(pareto_front_without_ranking);
//			}
		};
	}
}
