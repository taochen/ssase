package org.ssascaling.objective.optimization;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;

import org.ssascaling.objective.Objective;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.util.Tuple;
import org.ssascaling.util.Util;


public abstract class Ant extends Observable implements Runnable, Comparable<Ant>, Cloneable {
	protected String antID;

	// Decision of this ant
	// Should be the same order as primitives in AntColony, this include any primitives in the final pareto optimality,
	// for the primitives associated with 'this' objective, see the 'primitives' in associated structure.
	// Triple<Integer, Double> index of CP value and value of CP
	
	// Double here is the original value
	// Changed to concurrent?
	protected LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> cpInput = new LinkedHashMap<ControlPrimitive,  Tuple<Integer, Double>>();
	protected LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> bestCpInput = new LinkedHashMap<ControlPrimitive,  Tuple<Integer, Double>>();
	protected double bestEverResult = 0.0;
	protected Structure strcture;

	protected Observer observer;
	//private static int antIDCounter = 0;

	protected int pathCounter = 0;

	// The objective's output, used for test when dynamic occur.
	protected Double snapshotedValue;
	
	// The objectives' outputs, order is the same as objectiveMap in AntColony
	protected double[] front;
	
	// The values extract from cpInput
	protected double[] paretoSetValues;
	
	private int maxRun = 100;
	private int currentRun = 0;
	
	
	private Thread thread;
	/*
	 * protected int[][] m_path; protected int m_nCurNode; protected int
	 * m_nStartNode; protected double m_dPathValue; protected Observer
	 * m_observer;
	 * 
	 * 
	 * private static int s_nAntIDCounter = 0; private static PrintStream
	 * s_outs;
	 */

	protected AntColony antColony;

	/*
	 * public static double s_dBestPathValue = Double.MAX_VALUE; public static
	 * Vector s_bestPathVect = null; public static int[][] s_bestPath = null;
	 * public static int s_nLastBestPathIteration = 0;
	 */

	public void setAntColony(AntColony antColony) {
		this.antColony = antColony;
	}

	/*
	 * public static void reset() { s_dBestPathValue = Double.MAX_VALUE;
	 * s_bestPathVect = null; s_bestPath = null; s_nLastBestPathIteration = 0;
	 * s_outs = null; }
	 * 
	 * public Ant(int nStartNode, Observer observer) { s_nAntIDCounter++;
	 * m_nAntID = s_nAntIDCounter; m_nStartNode = nStartNode; m_observer =
	 * observer; }
	 */

	public Ant(String antID, AntColony antColony, Structure strcture) {
		this.antID = antID;
		this.antColony = antColony;
		this.strcture = strcture;
	}

	public void start() {
		thread = new Thread(this);
		thread.setName("Ant " + antID);
		thread.start();
	}

	public void stop() {
		thread.interrupt();
	}
	
