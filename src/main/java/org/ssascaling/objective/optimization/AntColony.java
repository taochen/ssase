package org.ssascaling.objective.optimization;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.ssascaling.objective.Objective;
import org.ssascaling.observation.event.ModelChangeEvent;
import org.ssascaling.observation.listener.ModelListener;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.util.Triple;
import org.ssascaling.util.Tuple;

public abstract class AntColony implements  ModelListener {

	public static final boolean ifConsiderDynamic = false;
	    // Herustic factor
	    protected double[][] mu;
	    
	    // Only the CPs for objectives within optimization, not include the reduced ones.
	    //**************** same order ***************
	    protected List<ControlPrimitive> primitives;
	    // value of the CPs for objectives within optimization.
	   // protected double[] initilisedValues;
	    //**************** same order ***************
	    
	    
	    // Sorted, list of integer needs to fit the sequential requirement of objective's inputs. Including EPs.
	    // The tupe here need to be a unique instance.
	    protected LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> objectiveMap;
	    // The same tuple instance as objectiveMap. Including EPs.
	    protected LinkedHashMap<Primitive, Tuple<Primitive, Double>> objectivePrimitiveMap;
	    
	    // The objectives that needs to be checked in invalidation.
	    // This does not include the objective within scalarized objective, as in such case
	    // there will be a global constraint
	    
	    // Double here is the original value
	    // This should include EP as well.
	    // Including EPs.
	    protected LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> constraintedObjectiveMap;
	    
	    // May be the concurrent version is better
	    // This size should be the same as the number of objective in objectiveMap
	    protected Set<Structure> structures = new HashSet<Structure>();
	    
	    // Used to get bestSoFar ant for testing when dynamics occur.
	    protected Map<Objective, Structure> OSMap = new HashMap<Objective, Structure>();
	    
	    // Include the decomposition of scaled objective, including reduced ones.
	    private int totalNumberOfObjective;
	    
	    protected Ant[]    ants;
	    protected int      numberOfAnt;
	    protected int      antRatio = 5;
	    protected int      iterCounter = 0;
	    protected int      iterations = 20;
	    
	    protected int completedAnt=0;
	    
	     
	    
	    private int      nID;
	    private Boolean isStop = false;
	    private Boolean reEvaluate = false;
	    private Boolean triggerDynamics = false;
	    // Ensure to trigger dynamics adapation only when all models are up-to-date. 
	    // In case where failure occur, this instance is invalid anyway as the optimization thread
	    // would be stopped by the Cloud.
	    private AtomicInteger knownConunter = new AtomicInteger(0);
	    // Mainly for maintain MU
	    private AtomicInteger writeLock = new AtomicInteger(0);
	    //private static int s_nIDCounter = 0;
	    
	    public AntColony(int nID,
	    		List<ControlPrimitive> primitives,
	    		LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> objectiveMap,
	    		LinkedHashMap<Objective, List<Tuple<Primitive, Double>>> constraintedObjectiveMap)
	    {
	        this.nID = nID;
	        this.primitives = primitives;
	        this.objectiveMap = objectiveMap;
	        this.constraintedObjectiveMap = constraintedObjectiveMap;
	        
	        numberOfAnt = objectiveMap.size() * antRatio;
	      
	        objectivePrimitiveMap = new LinkedHashMap<Primitive, Tuple<Primitive, Double>>(); 
	        for (Map.Entry<Objective, List<Tuple<Primitive, Double>>> entry : objectiveMap.entrySet()){

	        	final Map<ControlPrimitive, Integer> primitivesMap = new  HashMap<ControlPrimitive, Integer>();
	        	
	        	for (Tuple<Primitive, Double> tuple : entry.getValue()) {
	        		
	        		// Find the CP and its index in primitives for each objective.
	        		if (tuple.getVal1() instanceof ControlPrimitive) {
	        			primitivesMap.put((ControlPrimitive) tuple.getVal1() , primitives.indexOf(tuple.getVal1() ));
	        		}
	        		
	        		objectivePrimitiveMap.put(tuple.getVal1(), tuple);
	        	}
	        	
	        	
	        	Structure s = new Structure(entry.getKey(), primitives, primitivesMap, 
	        			constraintedObjectiveMap);
	        	structures.add(s);
	        	OSMap.put(entry.getKey(), s);
	        }
	        
	        calculateMu();
	        
	        
	        for (int i = 0; i < mu.length; i++) {
	        	double min = 0.0;
	        	 for (int j = 0; j < mu[i].length; j++) {
	        		 if(min == 0 || (mu[i][j] >0 && mu[i][j] < min)) {
	        			 min = mu[i][j];
	        		 }
	        	 }
	        	 
	        	 // In case no value in this primitive could have improvement.
	        	 if (min == 0.0) {
	        		 min = 1.0;
	        	 }
	        	 
	        	 for (int j = 0; j < mu[i].length; j++) {
	        		 if (mu[i][j] < 0) {
	        			 mu[i][j]  = Math.abs(min / mu[i][j]);
	        		 }
	        	 }
	        	
	        }
	    
	        print();
	    }
	    
