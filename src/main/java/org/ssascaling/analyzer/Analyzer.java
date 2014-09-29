package org.ssascaling.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.ssascaling.ControlBus;
import org.ssascaling.Service;
import org.ssascaling.executor.VM;
import org.ssascaling.objective.Cost;
import org.ssascaling.objective.Objective;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.HardwareControlPrimitive;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.qos.QualityOfService;
import org.ssascaling.region.SuperRegionControl;
import org.ssascaling.util.Repository;
import org.ssascaling.util.SSAScalingThreadPool;

public class Analyzer {

	
	
	// Ensure it proceeds to Plan phase only if all the models are trained and updated. 
	private final static AtomicInteger updatedModel = new AtomicInteger(0);
	
	private static boolean isReachTheLeastSamples = false;
	
	public static List<Objective> doAnalysis(long samples){
		
		
		// should block the training of new models of different threads, (This has been done by using the lock
		// in ControlBus class).
		// Trigger detectSymptoms only when all models have been trained.
		updateModels(samples);
		
		synchronized(updatedModel) {
			while (updatedModel.get() != Repository.getQoSSet().size()) {
				try {
					updatedModel.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
			}
			//System.out.print("========================== run ===============================\n");
			//System.out.print(Thread.currentThread() + " run\n");
			//System.out.print("========================== run ===============================\n");
			
			doResetValues();
			// Reset counter to zero upon finish all modeling.
			updatedModel.set(0);
		}
		// Directly go for detection, the change of model would be detected by the listeners
		// in RegionControl
		
		return detectSymptoms();
	}
	
	/**
	 * This is to train the models in adaptive multi-learners.
	 */
	private static void updateModels(long samples) {

		// Trigger the 'prepareToAddValue', 
		// Alternatively, we could get this done in Monitor. If later we need to remove the historical
		// data from memory, and only load them upon training, we can resume this method.
		
		// updatePrimitivesAndQoSFromFiles();
		// Actually adding the values.
		doAddValues(samples);
		
		for (final QualityOfService qos : Repository.getQoSSet()) {
			
			
			SSAScalingThreadPool.executeJob(new Runnable() {

				@Override
				public void run() {
					//System.out.print("***** doing training *********\n");
					boolean result = false;
					try {
						
						
					   result = qos.doTraining();
					   
					   
					} catch (RuntimeException e) {
						// Make sure the process can keep going and avoid deadlock.
						e.printStackTrace();
						synchronized(updatedModel) {
							updatedModel.incrementAndGet();
							//System.out.print("==========================  ===============================\n");
							//System.out.print(updatedModel.get() + " processed\n");
							//System.out.print("==========================  ===============================\n");
							isReachTheLeastSamples = result;
							if (updatedModel.get() == Repository.getQoSSet().size()) {
								updatedModel.notifyAll();
							}
						}
					}
					synchronized(updatedModel) {
						updatedModel.incrementAndGet();
						//System.out.print("==========================  ===============================\n");
						//System.out.print(updatedModel.get() + " processed\n");
						//System.out.print("==========================  ===============================\n");
					
						isReachTheLeastSamples = result;
						if (updatedModel.get() == Repository.getQoSSet().size()) {
							updatedModel.notifyAll();
						}
					}
					//System.out.print("***** finish doing training *********\n");
				}

			});
		}

		
	}
	
	private static void doAddValues(long samples){
		
		for (Service s : Repository.getAllServices() ) {
			for (Primitive p : s.getPrimitives()) {
				p.addValue(samples);
			}
		}
		
		for (VM v : Repository.getAllVMs()) {
			for (Primitive p : v.getAllPrimitives()){
				p.addValue(samples);
			}
		}
		
		for (final QualityOfService qos : Repository.getQoSSet()) {
			qos.doAddValue(samples);
		}
			
	}
	
	/**
	 * This is used to ensure that the 'values' attribute is empty,
	 * 
	 * so that the CP, EP and QoS can follow the workflow that uses 'value attribute'
	 */
	private static void doResetValues(){
		
		for (Service s : Repository.getAllServices() ) {
			for (Primitive p : s.getPrimitives()) {
				p.resetValues();
			}
		}
		
		for (VM v : Repository.getAllVMs()) {
			for (Primitive p : v.getAllPrimitives()){
				p.resetValues();
			}
		}
		
		for (final QualityOfService qos : Repository.getQoSSet()) {
			qos.resetValues();
		}
			
	}
	
	private static void updatePrimitivesAndQoSFromFiles(){
		// TODO read the latest data from the file, these should update newly measured
		// data of all QoS and primitives.
		 org.ssascaling.util.Util.readMeasuredData();
		// For QoS and primitives, should use prepareToAddValue here, as the actual 'add' operation is done
		// in 'updateModels' function.
	}
	
	private static  List<Objective> detectSymptoms(){
		// TODO only trigger if the current violation has been longer than t intervals and the current
		// configuration has been up and running for t intervals.
		if (!isReachTheLeastSamples /*Only detect when essential number of samples have been collected*/) {
			return null;
		}
		
		
		// TODO add proactive detection based on the QoS models.
		// as here we have only reactive detection on violation of QoS and CP utilization.
		 List<Objective> result = new ArrayList<Objective>();
		 
		 for (QualityOfService q : Repository.getQoSSet()) {
			 if (q.isViolate()) {
				 result.add(q);
			 }
		 }
		 
		 for (Cost c : Repository.getCostSet()) {
			 if (c.isViolate()) {
				 result.add(c);
			 }
		 }
		 
		 // Doing the first run of filtering objectives that belong to the same region.
		 // TODO May also communicate with other PM.
		 if (!ControlBus.isTestQoSModelingOnly) {
		    SuperRegionControl.getInstance().filterObjective(result);
		 }
		  
		 return result;
		 
	}
	
}