	public void run() {
		//final Strcture strcture = antColony.getStrcture()[0];

		// The sequence of CP is determined in the primitives of AntColony
		// This is because we invalidate only when a solution is completed, therefore
		// the sequence does not matter at all.
		do {

			pathCounter = 0;
			cpInput.clear();
			currentRun ++;
			while (!end()) {
				
				Tuple<Integer, Double> newValue;
				
				// This is for maintain consistency on MU updates.
				synchronized (antColony.getWriteLock()) {
					antColony.getWriteLock().incrementAndGet();
				}

				// This is for maintain consistency on TAU updates.
				// synchronize the access to the graph by locking
				synchronized (strcture.getWriteLock()) {
					strcture.getWriteLock().incrementAndGet();
				}

				
				// apply the State Transition Rule
				newValue = valueTransitionRule(pathCounter);
				
				// synchronize the access to the graph by unlocking
				synchronized (strcture.getWriteLock()) {
					strcture.getWriteLock().decrementAndGet();
					if (strcture.getWriteLock().get() == 0) {
						strcture.getWriteLock().notifyAll();
					 }
				}
				
				synchronized (antColony.getWriteLock()) {
					antColony.getWriteLock().decrementAndGet();
					if (antColony.getWriteLock().get() == 0) {
						antColony.getWriteLock().notifyAll();
					 }
				}
				
				// add the current node the list of visited nodes
			
				cpInput.put(antColony.getPrimitive(pathCounter), new Tuple<Integer, Double>( newValue.getVal1(), newValue.getVal2()));
				
				// update the current node
				pathCounter++;
			}

		} while (!antColony.invalidate(this, cpInput) && currentRun <= maxRun);

		// If the ant die
		if (currentRun >= maxRun) {
			cpInput = bestCpInput;
		}
		
		//System.out.print(cpInput.size() + "***************** Finsihed an Ant " + "\n");
		// Do not put in to the sync of structrue as it would cause deadlock
		// with the dynamics triggering.
		snapshotedValue = evaluate();
		
		
		//System.out.print(strcture.getObjective().getName() + " is finished, achevied value: " + snapshotedValue  + "\n");
		
		
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		synchronized (strcture.getWriteLock()) {
			
			while (strcture.getWriteLock().get() != 0) {
				try {
					strcture.getWriteLock().wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		
			// apply the Local Updating Rule when it is an acceptable solution
			// update tau only if the CP is within the target objective.
			localUpdatingRule();
		}
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
		
		
		
		antColony.antCompleted();
		// update the observer
		//observer.update(this, null);

	}

	public LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> getDecision(){
		return cpInput;
	}
	
	/**
	 * We can snapshot this for performance improvement,  even the change occur it does
	 * not really matter since the optimizaiton is completed. So this can maintain consistency
	 * @return
	 */
	public double[] getFront(){
		if(front == null) {
			front = antColony.calculateFront(cpInput);
		}
		
		return front;
	}
	
	public double[] getFront(double[] max){
		if(front == null) {
			front = antColony.calculateFront(cpInput, max);
		}
		
		return front;
	}
	
	public abstract Tuple<Integer, Double> valueTransitionRule(int r);

	public abstract void localUpdatingRule();

	public abstract boolean end();

	/**
	 * Have to calculate on the fly, to avoid model change during the sort of ants during
	 * localUpdate and globalUpdate.
	 * @return
	 */
	public double getValue(){
		
		if (!AntColony.ifConsiderDynamic) {
			return getSnapshotedValue();
		}
		
		return evaluate();
	}
	
	public boolean selfInvalidate(){
		return antColony.invalidate(null, cpInput);
	}
	
	public int noOfViolatedObjective(){
		return antColony.invalidate(cpInput);
	}
	
	/**
	 * Used to evaluate on completed ants, so that it can decide 
	 * if there is need to trigger dynamics. 
	 * @return
	 */
	public double getSnapshotedValue(){
		return snapshotedValue;
	}
	
	public boolean paretoDominates (Ant another) {
		// Should be the same as the size of objectives
		double[] front = getFront();
		int index = 0;
		Set<Objective> set = antColony.getObjectives();
		for (Objective obj : set){
			if (!obj.isBetter(front[index], another.getFront()[index])){
				return false;
			}
			
			index++;
		}
		
		return true;
	}
	
	public boolean nashDominates (Ant another) {
		// Should be the same as the size of objectives
		double[] front = getFront();
		int index = 0;
		Set<Objective> set = antColony.getObjectives();
		int countA = 0;
		int countB = 0;
		for (Objective obj : set){
			if (!obj.isBetter(front[index], another.getFront()[index])){
				countB ++;
			} else if (!obj.isBetter(another.getFront()[index], front[index])){
				countA ++;
			}
			
			index++;
		}
		
		return countA > countB;
	}
	
	public int[] c_measure (Ant another) {
		// Should be the same as the size of objectives
		double[] front = getFront();
		int index = 0;
		Set<Objective> set = antColony.getObjectives();
		int countA = 0;
		int countB = 0;
		for (Objective obj : set){
			if (!obj.isBetter(front[index], another.getFront()[index])){
				countB ++;
			} else if (!obj.isBetter(another.getFront()[index], front[index])){
				countA ++;
			}
			
			index++;
		}
		
		return new int[]{countA, countB};
	}
	
	
	public void print(){
		/*for (Map.Entry<ControlPrimitive, Tuple<Integer, Double>> e : cpInput.entrySet()) {
			   System.out.print(e.getKey().getAlias() + ", " + e.getKey().getName() + ", value: " + e.getValue().getVal2() +  "\n");
		}*/
	}
	
	public boolean isBetter(Ant another){
		return strcture.getObjective().isBetter(getValue(), another.getValue());
	}
	
	public double solutionSimilarityValue(Ant another){
		return Util.calculateEuclideanDistance(getParetoSetValues(), another.getParetoSetValues());
	}
	
	/**
	 * Only call this after this ant completed.
	 * @return
	 */
	private double[] getParetoSetValues(){
		if (paretoSetValues == null) {
			paretoSetValues = new double[cpInput.size()];
			
			int index = 0;
			for (Map.Entry<ControlPrimitive, Tuple<Integer, Double>> entry : cpInput
					.entrySet()) {

				paretoSetValues[index] = entry.getValue().getVal2();
				index++;
			}
		}
		
		return paretoSetValues;
	}
	
	private double evaluate(){
		
		List<Tuple<Primitive, Double>> list = strcture.getObjectiveInputList();
		
		double[] xValue = new double[list.size()];
	
		for (int i = 0; i < list.size(); i++) {
			
			Primitive p = list.get(i).getVal1();
			
			// Means it has to been a value in this ant's solution as it is CP.
			if (p instanceof ControlPrimitive) {
				xValue[i] = cpInput.get(p).getVal2();
			// Otherwise, if it is a EP then use its original value as we can not control EP.
			} else {
				xValue[i] = list.get(i).getVal2();
			}
			
			
			
		}
		
		// Set the value before local update
		return strcture.predict(xValue);
	}
	
	/**
	 * Used to return the final selected solution for taking actions.
	 * @return the CPs and their associated values for future adaptation
	 */
	public LinkedHashMap<ControlPrimitive, Double> getFinalResult(){
		final LinkedHashMap<ControlPrimitive, Double> map = new LinkedHashMap<ControlPrimitive, Double> ();
		
		for (Map.Entry<ControlPrimitive, Tuple<Integer, Double>> entry : cpInput.entrySet()){
			map.put(entry.getKey(), entry.getValue().getVal2());
		}
		
		return map;
	}

	/*
	 * public static int[] getBestPath() { int nBestPathArray[] = new
	 * int[s_bestPathVect.size()]; for(int i = 0; i < s_bestPathVect.size();
	 * i++) { nBestPathArray[i] =
	 * ((Integer)s_bestPathVect.elementAt(i)).intValue(); }
	 * 
	 * return nBestPathArray; }
	 */

	/**
	 * Used to check during local update, as
	 * the invalidation process itself has no syncronization.
	 * 
	 * Used this only when the ant completes its task.
	 */
	public boolean reinvlidateWhenChange(){
		double result = 0;
		if (snapshotedValue != (result = getValue())) {
			snapshotedValue = result;
			return antColony.invalidate(null, cpInput);
		}
		
		return true;
	}
	
	
	public void setBestCpInput ( LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> inputs){
		
        List<Tuple<Primitive, Double>> list = strcture.getObjectiveInputList();
		
		double[] xValue = new double[list.size()];
	
		for (int i = 0; i < list.size(); i++) {
			
			Primitive p = list.get(i).getVal1();
			
			// Means it has to been a value in this ant's solution as it is CP.
			if (p instanceof ControlPrimitive) {
				xValue[i] = inputs.get(p).getVal2();
			// Otherwise, if it is a EP then use its original value as we can not control EP.
			} else {
				xValue[i] = list.get(i).getVal2();
			}
			
			
			
		}
		
		double result = strcture.getObjective().predict(xValue);
		
		
		if (bestCpInput.size() == 0 || strcture.getObjective().isBetter(result, bestEverResult)) {
			bestEverResult = result;
			bestCpInput.clear();
			bestCpInput.putAll(inputs);
		}
	}
	
	/**
	 * Only used on immigrant ants.
	 */
	public boolean mutate(){
		Random r = new Random();
		
		Set<Integer> used = new HashSet<Integer>();
		
		int totalCP = r.nextInt(antColony.getPrimitiveSize());
		
		for (int k = 0; k < totalCP; k++) {
			
			int index = r.nextInt(antColony.getPrimitiveSize());
			
			if (used.contains(index)) {
				continue;
			}
			
			used.add(index);
			ControlPrimitive p = antColony.getPrimitive(index);
			int CPindex = r.nextInt(p.getValueVector().length);
			
			cpInput.put(antColony.getPrimitive(index), new Tuple<Integer, Double>(CPindex, p.getValueVector()[CPindex]));
			
			
			
		}
		snapshotedValue = evaluate();
		return antColony.invalidate(null, cpInput);
	}
	
	/**
	 * Using a random strategy, like mutation.
	 */
	public Object clone()  {
		Random r = new Random();
		Ant ant = antColony.buildAnt(strcture);
		ant.pathCounter = this.pathCounter;
		ant.antID = this.antID + "-Immigraint-" + r.nextInt(100);
		
		ant.cpInput.putAll(this.cpInput);
		
		return ant;
    }
	
	public Objective getObjective(){
		return strcture.getObjective();
	}
	
	public int compareTo(Ant another) { 		
		return isBetter(another)? -1 : 1;
	}
	
	public String toString() {
		return "Ant " + antID;
	}
}
