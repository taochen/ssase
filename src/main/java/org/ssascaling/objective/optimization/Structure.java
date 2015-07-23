package org.ssascaling.objective.optimization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.ssascaling.objective.Objective;
import org.ssascaling.objective.optimization.dynamic.Dynamics;
import org.ssascaling.objective.optimization.dynamic.ClassicalDynamic;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.util.Tuple;

public class Structure {

	/*public static final double MAX_GRAPH_TAU = 100;

	public static final double MIN_GRAPH_TAU = 1;

	public static final double GRAPH_EVAPORATION = 3;*/

	private double MAX_VALUE_TAU = 0;

	private double MIN_VALUE_TAU = 0;
	
	private double P_MAX_VALUE_TAU = 0;

	private double P_MIN_VALUE_TAU = 0;
	
	
	public static final double g = 2;

	public static final double VALUE_EVAPORATION = 0.1;

	// Weight for mu
	private static final double ALPHA = 4;

	// Weight for tau
	private static final double BETA = 1;


	private Objective objective;

	//protected Ant localBestAnt;

	//protected AntGraph antGraph;

	private AntValues antValues;
	
	// violated
	private Queue<Ant> solutions = new PriorityQueue<Ant>();

	private Dynamics dynamics;
	//protected List<Ant> K_short = new LinkedList<Ant>();
	
	//protected List<Ant> K_long = new LinkedList<Ant>();

	// The primitives that only associated with the objective.
	// Integer - index of the CP in the CP list for all optmized objectives in AntColony
	// Order is not important here.
	private Map<ControlPrimitive, Integer> primitives;

	// Same instance of the one in AntColony
	private LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> constraintedObjectiveMap;
	
	private AtomicInteger writeLock = new AtomicInteger(0);
	
	public Structure (Objective objective, 
			List<ControlPrimitive> primitivesList,
			Map<ControlPrimitive, Integer> primitives,
			LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> constraintedObjectiveMap){
		this.objective = objective;
		this.primitives = primitives;
		this.constraintedObjectiveMap = constraintedObjectiveMap;
		dynamics = new ClassicalDynamic();
		updateTrailLimits(objective.getCurrentPrediction());
		antValues = new AntValues(primitivesList, MAX_VALUE_TAU);
	}
	

	/**
	 * If a primitive is not associated with the objective, then do not need to update its tau
	 * @param ant
	 */
	public void localUpdate(Ant ant) {
		
		final LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> decision = ant
		.getDecision();
		
		String selected = "";
		
		
		for (Map.Entry<ControlPrimitive, Tuple<Integer, Double>> entry : decision
				.entrySet()) {
		
			// If it is not associated with the objective, then do not need to update tau 
			// even it is selected
			if (!primitives.containsKey(entry.getKey())) {
				continue ;
			}
		

			selected += ", " + entry.getValue()
			.getVal1();
			/*antValues.update(primitives.get(entry.getKey()), entry.getValue()
					.getVal1(), -1, -1,
					objective.isMin(), MAX_VALUE_TAU, MIN_VALUE_TAU, 
					P_MAX_VALUE_TAU == 0? MAX_VALUE_TAU : P_MAX_VALUE_TAU,
							P_MIN_VALUE_TAU == 0? MIN_VALUE_TAU : P_MIN_VALUE_TAU);*/
		}

		/*Only local update when there is no change after a solution found*/
		if (dynamics instanceof ClassicalDynamic || ant.reinvlidateWhenChange()) {
			solutions.add(ant);
		}

		//System.out.print("===========" + selected + "\n");
	}

