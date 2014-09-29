package org.ssascaling;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.ssascaling.analyzer.Analyzer;
import org.ssascaling.executor.Executor;
import org.ssascaling.monitor.Monitor;
import org.ssascaling.objective.Objective;
import org.ssascaling.planner.Planner;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.test.modeling.Configurator;
import org.ssascaling.util.Repository;
import org.ssascaling.util.SSAScalingThreadPool;

public class ControlBus {
	
	public static final boolean isTestMonitoringOnly = false;
	public static final boolean isTestQoSModelingOnly = false;

	// Ensure only one MAPE loop running at a time.
	// This is the main lock of MAPE loop, in case the repository 
	// would change, it also need to rely on this lock.
	private final static AtomicInteger lock = new AtomicInteger(-1);
	private static List<Objective> objectivesToBeOptimized = null;
	
	private static long expectedSample = Monitor.getNumberOfNewSamples();
	
	// This is the samples that current MAPE should deal with, as when too much pending MAPE, the
	// later ones can simply abort.
	private static long targetSample = 0;
	private static boolean isThereIsMAPEwaiting = false;
	@SuppressWarnings("unused")
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
		
		System.out.print("**** MAPE start: " + samples  + "\n");
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
		
		
		// Put modeling in the Analyzer, which will also analyze symptons, the Adaptor would be trigger once model change significantly. 
	    // In case of changing super region, this should be trigger in the Analyzer, the same as previous TODO.
		if (isTestMonitoringOnly) {
			synchronized(lock) {
				lock.set(-1);
				lock.notifyAll();				
				return;
			}			
		}
		
		
		
		if (ifAnalyze) {
			System.out.print("***** will trigger training *********\n");
			/*
			 *The A part 
			 ***/
			objectivesToBeOptimized = Analyzer.doAnalysis(samples);
		}
		
	
		if (isTestQoSModelingOnly) {
			synchronized(lock) {
				lock.set(-1);
				lock.notifyAll();				
				return;
			}			
		}
		
		// ===================== force one trigger (should be removed) =====================
		if (expectedSample == 11) {
	 	objectivesToBeOptimized = new ArrayList<Objective>();
	 	objectivesToBeOptimized.add(Repository.getService("jeos-"+Configurator.service).getObjective("Response Time"));
		}
		// ===================== force one trigger (should be removed) =====================
		
		// If need trigger optimization in the Planer, then the Analyzer should tell.
		// TODO only trigger if the current setup has been longer than t intervals.
		if (objectivesToBeOptimized != null) {
		
			
			for (final Objective obj : objectivesToBeOptimized) {
				final String uuid = UUID.randomUUID().toString();
				// Optimize them on separate thread, if two or more are in the same group
				// then only the first one can trigger optimization as implemented in 
				// SuperRegionControl
				Future f = SSAScalingThreadPool.submitJob(new Runnable(){

					@Override
					public void run() {
						 doDecisionMaking(obj, uuid);
					}
					
				});
				
				SSAScalingThreadPool.putThread(uuid, f);
			    
			}
		}
		
		
		synchronized(lock) {
			while (objectivesToBeOptimized != null && lock.get() != objectivesToBeOptimized.size()) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
			}
			
			// Reset counter to zero upon finish.
			lock.set(-1);
			objectivesToBeOptimized = null;
			lock.notifyAll();
			System.out.print("***** MAPE finished " + samples + " *********\n");
		}
		
		System.gc();
	}
	
	/**
	 * 
	 * Do the PE parts.
	 * 
	 * This would be called by SuperRegionControl and RegionControl in case of deployment/model changes during
	 * optimization or region partitioning.
	 * @param obj
	 */
	public static void doDecisionMaking (Objective obj, String uuid){
		/*
		 *The P part 
		 ***/
		// If need trigger optimization in the Planer, then the Analyzer should tell.
		final LinkedHashMap<ControlPrimitive, Double>  decisions = Planner.optimize(obj, uuid);
		// Get the result from Planer to the Executor who will trigger Actuator. 
		
		// If the region is under optimization already or there is no
		// proper solution found, then the decisions would be null.
		if (decisions != null) {
			/*
			 *The E part 
			 ***/
			Executor.execute(decisions);
			Executor.print();
		} else {
			System.out.print("There is no proper solutions found!\n");
		}
		
		synchronized(lock) {
			lock.incrementAndGet();
			if (lock.get() == objectivesToBeOptimized.size()) {
				lock.notifyAll();
			}
		}
	}
}
