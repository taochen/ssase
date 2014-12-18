package org.ssascaling.objective.optimization;

import java.util.*;
import java.util.Map.Entry;

import org.ssascaling.objective.Objective;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.util.Triple;
import org.ssascaling.util.Tuple;
import org.ssascaling.util.Util;
public class BasicAntColony extends AntColony {

	public BasicAntColony(
			int nID,
			List<ControlPrimitive> primitives,
			LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> objectiveMap,
			LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> constraintedObjectiveMap) {
		super(nID, primitives, objectiveMap, constraintedObjectiveMap);
	}

	@Override
	protected Ant[] createAnts(int ants) {
		Ant[] array = new Ant[ants];
		Structure[] s = structures.toArray( new Structure[structures.size()]);	
		
		for (int i = 0; i < array.length;i++) {
			array[i] = new BasicAnt(iterCounter + "-" + i, this, selectStrcture(i, s));
		}
		
		return array;
	}

	@Override
	protected void globalUpdatingRule() {
		for (Structure strcture : this.structures) {
			strcture.globalUpdate();
		}
		
	}

	@Override
	protected boolean invalidate(Ant ant, LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> cpInput) {
		
		if (ant != null) {
			// Record the best ever solution in case the ant can not find a solution before it dies.
			ant.setBestCpInput(cpInput);
		}
		
		for (Entry<Objective, List<Tuple<Primitive, Double>>> entry : constraintedObjectiveMap.entrySet() ) {
			double[] xValue = new double[entry.getValue().size()];
			
			for (int i = 0; i < entry.getValue().size(); i++) {
				Primitive cp = entry.getValue().get(i).getVal1();
				// If the CP is within the optimized objectives, then use it. otherwise use the original value
				// in case it is a reduced objective.
				xValue[i] = cpInput.containsKey(cp)? cpInput.get(cp).getVal2() : entry.getValue().get(i).getVal2();
			}
			
			
			
			if (!entry.getKey().isSatisfied(xValue)) {
				
				/*for (Map.Entry<ControlPrimitive, Tuple<Integer, Double>>  e : cpInput.entrySet()) {
					System.out.print(e.getValue().getVal2()  + " \n"); 
				}*/
				
				//System.out.print(entry.getKey().getName() + " is not satisfied, start again, value: " + entry.getKey().predict(xValue)  + "\n");
				
				return false;
			}
		
		}
		return true;
	}
	
	protected int invalidate(LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> cpInput) {
		
		int result = 0;
		for (Entry<Objective, List<Tuple<Primitive, Double>>> entry : constraintedObjectiveMap.entrySet() ) {
			double[] xValue = new double[entry.getValue().size()];
			
			for (int i = 0; i < entry.getValue().size(); i++) {
				Primitive cp = entry.getValue().get(i).getVal1();
				// If the CP is within the optimized objectives, then use it. otherwise use the original value
				// in case it is a reduced objective.
				xValue[i] = cpInput.containsKey(cp)? cpInput.get(cp).getVal2() : entry.getValue().get(i).getVal2();
			}
			
			
			
			if (!entry.getKey().isSatisfied(xValue)) {
				
				result++;
			}
		
		}
		return result;
	}

	@Override
	protected Structure selectStrcture(int i, Structure[] structures) {
		//System.out.print(structures[0] + " test " + (( structures.length > i)? i : i%structures.length) + "\n");
		return structures[( structures.length > i)? i : i%structures.length];
	}

	// TODO check here
	@Override
	protected LinkedHashMap<ControlPrimitive, Double> getParetoOptimality() {
		Set<Structure> set = structures;
		
		List<Ant> ants = new ArrayList<Ant> ();
		List<Ant> reducedAnts = new ArrayList<Ant> ();
		for (Structure structure : set) {
			final List<Ant> sub = structure.getFronts();
			ants.addAll(sub);
			reducedAnts.addAll(sub);
		}
		
	
		final LinkedHashMap<Objective, Tuple<Double, Double>> result = findBestAndMax(ants);
		
		
		double[] best = new double[result.size()];
		double[] max = new double[result.size()];
		int i = 0;
		for (Map.Entry<Objective, Tuple<Double, Double>> e : result.entrySet()) {
			best[i] = e.getValue().getVal1()/e.getValue().getVal2();
			max[i] = e.getValue().getVal2();
			i++;
		}
		
		System.out.print("We have total solutions " + ants.size() + "\n");
		Ant optimal = null;
		invalidate(reducedAnts);
		// if there is an solution that violates non of the constraints.
		if (reducedAnts.size() != 0) {
		
			System.out.print("We have valid solutions " + reducedAnts.size() + "\n");
			// 0 means the first non-dominated fronts.
			optimal = doDominanceSelection(best, max, reducedAnts);
		} else {
			System.out.print("We do not have valid solutions  \n");
			getLeastNoOfViolatedObjectives(ants);
			System.out.print("We have valid solutions with least number of violated objectives " + ants.size() + "\n");
			// 0 means the first non-dominated fronts.
			// Store one in case there is no solution that violates non of the constraints.
			optimal = doDominanceSelection(best, max, ants);
		}
		
		return optimal == null? null : optimal.getFinalResult();
	}
	