	/**
	 * If a primitive is not associated with the objective, then do not need to update its tau
	 * @param globalBest
	 * @return
	 */
	public void globalUpdate() {
		//System.out.print("Start ******MAX: " + Structure.MAX_VALUE_TAU + ", MIN: " + Structure.MIN_VALUE_TAU + "\n");
		
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		// synchronize the access to the graph
		synchronized (getWriteLock()) {

			while (getWriteLock().get() != 0) {
				try {
					getWriteLock().wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Ant localBestAnt = solutions.peek();
			Ant globalBestAnt = dynamics.getBestSoFar();
			
			// If the current run's best ant is the best ever one, then set it as the global best.
			// So the update value would be 1, which is quite a great reward.
			if (globalBestAnt == null || localBestAnt.isBetter(globalBestAnt)) {
				globalBestAnt = localBestAnt;
			} 
				
			//System.out.print(globalBestAnt.getValue() +" : "+ localBestAnt.getValue() + "********* compare \n");
			globalBestAnt.print();
			
			
			LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> decision = localBestAnt
					.getDecision();

			updateTrailLimits(localBestAnt.getValue());
			//updateTrailLimits(localBestAnt.getValue(), globalBestAnt.getValue());
		
			for (Map.Entry<ControlPrimitive, Tuple<Integer, Double>> entry : decision
					.entrySet()) {
			
				// If it is not associated with the objective, then do not need to update tau
				// even it is selected
				if (!primitives.containsKey(entry.getKey())) {
					continue ;
				}
				
				
				antValues.update(primitives.get(entry.getKey()), entry.getValue()
						.getVal1(), globalBestAnt.getValue(), localBestAnt.getValue(),
						objective.isMin(), MAX_VALUE_TAU, MIN_VALUE_TAU,P_MAX_VALUE_TAU == 0? MAX_VALUE_TAU : P_MAX_VALUE_TAU,
								P_MIN_VALUE_TAU == 0? MIN_VALUE_TAU : P_MIN_VALUE_TAU);
			}
			
			dynamics.updateShort(solutions, antValues, primitives);
			dynamics.updateLong(solutions); //dynamics.updateLong(localBestAnt);
		
			
			
			solutions.clear();
			
		}
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

	}


	public double[] getValueProbability(int j, double[][] mu) {
		double sum = 0;
		int length = antValues.getTaus()[j].length;
		for (int k = 0; k < length; k++) {
			sum += Math.pow(mu[j][k], BETA)
					* Math.pow(antValues.getTaus()[j][k], ALPHA);
		}
		
		

		double[] results = new double[length];
		for (int k = 0; k < length; k++) {
			results[k] = Math.pow(mu[j][k], BETA)
					* Math.pow(antValues.getTaus()[j][k], ALPHA) / sum;
		}

		return results;
	}

	public List<Ant> getFronts() {
		List<Ant> ants = null;
		synchronized (getWriteLock()) {
			getWriteLock().incrementAndGet();
		}

		// This would actually get a list with only one element.
		ants = dynamics.getFronts();
		
		synchronized (getWriteLock()) {
			getWriteLock().decrementAndGet();
			if (getWriteLock().get() == 0) {
				getWriteLock().notifyAll();
			 }
		}
		
		// Remove invalid solutions.
	//	invalidate(ants);
        //System.out.print(objective.getName()+ "==========\n");
		
		//antValues.print();
		
		return ants;
	}

	
	public double predict(double[] xValue) {
		return objective.predict(xValue);
	}
	
	public void doDynamicAdapation(){
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		synchronized (getWriteLock()) {
			
			while (getWriteLock().get() != 0) {
				try {
					getWriteLock().wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			/*
			 * This can avoid ant's value set before model update,
			 * but dynamic adaptation taken place after the localupdate
			 
			for(int i = 0; i < ants.length; i++)
		    {
		            ants[i].evaluate();
		    }*/
			reinvalidate();
			dynamics.copeDynamics(antValues, primitives);
		}
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
	}
	
	/**
	 * An alternative function to doDynamicAdapation().
	 * @return
	 */
	public void reinvlidateWhenChange(){
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		synchronized (getWriteLock()) {
			
			while (getWriteLock().get() != 0) {
				try {
					getWriteLock().wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			reinvalidate();
			dynamics.reinvalidate();
		}
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
	}
	
	public Ant getBestSoFar (){
		return dynamics.getBestSoFar();
	}
	
	public Objective getObjective (){
		return objective;
	}
	
	public List<Tuple<Primitive, Double>> getObjectiveInputList (){
		return constraintedObjectiveMap.get(objective);
	}
	
	/**
	 * This should be inserted order
	 * @return
	 */
	public Set<ControlPrimitive> getPrimitives(){
		return primitives.keySet();
	}

	public AtomicInteger getWriteLock() {
		return writeLock;
	}
	
	private void updateTrailLimits(double current, double best){
		
		P_MAX_VALUE_TAU = MAX_VALUE_TAU;
		P_MIN_VALUE_TAU = MIN_VALUE_TAU;
		if (objective.isMin()) {
			MAX_VALUE_TAU = 1 / ((1 + current - best)*(/*1 -*/ VALUE_EVAPORATION));
			
			//MAX_VALUE_TAU = 1 / (value*(1 - VALUE_EVAPORATION));
			MIN_VALUE_TAU = MAX_VALUE_TAU / g;
		} else {
			MAX_VALUE_TAU = 1 / ((1+(1/current) - (1/best))*(/*1 -*/ VALUE_EVAPORATION));
			//MAX_VALUE_TAU = value*(1 - VALUE_EVAPORATION);
			MIN_VALUE_TAU = MAX_VALUE_TAU / g;
		}
		
		print();
	}
	
	private void updateTrailLimits(double value){
		
		P_MAX_VALUE_TAU = MAX_VALUE_TAU;
		P_MIN_VALUE_TAU = MIN_VALUE_TAU;
		if (objective.isMin()) {
			MAX_VALUE_TAU = 1 / ((value)*(1 - VALUE_EVAPORATION));
			
			//MAX_VALUE_TAU = 1 / (value*(1 - VALUE_EVAPORATION));
			MIN_VALUE_TAU = MAX_VALUE_TAU / g;
		} else {
			MAX_VALUE_TAU = 1 / (((1/value))*(1 - VALUE_EVAPORATION));
			//MAX_VALUE_TAU = value*(1 - VALUE_EVAPORATION);
			MIN_VALUE_TAU = MAX_VALUE_TAU / g;
		}
		
		print();
	}

	/**
	 * Reinvalidate the solution queue in the current iteration.
	 */
	private void reinvalidate(){
		final List<Ant> longL = new LinkedList<Ant>();
		
		for (Ant ant : solutions) {
			if(ant.reinvlidateWhenChange()){
				longL.add(ant);
			}
			
		}
		
		solutions.clear();
		// We need to mannully resort the list.
		solutions.addAll(longL);
	}
	
	private void print(){
		//System.out.print(objective.getName() + " MAX: " + MAX_VALUE_TAU + ", MIN: " + MIN_VALUE_TAU + "\n");
	}
}
