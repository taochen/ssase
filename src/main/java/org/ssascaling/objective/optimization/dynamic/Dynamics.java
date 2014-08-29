package org.ssascaling.objective.optimization.dynamic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.SortedSet;

import org.ssascaling.objective.optimization.Ant;
import org.ssascaling.objective.optimization.AntValues;
import org.ssascaling.primitive.ControlPrimitive;

/**
 * Should only be used within Structure. Syncronization is done within Structure
 * as well.
 * 
 * Implement a memory based solution.
 * @author tao
 *
 */
public interface Dynamics {

	
	public Ant getBestSoFar();
	
	public List<Ant> getFronts();
	
	public void updateShort(Queue<Ant> ants, AntValues values, Map<ControlPrimitive, Integer> primitives);
	
	public void updateLong (Ant localBestAnt);
	
	public void updateLong (Collection<Ant> ants);
	
	public void copeDynamics(AntValues values, Map<ControlPrimitive, Integer> primitives);
	
	public void reinvalidate();
}