	private Ant doDominanceSelection (double[] best, double[] max, List<Ant> ants){
		
		Ant compromise_dominance = compromiseNonDominatedSort(ants, best, max);
		
		// 0 means the first non-dominated fronts.
		// Store one in case there is no solution that violates non of the constraints.
		//Ant pareto_harmonic = harmonicDistanceSort(paretoNonDominatedSort(ants).get(0), max);
		//Ant nash_harmonic = harmonicDistanceSort(nashNonDominatedSort(paretoNonDominatedSort(ants).get(0)).get(0), max);
		//Ant nash_best_distance = bestDistanceSort(max, best, nashNonDominatedSort(paretoNonDominatedSort(ants).get(0)).get(0));
		//Ant nash_nash_product = nashProductSort(max, nashNonDominatedSort(paretoNonDominatedSort(ants).get(0)).get(0));
		//Ant best_distance  = bestDistanceSort(max, best, ants);
		//Ant nash_product  = nashProductSort(max, ants);
		
		/*printCMeasure("pareto_harmonic", "nash_harmonic", pareto_harmonic, nash_harmonic);
		printCMeasure("pareto_harmonic", "nash_best_distance", pareto_harmonic, nash_best_distance);
		printCMeasure("pareto_harmonic", "best_distance", pareto_harmonic, best_distance);
		printCMeasure("nash_harmonic", "nash_best_distance", nash_harmonic, nash_best_distance);
		printCMeasure("nash_harmonic", "best_distance", nash_harmonic, best_distance);
		printCMeasure("nash_best_distance", "best_distance", nash_best_distance, best_distance);*/
		
		//printCMeasure("nash_nash_product", "pareto_harmonic", nash_nash_product, pareto_harmonic);
		//printCMeasure("nash_nash_product", "nash_harmonic", nash_nash_product, nash_harmonic);
		//printCMeasure("nash_nash_product", "nash_best_distance", nash_nash_product, nash_best_distance);
		/*printCMeasure("nash_nash_product", "best_distance", nash_nash_product, best_distance);
		printCMeasure("nash_nash_product", "nash_product", nash_nash_product, nash_product);
		
		printCMeasure("nash_product", "pareto_harmonic", nash_product, pareto_harmonic);
		printCMeasure("nash_product", "nash_harmonic", nash_product, nash_harmonic);
		printCMeasure("nash_product", "nash_best_distance", nash_product, nash_best_distance);
		printCMeasure("nash_product", "best_distance", nash_product, best_distance);*/
		
		//printCMeasure("compromise_dominance", "nash_best_distance", compromise_dominance, nash_best_distance);
		//printCMeasure("compromise_dominance", "nash_nash_product", compromise_dominance, nash_nash_product);
		
		//printDistance("pareto_harmonic", pareto_harmonic, max, best);
		//printDistance("nash_harmonic", nash_harmonic, max, best);
		//printDistance("nash_best_distance", nash_best_distance, max, best);
		//printDistance("best_distance", best_distance, max, best);
		//printDistance("nash_nash_product", nash_nash_product, max, best);
		//printDistance("nash_product", nash_product, max, best);
		//printDistance("compromise_dominance", compromise_dominance, max, best);
		
		return compromise_dominance;
	}
	
