package org.ssase.objective.optimization.dynamic;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.ssase.objective.optimization.Ant;
import org.ssase.objective.optimization.AntValues;
import org.ssase.primitive.ControlPrimitive;

public class ClassicalDynamic implements Dynamics {
	
	protected Queue<Ant> solutions = new PriorityQueue<Ant>();

	@Override
	public Ant getBestSoFar() {
		return solutions.peek();
	}

	@Override
	public List<Ant> getFronts() {
		List<Ant> ants = new LinkedList<Ant>();
		ants.addAll(solutions);
		return ants;
	}

	@Override
	public void updateShort(Queue<Ant> ants, AntValues values,
			Map<ControlPrimitive, Integer> primitives) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateLong(Ant localBestAnt) {
		solutions.add(localBestAnt);

	}

	@Override
	public void copeDynamics(AntValues values,
			Map<ControlPrimitive, Integer> primitives) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reinvalidate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateLong(Collection<Ant> ants) {
		solutions.addAll(ants);		
	}

}
