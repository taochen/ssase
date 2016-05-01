package org.ssase.objective.optimization.nsgaii;

import jmetal.core.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGA2_SAS_main;
import jmetal.problems.SASAlgorithmAdaptor;
import jmetal.problems.SASSolution;

import org.ssase.objective.optimization.femosaa.FEMOSAASolutionAdaptor;
import org.ssase.region.Region;

public class NSGAIIwithKAndDRegion extends NSGAIIRegion {
	
	protected void init(){
		if(vars == null) {
			vars = FEMOSAASolutionAdaptor.getInstance().convertInitialLimits(objectives.get(0));
			
		}
	}
	
	protected SASAlgorithmAdaptor getAlgorithm(){
		return new NSGA2_SAS_main(){
			protected SolutionSet filterRequirementsAfterEvolution(SolutionSet pareto_front){		
				return Region.filterRequirementsAfterEvolution(pareto_front, objectives);
			}
		
		};
	}
}
