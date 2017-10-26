package org.ssase.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssase.ControlBus;
import org.ssase.Service;
import org.ssase.debt.AdaptationDebtBroker;
import org.ssase.debt.TwoPhaseTrigger;
import org.ssase.executor.VM;
import org.ssase.model.ModelingType;
import org.ssase.objective.Cost;
import org.ssase.objective.Objective;
import org.ssase.objective.QualityOfService;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.HardwareControlPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.region.SuperRegionControl;
import org.ssase.util.Repository;
import org.ssase.util.SSAScalingThreadPool;

public class Analyzer {

	
	protected static final Logger logger = LoggerFactory
	.getLogger(Analyzer.class);
	// Ensure it proceeds to Plan phase only if all the models are trained and updated. 
	private final static AtomicInteger updatedModel = new AtomicInteger(0);
	
	private static boolean isReachTheLeastSamples = false;
	
	public static TriggerType selected = TriggerType.Requirement;
	
	public static final long frequencyFactor = 10L;
	
	// True if every timestep is an adaptation, this will enforce training of each timestep.
	public static boolean isEachStepIsAdaptation= false;
	
	// Used only for trigger by adaptation debt
	private static boolean isTrigger = false;
	
	private static AdaptationDebtBroker debtBroker = null;
	private static TwoPhaseTrigger twoPhaseTrigger = null;
	
	public static boolean isDebtAware(){
		return selected == TriggerType.DebtAll || selected == TriggerType.Debt;
	}
	
	
	public static void setSelectedTriggerType(String type) {
		if (type == null)
			throw new RuntimeException("No proper TriggerType found!");

		type = type.trim();

		if ("debt".equals(type)) {
			selected = TriggerType.Debt;
		} else 	if ("debtall".equals(type)) {
			selected = TriggerType.DebtAll;
		} else 	if ("frequency".equals(type)) {
			selected = TriggerType.Frequency;
		} else 	if ("prediction".equals(type)) {
			selected = TriggerType.Prediction;
		}


		if(debtBroker == null && (selected == TriggerType.DebtAll || selected == TriggerType.Debt)) {
			
			List<QualityOfService> qos = new ArrayList<QualityOfService>();			
			qos.addAll(Repository.getQoSSet());
			List<Primitive> primitives = new ArrayList<Primitive>();	
			for (Service s : Repository.getAllServices()) {
				primitives.addAll(s.getPrimitives());
			}
			
			debtBroker = AdaptationDebtBroker.getInstance(qos, primitives);
		}
		
		if(twoPhaseTrigger == null && selected == TriggerType.Prediction) {
			List<QualityOfService> qos = new ArrayList<QualityOfService>();			
			qos.addAll(Repository.getQoSSet());
			twoPhaseTrigger = TwoPhaseTrigger.getInstance(qos);
		}
		
		if (selected == null)
			throw new RuntimeException("Can not find trigger type for type "
					+ type);
	}
	
	public static AdaptationDebtBroker getAdaptationDebtBroker (){
		return debtBroker;
	}
	
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
		
		if(selected == TriggerType.Debt || 
				selected == TriggerType.DebtAll ||
				selected == TriggerType.Frequency ||
				selected == TriggerType.Prediction) {
			
			// Forcebly disenable adaptation
			if(!isTrigger) return new ArrayList<Objective>();
			
			List<Objective> list = detectSymptoms();
			if(list != null && list.size() == 0) {
				// Forcebly trigger adaptation, now we just random pick an objective.
				list.add(Repository.getAllObjectives().iterator().next());
			}
			
			return list;
		} else {
			return detectSymptoms();
		}
		
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
		
		if(!ControlBus.isTriggerQoSModeling) {
			// To avoid waiting.
			updatedModel.set(Repository.getQoSSet().size());
			logger.debug("Notice that ControlBus.isTriggerQoSModeling has been set to false!");
			return;
		}
		
