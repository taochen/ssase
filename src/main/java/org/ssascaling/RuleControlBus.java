package org.ssascaling;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.ssascaling.analyzer.Analyzer;
import org.ssascaling.executor.Executor;
import org.ssascaling.executor.VM;
import org.ssascaling.monitor.Monitor;
import org.ssascaling.objective.Cost;
import org.ssascaling.objective.Objective;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.qos.QualityOfService;
import org.ssascaling.region.SuperRegionControl;
import org.ssascaling.util.Repository;
import org.ssascaling.util.SSAScalingThreadPool;

public class RuleControlBus extends ControlBus {
	
	private final static AtomicInteger lock = new AtomicInteger(-1);
	
	private static long expectedSample = Monitor.getNumberOfNewSamples();
	private static List<Objective> objectivesToBeOptimized = null;

	public static void begin(DataInputStream is){
		// This is the Monitor, do not detect symptons but record historical data.
		/*
		 *The M part 
		 ***/
		
		long samples = Monitor.write(is);
		boolean ifAnalyze = samples == 0? false : true;
		if (!ifAnalyze) {
			return;
		}
		
		synchronized(lock) {
			
			// We do not allow more than two MAPEs loop that one for current processing and one for waiting,			
			// the subsequent MAPE would give the samples for the waiting MAPE to process.
			/*if (isThereIsMAPEwaiting) {
				if (targetSample < samples) {
					targetSample = samples;
				}
				return;
			} else {
				isThereIsMAPEwaiting = true;
			}*/
			
			
			// Ensure only one MAPE loop running at a time.
			// Can not just get rid of as this may be newly measured data.	
				while (lock.get() != -1 || samples != expectedSample) {
					System.out.print("**** this one is waiting: " + samples  + "\n");
					try {
						//System.out.print("========================== Break ===============================\n");
						//System.out.print(Thread.currentThread() + " break\n");
						//System.out.print("========================== Break ===============================\n");
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					
				}
				System.out.print("**** is trigger analyzer: " + samples  + "\n");
				System.out.print("**** is trigger analyzer, expected: " + expectedSample  + "\n");
				
				//System.out.print("========================== Run ===============================\n");
				//System.out.print(Thread.currentThread() + " running\n");
				//System.out.print("========================== Run ===============================\n");
				lock.set(0);
				
				/*isThereIsMAPEwaiting = false;
				if (targetSample != 0 ) {
					samples = targetSample;
					targetSample = 0;
				}*/
				expectedSample = samples + Monitor.getNumberOfNewSamples();
			
		}
		
		

		if (ifAnalyze) {
			System.out.print("***** will trigger training *********\n");
			/*
			 *The A part 
			 ***/
			objectivesToBeOptimized = doAnalysis(samples);
		}
		
		Set<Primitive> decreased = new HashSet<Primitive>();
		Set<Primitive> increased = new HashSet<Primitive>();
		
		
		for (final Objective obj : objectivesToBeOptimized) {
			
			if (obj instanceof Cost) {
				decreased.addAll(((Cost)obj).getPrimitivesInput());
			} else {
				for (Primitive p : Repository.getDirectPrimitives(obj)) {
					if (p instanceof ControlPrimitive) {
						increased.add(p);
					}
				}
				
			}
		}
		
		objectivesToBeOptimized = null;
		
		Set<Primitive> removed = new HashSet<Primitive>();
		for (Primitive p : increased) {
			if (decreased.contains(p)) {
				removed.add(p);
			}
		}
		
		increased.removeAll(removed);
		decreased.removeAll(removed);
		
		
		LinkedHashMap<ControlPrimitive, Double> decisions = new LinkedHashMap<ControlPrimitive, Double>();
		
		for (Primitive p : increased) {
			ControlPrimitive cp = (ControlPrimitive)p;
			if (cp.getProvision() + cp.getDifference() >= cp.getValueVector()[cp.getValueVector().length - 1]) {
			      decisions.put(cp, cp.getValueVector()[cp.getValueVector().length - 1]);
			} else {
				  decisions.put(cp, cp.getProvision() + cp.getDifference());
			}
		}
		
		for (Primitive p : decreased) {
			ControlPrimitive cp = (ControlPrimitive)p;
			if (cp.getProvision() - cp.getDifference() <= cp.getValueVector()[0]) {
			    decisions.put(cp, cp.getValueVector()[0]);
			} else {
				decisions.put(cp, cp.getProvision() - cp.getDifference());
			}
		}
		
		
		Executor.execute(decisions);
		Executor.print();
	}
	
	
	
	public static List<Objective> doAnalysis(long samples){
		
		
		// should block the training of new models of different threads, (This has been done by using the lock
		// in ControlBus class).
		// Trigger detectSymptoms only when all models have been trained.
		updateModels(samples);
		
		doResetValues();
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
	

	private static  List<Objective> detectSymptoms(){
		
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
		 
		
		 return result;
		 
	}
}
