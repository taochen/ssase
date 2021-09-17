package org.ssase.objective.optimization.femosaa.nsgaii;

import java.io.File;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.nsgaII.NSGA2_SAS_main;
import jmetal.metaheuristics.nsgaII.Utils;
import jmetal.util.PseudoRandom;

import org.femosaa.core.SAS;
import org.femosaa.core.SASAlgorithmAdaptor;
import org.femosaa.core.SASSolution;
import org.ssase.objective.optimization.femosaa.FEMOSAASolutionAdaptor;
import org.ssase.region.Region;

public class NSGAIIwithKAndDRegion extends NSGAIIRegion {

	private int index = -1;

	public NSGAIIwithKAndDRegion() {

	}

	public NSGAIIwithKAndDRegion(int index) {
		this.index = index;
	}

	protected void init() {
		if (vars == null) {
			vars = FEMOSAASolutionAdaptor.getInstance().convertInitialLimits(objectives.get(0));

		}
	}

	protected SASAlgorithmAdaptor getAlgorithm() {
		return new NSGA2_SAS_main() {
			protected SolutionSet filterRequirementsAfterEvolution(SolutionSet pareto_front) {
				return Region.filterRequirementsAfterEvolution(pareto_front, objectives);
			}

			protected SolutionSet correctDependencyAfterEvolution(SolutionSet pareto_front) {
				return Region.correctDependencyAfterEvolution(pareto_front);
			}

			@Override
			protected Solution findSoleSolutionAfterEvolution(SolutionSet pareto_front) {
				if (index < 0) {
					// find the knee point
					Solution individual = ((jmetal.metaheuristics.nsgaII.NSGAII_SAS) algorithm)
							.kneeSelection(pareto_front);

					for (int i = 0; i < problem.getNumberOfObjectives(); i++)
						System.out.print(individual.getObjective(i) + "\n");

					String str = "data/NSGAII/SAS";
					if (SAS.isTest)
						Utils.deleteFolder(new File(str + "/knee_results.dat"));
					SolutionSet set = new SolutionSet(1);
					set.add(individual);
					if (SAS.isTest)
						set.printObjectivesToFile(str + "/knee_results.dat");

					return individual;

				} else {

					Solution best = null;
					for (int i = 0; i < pareto_front.size(); i++) {
						System.out.print("final " + pareto_front.get(i).getObjective(index) + "***\n");
						if (best == null || pareto_front.get(i).getObjective(index) < best.getObjective(index)) {
							best = pareto_front.get(i);
						}
					}

					//System.out.print("best " + best.getObjective(index) + "***\n");
					return best;
				}
			}

			protected void printParetoFront(SolutionSet pareto_front) {
				Region.printParetoFront(pareto_front, objectives);
			}

		};
	}
}