		if(selected == TriggerType.Debt) {
			
			// The function inside should deal with the case
			// where the previous one is not adaptation.
			debtBroker.doPosteriorDebtAnalysis();
			isTrigger = debtBroker.isTrigger();
			System.out.print("If predicted to trigger adaptation at current timestap: " + isTrigger + ", isEachStepIsAdaptation=" + isEachStepIsAdaptation + "\n");
			isTrigger = isEachStepIsAdaptation? true : isTrigger;
			if(isTrigger) {
				debtBroker.doPriorDebtAnalysis();
				train();
			} else {
				for (final QualityOfService qos : Repository.getQoSSet()) {
					isReachTheLeastSamples = qos.doUpdate();
					
				}
				
				updatedModel.set(Repository
						.getQoSSet().size());
				
				synchronized (updatedModel) {
						updatedModel.notifyAll();
					
				}
				
			}
			
			
		} else if(selected == TriggerType.DebtAll) {	
			// The function inside should deal with the case
			// where the previous one is not adaptation.
			debtBroker.doPosteriorDebtAnalysis();
			isTrigger = debtBroker.isTrigger();
			System.out.print("If predicted to trigger adaptation at current timestap: " + isTrigger + ", isEachStepIsAdaptation=" + isEachStepIsAdaptation + "\n");
			isTrigger = isEachStepIsAdaptation? true : isTrigger;
			if(isTrigger) {
				debtBroker.doPriorDebtAnalysis();
				train();
			} else {
				for (final QualityOfService qos : Repository.getQoSSet()) {
					isReachTheLeastSamples = qos.doUpdate();
					
				}
				
				updatedModel.set(Repository
						.getQoSSet().size());
				
				synchronized (updatedModel) {
						updatedModel.notifyAll();
					
				}
				
			}
		} else if(selected == TriggerType.Frequency) {
			
			if (samples != 0 && samples % frequencyFactor == 0) {
				isTrigger = true;
			} else {
				isTrigger = false;
			}
			
			
			if(isTrigger) {
				train();
			} else {
				for (final QualityOfService qos : Repository.getQoSSet()) {
					isReachTheLeastSamples = qos.doUpdate();
					
				}
				
				updatedModel.set(Repository
						.getQoSSet().size());
				
				synchronized (updatedModel) {
						updatedModel.notifyAll();
					
				}
				
			}
			
		} else if(selected == TriggerType.Prediction) {
			twoPhaseTrigger.doTraining();
			twoPhaseTrigger.preTraining();
			isTrigger = twoPhaseTrigger.isTrigger();
			System.out.print("If predicted to trigger adaptation at current timestap: " + isTrigger + ", isEachStepIsAdaptation=" + isEachStepIsAdaptation + "\n");
			isTrigger = isEachStepIsAdaptation? true : isTrigger;
			if(isTrigger) {
				train();
			} else {
				for (final QualityOfService qos : Repository.getQoSSet()) {
					isReachTheLeastSamples = qos.doUpdate();
					
				}
				
				updatedModel.set(Repository
						.getQoSSet().size());
				
				synchronized (updatedModel) {
						updatedModel.notifyAll();
					
				}
				
			}
		} else {
			train();
			
		}

		
	}
	
	private static void train(){
		for (final QualityOfService qos : Repository.getQoSSet()) {

			SSAScalingThreadPool.executeJob(new Runnable() {

				@Override
				public void run() {
					// System.out.print("***** doing training *********\n");
					boolean result = false;
					try {

						result = qos.doTraining();

					} catch (RuntimeException e) {
						// Make sure the process can keep going and avoid
						// deadlock.
						e.printStackTrace();
						synchronized (updatedModel) {
							updatedModel.incrementAndGet();
							// System.out.print("==========================  ===============================\n");
							// System.out.print(updatedModel.get() +
							// " processed\n");
							// System.out.print("==========================  ===============================\n");
							isReachTheLeastSamples = result;
							if (updatedModel.get() == Repository
									.getQoSSet().size()) {
								updatedModel.notifyAll();
							}
						}
					}
					synchronized (updatedModel) {
						updatedModel.incrementAndGet();
						// System.out.print("==========================  ===============================\n");
						// System.out.print(updatedModel.get() +
						// " processed\n");
						// System.out.print("==========================  ===============================\n");

						isReachTheLeastSamples = result;
						if (updatedModel.get() == Repository.getQoSSet()
								.size()) {
							updatedModel.notifyAll();
						}
					}
					// System.out.print("***** finish doing training *********\n");
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
			for (Primitive p : v.getAllHardwarePrimitives()){
				p.addValue(samples);
			}
			for (Primitive p : v.getAllSharedSoftwarePrimitives()){
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
			for (Primitive p : v.getAllHardwarePrimitives()){
				p.resetValues();
			}
			for (Primitive p : v.getAllSharedSoftwarePrimitives()){
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
		 org.ssase.util.Util.readMeasuredData();
		// For QoS and primitives, should use prepareToAddValue here, as the actual 'add' operation is done
		// in 'updateModels' function.
	}
	
	private static  List<Objective> detectSymptoms(){
		// Done! only trigger if the current violation has been longer than t intervals and the current
		// configuration has been up and running for t intervals.
		if (ControlBus.isTriggerQoSModeling && !isReachTheLeastSamples /*Only detect when essential number of samples have been collected*/) {
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
		 
		 /*for (Service s : Repository.getAllServices() ) {
			 s.updateCostModelInputs();
		 }*/
		 
		 // Doing the first run of filtering objectives that belong to the same region.
		 // TODO May also communicate with other PM.
		 if (!ControlBus.isTestQoSModelingOnly) {
		    SuperRegionControl.getInstance().filterObjective(result);
		 }
		  
		 return result;
		 
	}
	
	
}