	private Ant compromiseNonDominatedSort(List<Ant> ants, double[] best, double[] max){
	
		
		List<Ant> pop = new ArrayList<Ant>(ants);
		Map<Ant, Integer> id = new HashMap<Ant, Integer>();
		for (int i = 0; i < pop.size(); i++) {
			id.put(pop.get(i), i);
		}


		Map<Ant, List<Ant>> pareto_S = new HashMap<Ant, List<Ant>>();
		Map<Ant, List<Ant>> nash_S = new HashMap<Ant, List<Ant>>();
		int[] pareto_n = new int[pop.size()];
		int[] nash_n = new int[pop.size()];
		
		for (Ant e : pop) {
			pareto_S.put(e, new ArrayList<Ant>());
			pareto_n[id.get(e)] = 0;
			
			nash_S.put(e, new ArrayList<Ant>());
			nash_n[id.get(e)] = 0;
		}

		for (int i = 0; i < pop.size(); i++) {
			for (int j = i + 1; j < pop.size(); j++) {

				Ant p = pop.get(i);
				Ant q = pop.get(j);

				if (p.paretoDominates(q)) {
					pareto_S.get(p).add(q);
					pareto_n[id.get(q)]++;
				} else if (q.paretoDominates(p)) {
					pareto_S.get(q).add(p);
					pareto_n[id.get(p)]++;
				}
				

				if (p.nashDominates(q)) {
					nash_S.get(p).add(q);
					nash_n[id.get(q)]++;
				} else if (q.nashDominates(p)) {
					nash_S.get(q).add(p);
					nash_n[id.get(p)]++;
				}
				
			}
		}

		
		
		// Do the pareto-dominance.
		List<Ant> pareto_f1 = this.selectDominanceRelation(pop, id, pareto_n);
		
		
		System.out.print("Number of pareto dominanted ants with this value " + pareto_f1.size() + "\n");
		
	   // Do the nash-dominance.
		List<Ant> nash_f1 = this.selectDominanceRelation(pareto_f1, id, nash_n);
	
		
		System.out.print("Number of nash dominanted ants with this value " + nash_f1.size() + "\n");
		
		// The distance is absolute rather than relative, therefore it can be calculated
		// based on the reduced pareto fornts.
		return bestDistanceSort(max, best, nash_f1);
		
	}
	
	
	private List<Ant> selectDominanceRelation(List<Ant> ants, Map<Ant, Integer> id, int[] n){
		List<Ant> f1 = new ArrayList<Ant>();
		int smallest = ants.size();
		
        for (Ant i : ants) {
			
			if (n[id.get(i)] < smallest) {
				smallest = n[id.get(i)];
			}
			
			
			if (n[id.get(i)] == 0) {
				f1.add(i);
			}
		}
		
		// If no nondominat set, we use the ones that cloest to the nondominated solutions.
		if (f1.size() == 0) {
			for (Ant i : ants) {
				
				if (n[id.get(i)] == smallest) {
					f1.add(i);
				}
			}
		}
		
		System.out.print("Smallest dominance rank value is " + smallest + "\n");
	
		System.out.print("Dominated by: " + Arrays.toString(n) + "\n");
			
		
		return f1;
	}
	
	// TODO normalize the data by dividing it to the max value before using E-distance
	// TODO implement the disntace to the optmal value of each objective sort.
	private Ant harmonicDistanceSort(List<Ant> ants, double[] max){
		/*
		 * Here instead of using k-nearest, we use constant k which equals to the size - 1.
		 */
		
		Map<Ant, Map<Ant, Double>> map = new HashMap<Ant, Map<Ant, Double>>();
		
		int n = ants.size() - 1;
		
		Ant selected = null;
		double distance = 0.0;
		for (Ant ant : ants) {
			double sub = 0.0;
			for (Ant nestedAnt : ants) {
				
				if (ant.equals(nestedAnt)) {
					continue;
				}
				
				if (!map.containsKey(ant)) {
					map.put(ant, new HashMap<Ant, Double>());
				}
				
				if (!map.containsKey(nestedAnt)) {
					map.put(nestedAnt, new HashMap<Ant, Double>());
				}
				
			
				if (!map.get(ant).containsKey(nestedAnt)) {
					double ed = 1/Util.calculateEuclideanDistance(ant.getFront(max), nestedAnt.getFront(max));
					map.get(ant).put(nestedAnt, ed);
					map.get(nestedAnt).put(ant, ed);
					sub += ed;
				} else {
					sub += map.get(ant).get(nestedAnt);
				}
			}
			
			double hd = n/sub;
			// The larger harmonic distance, the better solution 
			if (selected == null || hd > distance) {
				selected = ant;
				distance = hd;
			}
		}
		
		
		return selected;
	}
	
