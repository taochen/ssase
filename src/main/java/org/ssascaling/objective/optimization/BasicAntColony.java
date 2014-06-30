package org.ssascaling.objective.optimization;

import java.util.*;
import java.util.Map.Entry;

import org.ssascaling.objective.Objective;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;
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
	protected boolean invalidate(LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> cpInput) {
		
		for (Entry<Objective, List<Tuple<Primitive, Double>>> entry : constraintedObjectiveMap.entrySet() ) {
			double[] xValue = new double[entry.getValue().size()];
			
			for (int i = 0; i < entry.getValue().size(); i++) {
				Primitive cp = entry.getValue().get(i).getVal1();
				// If the CP is within the optimized objectives, then use it. otherwise use the original value
				// in case it is a reduced objective.
				xValue[i] = cpInput.containsKey(cp)? cpInput.get(cp).getVal2() : entry.getValue().get(i).getVal2();
			}
			
			if (!entry.getKey().isSatisfied(xValue)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected Structure selectStrcture(int i, Structure[] structures) {
		return structures[structures.length%i - 1];
	}

	@Override
	protected LinkedHashMap<ControlPrimitive, Double> getParetoOptimality() {
		Set<Structure> set = structures;
		
		List<Ant> ants = new ArrayList<Ant> ();
		
		for (Structure structure : set) {
			ants.addAll(structure.getFronts());
		}
		
		// 0 means the first non-dominated fronts.
		Ant optimal = harmonicDistanceSort(nonDominatedSort(ants).get(0));
		
		return optimal.getFinalResult();
	}
	
	private Ant harmonicDistanceSort(List<Ant> ants){
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
					double ed = 1/Util.calculateEuclideanDistance(ant.getFront(), nestedAnt.getFront());
					map.get(ant).put(nestedAnt, ed);
					map.get(nestedAnt).put(ant, ed);
					sub += ed;
				} else {
					sub += map.get(ant).get(nestedAnt);
				}
			}
			
			double hd = n/sub;
			// The larger crowding distance, the better solution 
			if (selected == null || hd > distance) {
				selected = ant;
				distance = hd;
			}
		}
		
		
		return selected;
	}
	
	private List<List<Ant>> nonDominatedSort(Collection<Ant> ants) {

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

				if (p.dominates(q)) {
					S.get(p).add(q);
					n[id.get(q)]++;
				} else if (q.dominates(p)) {
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
		List<Ant> fi = f1;
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
	


}
