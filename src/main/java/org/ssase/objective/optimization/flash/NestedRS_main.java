package org.ssase.objective.optimization.flash;

/**
 * GP_SAS_main.java
 * 
 * @author Ke Li <keli.genius@gmail.com>
 * 
 * Copyright (c) 2016 Ke Li
 * 
 * Note: This is a free software developed based on the open source project 
 * jMetal<http://jmetal.sourceforge.net>. The copy right of jMetal belongs to 
 * its original authors, Antonio J. Nebro and Juan J. Durillo. Nevertheless, 
 * this current version can be redistributed and/or modified under the terms of 
 * the GNU Lesser General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */


import jmetal.core.*;
import jmetal.metaheuristics.nsgaII.NSGAII_SAS;
import jmetal.operators.crossover.*;
import jmetal.operators.mutation.*;
import jmetal.operators.selection.*;
import jmetal.problems.*;
import jmetal.problems.DTLZ.*;
import jmetal.problems.ZDT.*;
import jmetal.problems.cec2009Competition.*;
import jmetal.problems.WFG.*;

import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.femosaa.core.EAConfigure;
import org.femosaa.core.SAS;
import org.femosaa.core.SASAlgorithmAdaptor;
import org.femosaa.core.SASProblemFactory;
import org.femosaa.core.SASSolutionInstantiator;
import org.femosaa.seed.FixedSeeder;

public class NestedRS_main extends SASAlgorithmAdaptor{
	public static Logger logger_; // Logger object
	public static FileHandler fileHandler_; // FileHandler object

	Problem problem; // The problem to solve
	Algorithm algorithm; // The algorithm to use
	Operator crossover; // Crossover operator
	Operator mutation; // Mutation operator
	