	private List<List<Ant>> paretoNonDominatedSort(Collection<Ant> ants) {

		List<Ant> pop = new ArrayList<Ant>(ants);
		Map<Ant, Integer> id = new HashMap<Ant, Integer>();
		for (int i = 0; i < pop.size(); i++) {
			id.put(pop.get(i), i);
		}

		List<List<Ant>> fronts = new ArrayList<List<Ant>>();

		Map<Ant, List<Ant>> S = new HashMap<Ant, List<Ant>>();
		int[] n = new int[pop.size()];

		for (Ant e : pop) {
			S.put(e, new ArrayList<Ant>());
			n[id.get(e)] = 0;
		}

		for (int i = 0; i < pop.size(); i++) {
			for (int j = i + 1; j < pop.size(); j++) {

				Ant p = pop.get(i);
				Ant q = pop.get(j);

				if (p.paretoDominates(q)) {
					S.get(p).add(q);
					n[id.get(q)]++;
				} else if (q.paretoDominates(p)) {
					S.get(q).add(p);
					n[id.get(p)]++;
				}
			}
		}

		List<Ant> f1 = new ArrayList<Ant>();
		for (Ant i : pop) {
			if (n[id.get(i)] == 0) {
				f1.add(i);
			}
		}
		fronts.add(f1);
		
		System.out.print("Number of pareto nondomninant solutions " + f1.size() + "\n");
		List<Ant> fi = f1;
		// Sort the final fronts, in case there is no nondominated solutions.
		while (!fi.isEmpty()) {
			List<Ant> h = new ArrayList<Ant>();
			for (Ant p : fi) {
				for (Ant q : S.get(p)) {
					n[id.get(q)]--;
					if (n[id.get(q)] == 0) {
						h.add(q);
					}
				}
			}
			fronts.add(h);
			fi = h;
		}
		
		
		
		return fronts;
	}
	
	/**
	 * A elipson nash dominance
	 * @param ants
	 * @return
	 */
	private List<List<Ant>> nashNonDominatedSort(Collection<Ant> ants) {

		List<Ant> pop = new ArrayList<Ant>(ants);
		Map<Ant, Integer> id = new HashMap<Ant, Integer>();
		for (int i = 0; i < pop.size(); i++) {
			id.put(pop.get(i), i);
		}

		List<List<Ant>> fronts = new ArrayList<List<Ant>>();

		Map<Ant, List<Ant>> S = new HashMap<Ant, List<Ant>>();
		int[] n = new int[pop.size()];

		for (Ant e : pop) {
			S.put(e, new ArrayList<Ant>());
			n[id.get(e)] = 0;
		}

		for (int i = 0; i < pop.size(); i++) {
			for (int j = i + 1; j < pop.size(); j++) {

				Ant p = pop.get(i);
				Ant q = pop.get(j);

				if (p.nashDominates(q)) {
					S.get(p).add(q);
					n[id.get(q)]++;
				} else if (q.nashDominates(p)) {
					S.get(q).add(p);
					n[id.get(p)]++;
				}
			}
		}

		List<Ant> f1 = new ArrayList<Ant>();
		int smallest = ants.size();
		for (Ant i : pop) {
			
			if (n[id.get(i)] < smallest) {
				smallest = n[id.get(i)];
			}
			
			if (n[id.get(i)] == 0) {
				f1.add(i);
			}
		}
		
		// If no nash nondominat set, we use eplison nash nondominat
		if (f1.size() == 0) {
			for (Ant i : pop) {
				
				if (n[id.get(i)] == smallest) {
					f1.add(i);
				}
			}
		}
		
		System.out.print("Smallest value is " + smallest + "\n");
		System.out.print("Number of ants with this value " + f1.size() + "\n");
		fronts.add(f1);
	
		return fronts;
	}
	
	/**
	 * This is also pareto optimal.
	 * @param max
	 * @param best
	 * @param ants
	 * @return
	 */
	private Ant bestDistanceSort(double[] max, double[] best, List<Ant> ants){
		
		
		
		Ant selected = null;
		double distance = 0.0;
		for (int k = 0; k < ants.size(); k++) {
			double sub = Util.calculateEuclideanDistance(ants.get(k).getFront(max), best);
			
			if (selected == null || sub < distance) {
				selected = ants.get(k);
				distance = sub;
			}
		}
		return selected;
	}
	
