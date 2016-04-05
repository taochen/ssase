package org.ssase.objective.optimization.moga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ssase.objective.Objective;
import org.ssase.objective.optimization.moaco.Ant;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.util.Tuple;
import org.ssase.util.Util;

public class NSGAII {

	private List<Individual> populations = new ArrayList<Individual>(); 
	private int initialSize = 3;
	private int populationSize = 750;
	private int numberOfChild = 2;
	private int iterations = 100;//372;
	
	private int maxRun = 80;//60
	
	protected LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> objectiveMap;
	
	
	public NSGAII  (LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> objectiveMap) {
		this.objectiveMap = objectiveMap;
		for (int i = 0 ; i < initialSize; i++) {
			populations.add(new Individual(objectiveMap, true));
		}
	}
	
	public synchronized LinkedHashMap<ControlPrimitive, Double> doOptimization() {
		
		System.out.print("start NSGA-II optimization\n");
		
		  
        int iterCounter = 0;
      
        
        
        // loop for all iterations
        while(iterCounter < iterations)
        {
        	Individual[] parents = paretoNonDominatedSort(populations);
        	
        	Individual[] children = new Individual[numberOfChild];
        	for (int i = 0; i < numberOfChild; i ++) {
        		int run = 0;
        		int numberOfUnsatisfied = 0;
        		Individual temp = null;
        		do {
        			
        			
        		
        		  children[i] = parents[0].crossover(parents[1]);
        		  children[i].mutate();
        		  run++;
        		  

        		  /*if (temp == null || children[i].isSatisfy() <= numberOfUnsatisfied) {
        			  temp = children[i];
        		  }*/
        		
        		} while(((numberOfUnsatisfied = children[i].isSatisfy()) != 0) && run < maxRun);
        		
        		//children[i] = temp;
        		//System.out.print("least number of violated " + numberOfUnsatisfied + " \n");
        		//System.out.print(run + " runs\n");
        	}
        	
        	for (Individual ind : children) {
        		populations.add(ind);
        	}
        	//System.out.print(iterCounter + " compeleted\n");
        	iterCounter++;
        }
        
        paretoNonDominatedSort(populations);
        
        getSatisfiedObjectives(populations);
        
        return populations.get(Util.randInt(0, populations.size() - 1)).getFinalResult();
		
	}

    private Individual[] paretoNonDominatedSort(List<Individual> pop){
	
    	//System.out.print("Size :" + pop.size() + "\n");
    	
		Map<Individual, Integer> id = new HashMap<Individual, Integer>();
		for (int i = 0; i < pop.size(); i++) {
			id.put(pop.get(i), i);
			pop.get(i).resetCrowdingValue();
		}


		Map<Individual, List<Individual>> pareto_S = new HashMap<Individual, List<Individual>>();
		//Map<Individual, List<Individual>> nash_S = new HashMap<Individual, List<Individual>>();
		int[] pareto_n = new int[pop.size()];
		//int[] nash_n = new int[pop.size()];
		
		for (Individual e : pop) {
			pareto_S.put(e, new ArrayList<Individual>());
			pareto_n[id.get(e)] = 0;
			
			//nash_S.put(e, new ArrayList<Individual>());
			//nash_n[id.get(e)] = 0;
		}

		for (int i = 0; i < pop.size(); i++) {
			for (int j = i + 1; j < pop.size(); j++) {

				Individual p = pop.get(i);
				Individual q = pop.get(j);

				if (p.paretoDominates(q)) {
					pareto_S.get(p).add(q);
					pareto_n[id.get(q)]++;
				} else if (q.paretoDominates(p)) {
					pareto_S.get(q).add(p);
					pareto_n[id.get(p)]++;
				}
				

				
			}
		}

		
		
		// Do the pareto-dominance.
		List<Individual> pareto_f1 = this.selectDominanceRelation(pop, id, pareto_n);
		
		
		//System.out.print("Number of pareto dominIndividualed Individuals with this value " + pareto_f1.size() + "\n");
		
	   // Do the nash-dominance.
		//List<Individual> nash_f1 = this.selectDominanceRelation(pareto_f1, id, nash_n);
	
		
		//System.out.print("Number of nash dominIndividualed Individuals with this value " + nash_f1.size() + "\n");
		
		// The distance is absolute rather than relative, therefore it can be calculated
		// based on the reduced pareto fornts.
		crowdingDistance(pareto_f1);
		
		populations = pareto_f1;
		
		
		/*for (Individual in : pareto_f1) {
			System.out.print("Crowding value: " + in.getCrowdValue() + "\n");
		}*/
		
		return new Individual[]{pareto_f1.get(0), pareto_f1.get(1)};
		
	}
    