	/**
	 * @param args
	 *            Command line arguments. The first (optional) argument
	 *            specifies the problem to solve.
	 * @throws JMException
	 * @throws IOException
	 * @throws SecurityException
	 *             Usage: three options - jmetal.metaheuristics.moead.MOEAD_main
	 *             - jmetal.metaheuristics.moead.MOEAD_main problemName -
	 *             jmetal.metaheuristics.moead.MOEAD_main problemName
	 *             ParetoFrontFile
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws JMException,
			SecurityException, IOException, ClassNotFoundException {
		Problem problem; // The problem to solve
		Algorithm algorithm; // The algorithm to use
		Operator crossover; // Crossover operator
		Operator mutation; // Mutation operator
		Operator selection;

		HashMap parameters; // Operator parameters

		// Logger object and file to store log messages
		logger_ = Configuration.logger_;
		fileHandler_ = new FileHandler("MOEAD.log");
		logger_.addHandler(fileHandler_);
		
		if (args.length == 1) {
			Object[] params = { "Real" };
			problem = (new SASProblemFactory()).getProblem(args[0], params);
		}
		else {
			if (args.length == 2) {
				Object[] params = {"Real"};
				problem = (new SASProblemFactory()).getProblem(args[0], params);
			}
			else { // Default problem
				problem = new SAS("IntSolutionType", null, null, 2, 0);
			} // else
		}

		algorithm = new NestedRS(problem, null);

		// Algorithm parameters
		int popsize = 331;
		int generations = 1000;
		algorithm.setInputParameter("populationSize", popsize);
		algorithm.setInputParameter("maxEvaluations", popsize * generations);

		// Crossover operator
		parameters = new HashMap();
		parameters.put("probability", 0.9);
		parameters.put("distributionIndex", 20.0);
		// This needs to change in testing.
		parameters.put("jmetal.metaheuristics.moead.SASSolutionInstantiator", null);
		crossover = CrossoverFactory.getCrossoverOperator("UniformCrossoverSAS", parameters);
		
		// Mutation operator
		parameters = new HashMap();
		parameters.put("probability", 1.0 / problem.getNumberOfVariables());
		parameters.put("distributionIndex", 20.0);
		mutation = MutationFactory.getMutationOperator("BitFlipMutation", parameters);

		selection = SelectionFactory.getSelectionOperator("BinaryTournamentSAS", parameters);

		algorithm.addOperator("crossover", crossover);
		algorithm.addOperator("mutation", mutation);
		algorithm.addOperator("selection", selection);

		String curDir = System.getProperty("user.dir");
		String str 	  = curDir + "/" + problem.getName() + "M" + problem.getNumberOfObjectives();

		File dir = new File(str);
		/*if (Utils.deleteFolder(dir)) {
			System.out.println("Folders are deleted!");
		} else
			System.out.println("Folders can NOT be deleted!");
		Utils.createFolder(str);
        */
		String str1 = "FUN";
		String str2;
		String str3 = "VAR";
		String str4;
		for (int i = 0; i < 1; i++) {
			str2 = str1 + Integer.toString(i);
			str4 = str3 + Integer.toString(i);
			// Execute the Algorithm
			long initTime = System.currentTimeMillis();
			System.out.println("The " + i + " run");
			SolutionSet population = algorithm.execute();
			long estimatedTime = System.currentTimeMillis() - initTime;

			// Result messages
			logger_.info("Total execution time: " + estimatedTime + "ms");
			logger_.info("Variables values have been writen to file VAR");
//			population.printVariablesToFile(str4);
			logger_.info("Objectives values have been writen to file FUN");
			population.printObjectivesToFile(curDir + "/" + problem.getName()
					+ "M" + problem.getNumberOfObjectives() + "/" + str2);
		}
	} // main
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected SolutionSet findParetoFront(SASSolutionInstantiator factory, int[][] vars,  int numberOfObjectives_, int numberOfConstraints_) throws JMException,
			SecurityException, IOException, ClassNotFoundException {
	
		HashMap parameters; // Operator parameters
		Operator selection;

		// Logger object and file to store log messages
		if(SAS.isTest) { 
		logger_ = Configuration.logger_;
		fileHandler_ = new FileHandler("MOEAD.log");
		logger_.addHandler(fileHandler_);
		}
		problem = new SAS("SASSolutionType", factory, vars, numberOfObjectives_, numberOfConstraints_);

		algorithm = new NestedRS(problem, factory);

		// Algorithm parameters
		int popsize = 100;
		int factor = 10;
		algorithm.setInputParameter("populationSize", EAConfigure.getInstance().pop_size);
		algorithm.setInputParameter("maxEvaluations", EAConfigure.getInstance().pop_size * EAConfigure.getInstance().generation);
		algorithm.setInputParameter("weights", factory.getWeights());
		if(factory.getFixedBounds() != null) {
			algorithm.setInputParameter("fixed_bounds", factory.getFixedBounds());
		}
		// Crossover operator
		parameters = new HashMap();
		parameters.put("probability", EAConfigure.getInstance().crossover_rate);
		parameters.put("distributionIndex", 20.0);
		// This needs to change in testing.
		parameters.put("jmetal.metaheuristics.moead.SASSolutionInstantiator", factory);
		crossover = CrossoverFactory.getCrossoverOperator("UniformCrossoverSAS", parameters);
		

		if(SASAlgorithmAdaptor.isSeedSolution) {
			//algorithm.setInputParameter("seeder", new Seeder(mutation));	
			//algorithm.setInputParameter("seeder", NewSeeder.getInstance(mutation));
			algorithm.setInputParameter("seeder", FixedSeeder.getInstance());	
		}

		// Mutation operator
		parameters = new HashMap();
		parameters.put("probability", EAConfigure.getInstance().mutation_rate);
		parameters.put("distributionIndex", 20.0);
		mutation = MutationFactory.getMutationOperator("BitFlipMutation", parameters);

		selection = SelectionFactory.getSelectionOperator("BinaryTournamentSAS", parameters);

		algorithm.addOperator("crossover", crossover);
		algorithm.addOperator("mutation", mutation);
		algorithm.addOperator("selection", selection);
		
		long initTime = System.currentTimeMillis();
		
		SolutionSet population = algorithm.execute();
		long estimatedTime = System.currentTimeMillis() - initTime;
		if(SAS.isTest) { 
		logger_.setLevel(Level.CONFIG);
		logger_.log(Level.CONFIG, "Total execution time: " + estimatedTime + "ms");
		
		String str = "data/GP/SAS";
		
		//Utils.deleteFolder(new File(str+ "/results.dat"));
		//Utils.createFolder(str);
		
		population.printObjectivesToFile(str + "/results.dat");
		}
		return population;

	}


	@Override
	protected ApproachType getName() {
		return ApproachType.GP;
	}

	@Override
	protected Solution findSoleSolutionAfterEvolution(SolutionSet pareto_front) {
		// find the knee point
		Solution individual = pareto_front.get(PseudoRandom.randInt(0, pareto_front.size() - 1)); 
			
		
		for (int i = 0; i < problem.getNumberOfObjectives(); i++)
			System.out.print(individual.getObjective(i) + "\n");
		
		
		String str = "data/GP/SAS";
		//if(SAS.isTest) 
		//Utils.deleteFolder(new File(str+ "/knee_results.dat"));
		SolutionSet set = new SolutionSet(1);
		set.add(individual);
		if(SAS.isTest) 
		set.printObjectivesToFile(str + "/knee_results.dat");
		
		return individual;
	}


	@Override
	protected SolutionSet doRanking(SolutionSet population) {
		// TODO Auto-generated method stub
		return ((NestedRS)algorithm).doRanking(population);
	}
} // MOEAD_main

