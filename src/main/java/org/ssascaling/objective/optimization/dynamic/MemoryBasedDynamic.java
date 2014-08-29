package org.ssascaling.objective.optimization.dynamic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.ssascaling.objective.optimization.Ant;
import org.ssascaling.objective.optimization.AntColony;
import org.ssascaling.objective.optimization.AntValues;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.util.Tuple;

public class MemoryBasedDynamic implements Dynamics {
	
	private static final double replacement_rate = 0.5;

	
	private static final int short_size = 20;

	
	private static final int long_size = 20;
	// The queue of best so far ants of a structure.
	protected Queue<Ant> K_short = new PriorityQueue<Ant>();
	// The queue for mutation upon dynamics.
	protected Queue<Ant> K_long = new PriorityQueue<Ant>();

	
	public MemoryBasedDynamic(){
		
	}
	
	@Override
	public Ant getBestSoFar() {
		Ant ant = null;
		// The head of the queue would be the best one;
		Ant shortAnt = K_short.peek();
		Ant longAnt = K_long.peek();
		if (shortAnt.isBetter(longAnt)) {
			ant = shortAnt;
		} else {
			ant = longAnt;
		}
		return ant;
	}

	@Override
	public List<Ant> getFronts() {
		
		// This list is unsorted.
		List<Ant> ants = new LinkedList<Ant>();
		// ********* Use all solutions ***********
		//ants.addAll(K_short);
		//ants.addAll(K_long);
		
		
		// ********* Use only the best one for the corresponding objective ***********
		ants.add(getBestSoFar());
		
		return ants;
	}

	@Override
	public void updateShort(Queue<Ant> ants, AntValues values, Map<ControlPrimitive, Integer> primitives) {
		// Adding in  for sorting.
		List<Ant> list = new LinkedList<Ant>();
		list.addAll(K_short);
		list.addAll(ants);
		
		Set<Ant> shortSet = new HashSet<Ant>();
		Set<Ant> antSet = new HashSet<Ant>();
		
		shortSet.addAll(K_short);
		antSet.addAll(ants);
		
		Collections.sort(list);
	
		for (int i = short_size; i < list.size(); i++) {
			// Means there is a remove from the original short queue.
			if (shortSet.contains(list.get(i))) {
				updateAntValues(list.get(i), values, primitives, false);
			}
			
			antSet.remove(list.get(i));
		}
		
		// The remaining ones are those newly included.
		for (Ant ant : antSet) {
			updateAntValues(ant, values, primitives, true);
		}

		K_short.clear();
		K_short.addAll(list);
	}

	@Override
	public void updateLong(Ant localBestAnt) {
		if (K_long.size() < long_size) {
			K_long.add(localBestAnt);
			return;
		}
		
		double distance = 0;
		Ant replaced = null;
		for (Ant ant : K_long) {
			double value = localBestAnt.solutionSimilarityValue(ant);
			if (distance == 0 || value < distance) {
				distance = value;
				replaced = ant;
			}
		}
		

		// Replace the closest one if it is indeed better.
		if (localBestAnt.isBetter(replaced)) {
			K_long.remove(replaced);
			K_long.add(localBestAnt);
		}
		
		
	}
	
	public void reinvalidate() {
		final List<Ant> shortL = new LinkedList<Ant>();
		final List<Ant> longL = new LinkedList<Ant>();
		// Remove invalid solutions.
		for (Ant ant : K_short) {
			if(ant.reinvlidateWhenChange()){
			    shortL.add(ant);
			}
		}
		
		for (Ant ant : K_long) {
			if(ant.reinvlidateWhenChange()){
				longL.add(ant);
			}
			
		}
		
		K_short.clear();
		K_long.clear();
		// We need to mannully resort the list.
		K_short.addAll(shortL);
		K_long.addAll(longL);
	}

	@Override
	public void copeDynamics(AntValues values, Map<ControlPrimitive, Integer> primitives) {
	
		
		reinvalidate();

		// Update long list first with the best ant in short list.
		updateLong(K_short.peek());
		
		final Queue<Ant> ants = generateMutatedAnts();
		
		if (ants.size() == 0) {
			return;
		}
		
		// Update the short list by mutating the best of 
		updateShort(ants, values, primitives);

	}
	
	private void updateAntValues(Ant ant, AntValues values,
			Map<ControlPrimitive, Integer> primitives, boolean isGood) {
		LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> decision = ant
				.getDecision();

		for (Map.Entry<ControlPrimitive, Tuple<Integer, Double>> entry : decision
				.entrySet()) {

			// If it is not associated with the objective, then do not need to
			// update tau
			// even it is selected
			if (!primitives.containsKey(entry.getKey())) {
				continue;
			}

			values.update(primitives.get(entry.getKey()), entry.getValue()
					.getVal1(), isGood);
		}
	}
	
	private Queue<Ant> generateMutatedAnts(){
		
		Queue<Ant> ants = new PriorityQueue<Ant>();
		
		int total = (int) Math.round( replacement_rate * short_size);
		Ant base = K_long.peek();
		for (int k = 0; k < total; k++) {
			Ant immigraint = (Ant) base.clone();
			if (immigraint.mutate()) {
				ants.add(immigraint);
			}
		}
		return ants;
	}


	@Override
	public void updateLong(Collection<Ant> ants) {
		K_long.addAll(ants);		
	}
}