	private List<Individual> selectDominanceRelation(List<Individual> ants, Map<Individual, Integer> id, int[] n){
		List<Individual> f1 = new ArrayList<Individual>();
		int smallest = ants.size();
		
        for (Individual i : ants) {
			
			if (n[id.get(i)] < smallest) {
				smallest = n[id.get(i)];
			}
			
			
			if (n[id.get(i)] == 0) {
				f1.add(i);
			}
		}
		
		// If no nondominat set, we use the ones that cloest to the nondominated solutions.
		if (f1.size() == 0) {
			for (Individual i : ants) {
				
				if (n[id.get(i)] == smallest) {
					f1.add(i);
				}
			}
		}
		
		//System.out.print("Smallest dominance rank value is " + smallest + "\n");
	
		//System.out.print("Dominated by: " + Arrays.toString(n) + "\n");
			
		
		return f1;
	}
	
	@SuppressWarnings("unchecked")
	private void crowdingDistance (List<Individual> pareto_f1){
		
		
		int index = 0;
		
		
		for (Objective obj : objectiveMap.keySet()) {
			
			final int temp = index;
		
			
			Collections.sort(pareto_f1, new Comparator<Individual>(){

				@Override
				public int compare(Individual arg0, Individual arg1) {
					if (arg0.getObjectiveValue(temp) < arg1.getObjectiveValue(temp) ) 
						return -1;
					else if (arg0.getObjectiveValue(temp) > arg1.getObjectiveValue(temp) ) 
						return 1;
					else
					    return 0;
				}

				
			});
			
			double min = pareto_f1.get(0).getObjectiveValue(temp);
			double max = pareto_f1.get(pareto_f1.size()-1).getObjectiveValue(temp);
			
			double divide = max - min;
			
			if (divide == 0) {
				divide = max;
			}
			
			for (int i = 1; i < pareto_f1.size()-2; i ++) {
				double v = (pareto_f1.get(i+1).getObjectiveValue(temp) 
						- pareto_f1.get(i-1).getObjectiveValue(temp))/divide;

				//System.out.print("Previous: " + pareto_f1.get(i).getCrowdValue() + " update: " + v + "\n");
		
				pareto_f1.get(i).addCrowdingValue(v);
				//System.out.print("After: " + pareto_f1.get(i).getCrowdValue() + " update: " + v + "\n");
			}
			
			
			
			index++;
		}
		
		
		Collections.sort(pareto_f1);
		
		while (pareto_f1.size() > populationSize) {
			pareto_f1.remove(populationSize);
			
		}
	}
	
	private void getSatisfiedObjectives(List<Individual> ants){
		List<Individual> temp = new ArrayList<Individual>();
		
		for (Individual a : ants) {
			int no = a.isSatisfy();
			if (no != 0) {
				temp.add(a);
			}
		}
		
		if (temp.size() == ants.size()) {		
			System.out.print("There is no satisfied deicions\n");
			return;
		} 
		
		for (Individual a : temp) {
			ants.remove(a);
			
		}
		System.out.print("There are " + ants.size() + " satisfied deicions \n");
	}
	
}