	/**
	 * This is also pareto optimal.
	 * @param max
	 * @param best
	 * @param ants
	 * @return
	 */
	private Ant nashProductSort(double[] max,  List<Ant> ants){
		
		
		
		Ant selected = null;
		long product = 0;
		for (int k = 0; k < ants.size(); k++) {
			long sub = Util.calculateNashProduct(calculateFront(ants.get(k).getDecision(), max, true));
			
			if (selected == null || sub > product) {
				selected = ants.get(k);
				product = sub;
			}
		}
		return selected;
	}
	
	/**
	 * double, double - best, max
	 * @param ants
	 * @return The order should be the same as in objectiveMap
	 */
	private LinkedHashMap<Objective, Tuple<Double, Double>> findBestAndMax(List<Ant> ants){
		
		LinkedHashMap<Objective, Tuple<Double, Double>> map = new LinkedHashMap <Objective, Tuple<Double, Double>> ();
		LinkedHashMap<Objective, Tuple<Double, Double>> result = new LinkedHashMap <Objective, Tuple<Double, Double>> ();
		
		
		for (Ant ant : ants) {
			
			if (!map.containsKey(ant.getObjective())) {
				map.put(ant.getObjective(), new Tuple<Double, Double>(ant.getObjective().isMin()? Double.MAX_VALUE : Double.MIN_VALUE ,Double.MIN_VALUE));
			}
			
			if (ant.getValue() > map.get(ant.getObjective()).getVal2()) {
				map.get(ant.getObjective()).setVal2(ant.getValue());
			}
			
			
			if (ant.getObjective().isMin() && ant.getValue() < map.get(ant.getObjective()).getVal1()) {
				map.get(ant.getObjective()).setVal1(ant.getValue());
			} else if (!ant.getObjective().isMin() && ant.getValue() > map.get(ant.getObjective()).getVal1()) {
				map.get(ant.getObjective()).setVal1(ant.getValue());
			}
		}
		
		for (Map.Entry<Objective, List<Tuple<Primitive, Double>>> entry : objectiveMap.entrySet()){
			result.put(entry.getKey(), map.get(entry.getKey()));
		}
		
		return result;
	}
	

	@Override
	protected void dynamicAdapation() {
		
		
		// Update mu, sync by the write lock in AntColony.
		calculateMu();
		for (Structure structure : structures) {
			// Update tau, sync by the write lock in each Structure.
			structure.doDynamicAdapation();
		}
		
	}
	
	/**
     * If the changed model is a constraint one, then simply redo the evaluation.
     */
	protected void evaluateConstraints(){
		for (Structure structure : structures) {
			structure.reinvlidateWhenChange();
		}
    }

	@Override
	protected Ant buildAnt(Structure structure) {
		return new BasicAnt("", this, structure);
	}
	
	private void printCMeasure(String a, String b, Ant antA, Ant antB){
		int[] r = antA.c_measure(antB);
		String out = null;
		if(r[0] > r[1]) {
			out = ", " + a +" is better";
		} else if(r[0] < r[1]) {
			out = ", " + b +" is better";
		} else {
			out = ", they are equal";
		}
		System.out.print("C-measure:" + a + " to " + b + " is " + r[0] + ", " + b + " to " + a + " is " + r[1] + out +  "\n");
	}
	
	private void printDistance(String a, Ant ant, double[] max, double[] best){
		double distance = Util.calculateEuclideanDistance(ant.getFront(max), best);
		System.out.print("Best distance: the distance of " + a + " to the best is " + distance + "\n");
	}
	
	private void invalidate(List<Ant> ants){
		List<Ant> list = new ArrayList<Ant>();
		
		for (Ant a : ants) {
			if (!a.selfInvalidate()) {
				list.add(a);
			}
		}
		
		ants.removeAll(list);
	}

	private void getLeastNoOfViolatedObjectives(List<Ant> ants){
        Map<Ant, Integer> map = new  HashMap<Ant, Integer>();
		int least = 0;
		for (Ant a : ants) {
			int no = a.noOfViolatedObjective();
			map.put(a, no);
			if (no < least) {
				least = no;
			}
		}
		
		for ( Map.Entry<Ant, Integer> entry : map.entrySet()) {
			if (entry.getValue() != least) {
				ants.remove(entry.getKey());
			}
		}
		
	}

}