	    private void print(){	        
	    	for (int i = 0; i < mu.length; i++) {
	    		System.out.print(Arrays.toString(mu[i]) + "\n");
	    	}
	    }
	    
	    public  void calculateMu(){
	    	synchronized(writeLock){
	    		
	    		while (writeLock.get() != 0) {
					try {
						writeLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
	    	mu = new double[primitives.size()][];
	    	double[] original = calculateObjectiveValues();
	    	
	    
	    	for (int i = 0; i < primitives.size(); i++) {
	    		double[] nested = new double[primitives.get(i).getValueVector().length];
	    		for (int j = 0; j < primitives.get(i).getValueVector().length; j++) {
	    			// Keep the original value
	    			double temp = objectivePrimitiveMap.get(primitives.get(i)).getVal2();
	    			objectivePrimitiveMap.get(primitives.get(i)).setVal2(primitives.get(i).getValueVector()[j]);
	    			double result = calculateMuRatio(original);
	    			nested[j] = result;
	    			// Reset the original value
	    			objectivePrimitiveMap.get(primitives.get(i)).setVal2(temp);
	    		}
	    	
	    		mu[i] = nested;
	    	}
	    	}
	    }
	    
	    public synchronized LinkedHashMap<ControlPrimitive, Double> doOptimization()
	    {
	        
	        
	        iterCounter = 0;
	      
	        
	        // loop for all iterations
	        while(iterCounter < iterations && !isStop)
	        {
	        	System.out.print("Run " + iterCounter + "\n");
	        	// creates all ants
		        ants  = createAnts(numberOfAnt);
	            // run an iteration
	            iteration();
	            try
	            {
	                wait();
	            }
	            catch(InterruptedException ex)
	            {
	                ex.printStackTrace();
	            }
	            
	            synchronized(isStop) {
	               if (isStop) {
	            	   return null;
	               }
	            }
	            
	            // Trigger 
	            globalUpdatingRule();
	            completedAnt = 0;
	        }
	        
	        
	        synchronized(isStop) {
	               if (isStop) {
	            	   return null;
	               }
	               isStop = true;
	        }
	      
	        
	        // If model change down to this stage, then we really can do nothing
	        // since the optimization is finished.
	        return getParetoOptimality();
	       /* if(m_nIterCounter == m_nIterations)
	        {
	            m_outs.close();
	        }*/
	    }
	    
	    private void iteration()
	    {
	       
	    	iterCounter++;
	        //m_outs.print(m_nIterCounter);
	        for(int i = 0; i < ants.length; i++)
	        {
	            ants[i].start();
	        }
	    }
	    
	    public Set<Structure>  getStrcture()
	    {
	        return structures;
	    }
	    
	    public int getAnts()
	    {
	        return ants.length;
	    }
	    
	    public int getIterations()
	    {
	        return iterations;
	    }
	    
	    public int getIterationCounter()
	    {
	        return iterCounter;
	    }
	    
	    public int getID()
	    {
	        return nID;
	    }
	    
	   /* public synchronized void update(Observable ant, Object obj)
	    {
	        //m_outs.print(";" + ((Ant)ant).m_dPathValue);
	        m_nAntCounter++;
	        
	        if(m_nAntCounter == m_ants.length)
	        {
	            m_outs.println(";" + Ant.s_dBestPathValue + ";" + m_graph.averageTau());
	            
	            //            System.out.println("---------------------------");
	            //            System.out.println(m_iterCounter + " - Best Path: " + Ant.s_dBestPathValue);
	            //            System.out.println("---------------------------");
	            
	            
	            
	            notify();
	            
	        }
	    }*/
	    
	
	    public synchronized void antCompleted(){
	    	completedAnt++;
	    	if (completedAnt == numberOfAnt) {
	    		this.notifyAll();
	    	}
	    }
	    
	  
	    
	    public boolean done(int counter) {
	    	return counter == primitives.size();
	    }
	    
	    public ControlPrimitive getPrimitive (int i){
	    	return primitives.get(i);
	    }
	    
	    public int getPrimitiveSize (){
	    	return primitives.size();
	    }
	    
	    public Set<Objective> getObjectives(){
	    	return objectiveMap.keySet();
	    }
	    
	    public double[] calculateFront (LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> cpInput){
	    	
	    	int index = 0;
	    	double[] front = new double[objectiveMap.size()];
	    	for (Map.Entry<Objective, List<Tuple<Primitive, Double>>> entry : objectiveMap.entrySet()){
	    		double[] xValue = new double[entry.getValue().size()];
	    		
	    		for (int i = 0; i < entry.getValue().size() ;i++) {
	    			Primitive p = entry.getValue().get(i).getVal1();
	    			
	    			// Means it has to been a value in this ant's solution as it is CP.
	    			if (p instanceof ControlPrimitive) {
	    				xValue[i] = cpInput.get(p).getVal2();
	    			// Otherwise, if it is a EP then use its original value as we can not control EP.
	    			} else {
	    				xValue[i] = entry.getValue().get(i).getVal2();
	    			}
	    			
	    		}
	    		
	    		front[index] = entry.getKey().predict(xValue);
	    		index++;
	    	}
	    	
	    	return front;
	    }
	    
	    /**
	     * This would normalized the front point by divide it to a max value of the point.
	     * @param cpInput
	     * @param max
	     * @return
	     */
	    public double[] calculateFront (LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> cpInput, double[] max){
	    	
	    	int index = 0;
	    	double[] front = new double[objectiveMap.size()];
	    	for (Map.Entry<Objective, List<Tuple<Primitive, Double>>> entry : objectiveMap.entrySet()){
	    		double[] xValue = new double[entry.getValue().size()];
	    		
	    		for (int i = 0; i < entry.getValue().size() ;i++) {
	    			Primitive p = entry.getValue().get(i).getVal1();
	    			
	    			// Means it has to been a value in this ant's solution as it is CP.
	    			if (p instanceof ControlPrimitive) {
	    				xValue[i] = cpInput.get(p).getVal2();
	    			// Otherwise, if it is a EP then use its original value as we can not control EP.
	    			} else {
	    				xValue[i] = entry.getValue().get(i).getVal2();
	    			}
	    			
	    		}
	    		
	    		front[index] = entry.getKey().predict(xValue)/max[index];
	    		index++;
	    	}
	    	
	    	return front;
	    }
	    
	   
        public double[] calculateFront (LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> cpInput, double[] max, boolean isForProduct){
	    	
	    	int index = 0;
	    	double[] front = new double[objectiveMap.size()];
	    	for (Map.Entry<Objective, List<Tuple<Primitive, Double>>> entry : objectiveMap.entrySet()){
	    		double[] xValue = new double[entry.getValue().size()];
	    		
	    		for (int i = 0; i < entry.getValue().size() ;i++) {
	    			Primitive p = entry.getValue().get(i).getVal1();
	    			
	    			// Means it has to been a value in this ant's solution as it is CP.
	    			if (p instanceof ControlPrimitive) {
	    				xValue[i] = cpInput.get(p).getVal2();
	    			// Otherwise, if it is a EP then use its original value as we can not control EP.
	    			} else {
	    				xValue[i] = entry.getValue().get(i).getVal2();
	    			}
	    			
	    		}
	    		
	    		front[index] = isForProduct? entry.getKey().isMin()? (100 - (entry.getKey().predict(xValue)*100/max[index])) : entry.getKey().predict(xValue)*100/max[index]
	    		                                                                                            : entry.getKey().predict(xValue)/max[index];
	    		index++;
	    	}
	    	
	    	return front;
	    }
	    
	    /**
	     * This should be a different thread from the model change thread. 
	     * 
	     * So that the global update would get deadlock in the case where it is waiting for the 
	     * model change lock in prediction whereas this one is waiting for the structure lock.
	     * 
	     * 
	     * The objectives include those reduced onces as well.
	     * 
	     * 
	     * Unlike non-critical change, which can still be a valid solution even if the dynamic has not been
	     * resolved. Critical change could result in an invalid solution, which needs to be avoided.
	     */
	    public void updateWhenModelChange(ModelChangeEvent event) {
	    	
	    	synchronized(isStop) {  
    			// If it is stop already.
    			if (isStop) {
    				return;
    			}
    		}
	    
	    	// Many models associated with AntColony, but only needs to trigger once for dynamic adapation.
	    	synchronized(knownConunter) { 
	    		knownConunter.incrementAndGet();
	    		/**
			     * If the changed model is a constraint one, then simply redo the evaluation.
			     */
		    	if (!objectiveMap.containsKey(event.getObjective())) {
		    		// This is the action following by a change, although different thread, it is still 
			    	// unlikely to have further change would occur.	
		    		if (!reEvaluate) {
		    		     reEvaluate = isTriggerDynamicAdaptaion(event);
		    		}
		    	} else {
		    		if (!triggerDynamics) {
		    		     triggerDynamics = isTriggerDynamicAdaptaion(event);
		    		}
		    	}
		    	
    	     	// The last one should trigger.
		    	// We do not need to reset knownConunter to 0.
    	     	if (totalNumberOfObjective%knownConunter.get() != 0) {
    	     		return;
    	     	}
    		}
	    	
	    
	    	
	    	// When the last one to trigger and if it find out there is a critical change
	    	// it just give it up.
	    	
    		// If it is stop already, no sync needed as it is the only one
	    	// one change would trigger all dynamics adaptation as it needs to evaluate constraints
	    	// within all structures. 
    		if (isStop) {
    	    	reEvaluate = false;
    	    	triggerDynamics = false;
    	    	
    			return;
    		}
			
			// No sync needed, as it would be the last one
    		if (!triggerDynamics && reEvaluate) {
    		   evaluateConstraints();
    		}
    		
    		if (triggerDynamics) {
    		   // Cope with dynamics also include reevaluate constraints.
	    	   dynamicAdapation();
    		}
	    	
	    	reEvaluate = false;
	    	triggerDynamics = false;
	    	
	    	
	    }
	    
	    public double[][] getMu(){
	    	return mu;
	    }
	    
	    public AtomicInteger getWriteLock() {
			return writeLock;
		}
	    
	    protected abstract Ant[] createAnts(int ants);
	    
	    protected abstract void globalUpdatingRule();
	    
	    protected abstract boolean invalidate(Ant ant, LinkedHashMap<ControlPrimitive, Tuple<Integer, Double>> cpInput);
	    
	    protected abstract Structure selectStrcture(int i,Structure[] structures);
	    
	    protected abstract LinkedHashMap<ControlPrimitive, Double> getParetoOptimality();
	    
	    protected abstract void dynamicAdapation();
	    
	    protected abstract Ant buildAnt(Structure structure);
	    /**
	     * If the changed model is a constraint one, then simply redo the evaluation.
	     */
	    protected abstract void evaluateConstraints();
	    	
	    
	    
		private boolean isTriggerDynamicAdaptaion(ModelChangeEvent event){
		
			// If it is critical changes, kill the threads as this optimization is invlaid
	    	// critical refers to changes on when and which primitives correlates to QoS, and constraints changes
	    	// if it is changes on how primitives correlates to QoS, then we can trigger the dynamic
	    	// mechanism. 
	    	if (event.isCritical()) {
	    		synchronized(isStop) {  
	    			// If it is stop already.
	    			if (isStop) {
	    				return false;
	    			}
	    	     	isStop = true;
	    		}
	    		
	    		for (Ant ant : ants) {
	    			ant.stop();
	    		}
	    		
	    		synchronized(this) { 
	    		 this.notifyAll();
	    		}
	    		// Return as it is dead anyway.
	    		return false;
	    	} 
	    	
			
			Ant ant =  OSMap.get(event.getObjective()).getBestSoFar();
			
			//Means just start.
			if (ant == null) {
				return false;
			}
			
			return event.getObjective().isChangeSignificant(ant.getSnapshotedValue(), ant.getValue());
		}
	    
	    private double calculateMuRatio(double[] original){
	    	double improvement = 0;
	    	double degradation = 0;
	    	//System.out.print("=================\n");
	    	
	    	double[] values =  calculateObjectiveValues();
	    	
	    	
	    	
	    	Iterator<Map.Entry<Objective, List<Tuple<Primitive, Double>>>> itr =  objectiveMap.entrySet().iterator();
	    	
	    	for (int i =0; i < original.length;i++) {
	    		//System.out.print("Mu " + values[i] + " : " + original[i] + "\n");
	    		boolean isMin = itr.next().getKey().isMin();
	    		if (isMin) {
	    			if(values[i] < original[i]) {
	    				improvement += Math.abs(values[i] - original[i])/original[i];
	    			} else if (values[i] > original[i]) {
	    				degradation += Math.abs(values[i] - original[i])/original[i];
	    			}
	    		} else {
	    			if(values[i] > original[i]) {
	    				improvement += Math.abs(values[i] - original[i])/original[i];
	    			} else if (values[i] < original[i]) {
	    				degradation += Math.abs(values[i] - original[i])/original[i];
	    			}
	    		}
	    	}
	    	
	    	//System.out.print("Im " + improvement+ "\n");
    		//System.out.print("De " + degradation+ "\n");
    		//System.out.print("=================\n");
	    	return improvement==0? (-1*(degradation + 1)) : improvement/(degradation + 1);
	    }
	    
	    
	    private double[] calculateObjectiveValues(){
	    	double[] results = new double[objectiveMap.size()];
	    	int k = 0;
	    	for (Map.Entry<Objective, List<Tuple<Primitive, Double>>> entry : objectiveMap.entrySet()){
	    		double[] inputs = new double[entry.getValue().size()];
	    		for (int i =0; i < inputs.length;i++) {
	    			inputs[i] = entry.getValue().get(i).getVal2();
	    		}
	    		System.out.print("Inputs: " + Arrays.toString(inputs) + "\n");
	    		results[k] = entry.getKey().predict(inputs);
	    		k++;
	    	}
	    	
	    	return results;
	    }
	

}
