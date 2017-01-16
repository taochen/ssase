package org.ssase.objective.optimization.femosaa.ibea;

import java.io.File;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.ibea.IBEA_SAS_main;
import jmetal.metaheuristics.nsgaII.Utils;
import jmetal.problems.SAS;
import jmetal.problems.SASAlgorithmAdaptor;

import org.ssase.region.Region;

public class IBEAwithKRegion extends IBEARegion{

	
	protected SASAlgorithmAdaptor getAlgorithm(){
		return new IBEA_SAS_main(){
			protected SolutionSet filterRequirementsAfterEvolution(SolutionSet pareto_front){		
				return Region.filterRequirementsAfterEvolution(pareto_front, objectives);
			}
			protected SolutionSet correctDependencyAfterEvolution(
					SolutionSet pareto_front) {
				return Region.correctDependencyAfterEvolution(pareto_front);
			}
			@Override
			protected Solution findSoleSolutionAfterEvolution(SolutionSet pareto_front) {
				// find the knee point
				Solution individual = ((jmetal.metaheuristics.ibea.IBEA_SAS)algorithm).kneeSelection(pareto_front);
					
				
				for (int i = 0; i < problem.getNumberOfObjectives(); i++)
					System.out.print(individual.getObjective(i) + "\n");
				
				
				String str = "data/IBEA/SAS";
				if(SAS.isTest) 
				Utils.deleteFolder(new File(str+ "/knee_results.dat"));
				SolutionSet set = new SolutionSet(1);
				set.add(individual);
				if(SAS.isTest) 
				set.printObjectivesToFile(str + "/knee_results.dat");
				
				return individual;
			}
			protected void printParetoFront(SolutionSet pareto_front) {
				Region.printParetoFront(pareto_front, objectives);
		    }

		};
	}
}
