package org.ssascaling;

import java.io.DataInputStream;
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
import org.ssascaling.util.SSAScalingThreadPool;

public class ControlBus {
	
	public static final boolean isTestMonitoringOnly = false;
	public static final boolean isTestQoSModelingOnly = true;

	// Ensure only one MAPE loop running at a time.
	// This is the main lock of MAPE loop, in case the repository 
	// would change, it also need to rely on this lock.
	private final static AtomicInteger lock = new AtomicInteger(-1);
	private static List<Objective> objectivesToBeOptimized = null;
	@SuppressWarnings("unused")
	public static void begin(DataInputStream is){
		
		synchronized(lock) {
			// Ensure only one MAPE loop running at a time.
			// Can not just get rid of as this may be newly measured data.	
				while (lock.get() != -1) {
					try {
						//System.out.print("========================== Break ===============================\n");
						//System.out.print(Thread.currentThread() + " break\n");
						//System.out.print("========================== Break ===============================\n");
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					
				}
				
				
				//System.out.print("========================== Run ===============================\n");
				//System.out.print(Thread.currentThread() + " running\n");
				//System.out.print("========================== Run ===============================\n");
				lock.set(0);
			
		}
		
		// This is the Monitor, do not detect symptons but record historical data.
		/*
		 *The M part 
		 ***/
		
		boolean ifAnalyze = Monitor.write(is);
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
			objectivesToBeOptimized = Analyzer.doAnalysis();
		}
		
	
		if (isTestQoSModelingOnly) {
			synchronized(lock) {
				lock.set(-1);
				lock.notifyAll();				
				return;
			}			
		}
		// If need trigger optimization in the Planer, then the Analyzer should tell.
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
			while (lock.get() != objectivesToBeOptimized.size()) {
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
		}
		
		
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
		// If the region is under optimization already, then the decisions would be null.
		if (decisions != null) {
			/*
			 *The E part 
			 ***/
			Executor.execute(decisions);
		}
		
		synchronized(lock) {
			lock.incrementAndGet();
			if (lock.get() == objectivesToBeOptimized.size()) {
				lock.notifyAll();
			}
		}
	}
}
