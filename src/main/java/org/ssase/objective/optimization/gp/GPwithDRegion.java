package org.ssase.objective.optimization.gp;

import jmetal.core.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGA2_SAS_main;

import org.femosaa.core.SASAlgorithmAdaptor;
import org.ssase.objective.optimization.femosaa.FEMOSAASolutionAdaptor;
import org.ssase.region.Region;
import jmetal.metaheuristics.gp.GP_SAS_main;
public class GPwithDRegion extends GPRegion {
	
	protected void init(){
		if(vars == null) {
			vars = FEMOSAASolutionAdaptor.getInstance().convertInitialLimits(objectives.get(0));
			
		}
	}
	
	protected SASAlgorithmAdaptor getAlgorithm(){
		return new GP_SAS_main(){
			protected SolutionSet filterRequirementsAfterEvolution(SolutionSet pareto_front){	
				return Region.filterRequirementsAfterEvolution(pareto_front, objectives);
			}
			protected void printParetoFront(SolutionSet pareto_front) {
				Region.printParetoFront(pareto_front, objectives);
		    }
			
		};
	}
}
