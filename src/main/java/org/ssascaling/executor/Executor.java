package org.ssascaling.executor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ssascaling.ControlBus;
import org.ssascaling.Service;
import org.ssascaling.actuator.ActuationSender;
import org.ssascaling.actuator.Actuator;
import org.ssascaling.actuator.linux.CPUNoActuator;
import org.ssascaling.actuator.linux.CPUPinActuator;
import org.ssascaling.actuator.linux.ReplicationActuator;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.HardwareControlPrimitive;
import org.ssascaling.primitive.SoftwareControlPrimitive;
import org.ssascaling.primitive.Type;
import org.ssascaling.util.Repository;
import org.ssascaling.util.Logger;

public class Executor {
	
	/*Mainly need to record the provision of hardware CPs*/
	private static int totalMemory; /*Mb*/
	// these are based on the max possible provision value,
	// so the actual provision do not need to update them, but
	// the max provision value does.
	
	// These two means the max possible provision values rather than the 
	// actual provsion values.
	private static int remainingMemory;
	private static int remainingCPU;
	
	// This is mainly for changing the hardware, but need to maintain
	// integrity of each adaptation.
	private static Object lock = new Object();
	
	private static Actuator cpuNoActuator = new CPUNoActuator();
	private static Actuator cpuPinActuator = new CPUPinActuator();
	
	private static List<CPUCore> cores = new ArrayList<CPUCore>();
	
	public final static boolean isTest = false;
	
	/**********************
	 * 
	 * This is an important
	 * variable the should be changed.
	 * 
	 * 
	 * 
	 * *********************/
	public final static boolean isEnableUpperBoundUpdate = false;
	/**********************
	 * 
	 * This is an important
	 * variable the should be changed.
	 * 
	 * 
	 * 
	 * *********************/
	public final static boolean isEnableLowerBoundUpdate = false;
	
	
	public final static long memoryThreshold = 50;
	public final static long CPUThreshold = 3;
	
	/**
	 * This should be invoked after the Repository has been configured properly.
	 * 
	 * We assume that there would be one vcpu for each VM at the beginning.
	 */
	public static void init (int noOfCPU /**This should be >= the number of initial VMs*/){
		
		
		totalMemory = 1800 - 600;
		remainingMemory = totalMemory;
		
		int index = 0;
		for (VM v : Repository.getAllVMs()) {
			index++;
			cores.add(new CPUCore(index, new VM[]{v}));		
			remainingMemory -= v.getMaxMemory();
			remainingCPU += (int)(100 - v.getMaxCpuCap());
		}
		
		
		
		
		noOfCPU -= index;
		
		// Add vcpus that has not been occupied at the beginning.
		for (int i = 0; i > noOfCPU; i++) {
			index++;
			cores.add(new CPUCore(index, new VM[]{}));		
		}
		
		print();
	}
	
	public static void init(HardwareControlPrimitive... primitives) {
		totalMemory = 1800 - 500;
		remainingMemory = 0;
		// 3 vcpus
		remainingCPU = 0;
		
		/*This is a testing setup, the real setup should come from property files*/
		Repository.setService("jeos-edu.rice.rubis.servlets.SearchItemsByCategory", new Service());
		Repository.setService("jeos-edu.rice.rubis.servlets.BrowseCategories", new Service());  
		
		/*2 thread software CP, 6 CPU/memory hardware CP*/
		
		VM jeos = new VM("jeos", new HardwareControlPrimitive[]{primitives[0], primitives[1]});
		VM kitty = new VM("kitty", new HardwareControlPrimitive[]{primitives[2], primitives[3]});
		VM miku = new VM("miku", new HardwareControlPrimitive[]{primitives[4], primitives[5]});
		
		//remainingMemory -= 600;
		
		
		Repository.setVM("jeos", jeos);
		Repository.setVM("kitty", kitty);
		Repository.setVM("miku", miku);
		
		cores.add(new CPUCore(1, new VM[]{jeos}));
		cores.add(new CPUCore(2, new VM[]{kitty}));
		cores.add(new CPUCore(3, new VM[]{miku}));
		/*cores.add(new CPUCore(4, new VM[]{}));
		cores.add(new CPUCore(5, new VM[]{}));
		cores.add(new CPUCore(6, new VM[]{}));
		cores.add(new CPUCore(7, new VM[]{}));*/
	}
	
	/**
	 * 
	 * Double value here should be the denormalized ones, which should be fine as the normalized data
	 * is only done and used within QualityOfService class.
	 * @param decisions
	 */
	public static void execute(LinkedHashMap<ControlPrimitive, Double> decisions){
		long value  = 0;
		
		// Send all software control primitives value to VM in one shot.
		// VM_ID - data
		Map<String, StringBuilder> softwareCPData = new HashMap<String, StringBuilder> ();
		// VM_ID - data
		Map<String, StringBuilder> hardwareCPData = new HashMap<String, StringBuilder> ();
		LinkedHashMap<ControlPrimitive, Double> listMap = orderDecision(decisions);
		// For horizontal scaling, VM_ID - data (0 for scale out, others for scale in)
		Map<String, Integer> horizontalActions = new HashMap<String, Integer> ();
		// Number of hardware CPs
		int noOfHardwareCP = Repository.getAllVMs().iterator().next().getAllHardwarePrimitives().size();
		
		// Need to sync in order to consistent on the utilization of resource on the PM.
		synchronized (lock) {
			for (Map.Entry<ControlPrimitive, Double> entry :  listMap.entrySet()){
				
				
				// If not a responsible service nor VM, then return
				if (!Repository.isContainService(entry.getKey().getAlias()) && !Repository.isContainVM(entry.getKey().getAlias())){
					return;
				}
				
				value = Math.round(entry.getValue());
				
				//TODO if value is smaller than a threshold, should trigger scale in.
				/**
				 * Hardware CP allocation require special treatments.
				 */
				if (entry.getKey().isHardware()) {
				
					entry.getKey().outputCurrentVector();
					// Logging, here we assume that any actions of hardware CP can be taken placed on Dom0.
					if (!hardwareCPData.containsKey(entry.getKey().getAlias())) {
						hardwareCPData.put(entry.getKey().getAlias(), new StringBuilder());
					}
					hardwareCPData.get(entry.getKey().getAlias()).append(entry.getKey().getType() + "-" + value + "\n");
					
					
						// CPU is sepecial as its denormalized value is still %
						if (Type.CPU.equals(entry.getKey().getType())) {
							long finalValue = value;
							
							VM vm = Repository.getVM(entry.getKey().getAlias());
							// Scale down, always remove the core with higher ID first.
							if (vm.isScaleDown(value)) {
								double v = 0;
								
								if (Double.isNaN(v = entry.getKey().triggerMaxProvisionUpdate(false, value, CPUThreshold))) {
									System.out.print("Scale in due to CPU on " + entry.getKey().getAlias() + " \n");
									// TODO do scale in and free all resources.
									//return;
									horizontalActions.put(entry.getKey().getAlias(), horizontalActions.containsKey(entry.getKey().getAlias())? 
											horizontalActions.get(entry.getKey().getAlias()) + 1 : 1);
									hardwareCPData.get(entry.getKey().getAlias()).append(entry.getKey().getAlias()).append("Scale in due to CPU on " + entry.getKey().getAlias() + "\n");
									v = 0;
								}
								
								remainingCPU += v;
								
								long count = 0;
								int no = 0;
								for (CPUCore core : cores) {
									
									if (core.getVMs().containsKey(vm)) {
										count+=core.getVMs().get(vm);
										// If more CPU allocation on a core.
										if (count > value) {
											long minus = count - value;
											long change = core.getVMs().get(vm) - minus;
											
											core.getVMs().put(vm,  change);
											core.update(minus);
											// if can remove the core.
											if (change == 0) {
												core.getVMs().remove(vm);									
											}  else {
												no ++;	
											}
											//TODO do pin cpu (we do not really need this? as if no core needed to be removed then
											// nothing change, if there is, then it is simply removed by cpu_set as a core with higher ID) *************
											
											count = value;
										} else {
											no ++;
										}
										
										
								    }
								}
								
								if (vm.getCPUNo() != no) {
								// Set the CPU core number
								cpuNoActuator.execute(entry.getKey().getAlias(), no);
								}
								
	
	
								// this is only the cap one.
								entry.getKey().triggerActuator(new long[] { value });
							} else if (vm.isScaleUp(value)) /*Scale up*/ {
								
		                        double v = 0;
								
								if (Double.isNaN(v = entry.getKey().triggerMaxProvisionUpdate(true, value, remainingCPU))) {
									//TODO should trigger scale out.
									System.out.print("Scale out due to CPU on " + entry.getKey().getAlias() + " \n");
									System.out.print(value + " : " + remainingCPU+ " \n");
									//return;
									horizontalActions.put(entry.getKey().getAlias(), 0);
									hardwareCPData.get(entry.getKey().getAlias()).append("Scale out due to CPU on " + entry.getKey().getAlias() + ", "+ value + " : " + remainingCPU+  "\n");
									v = 0;
								}
								
								
								
								remainingCPU -= v;
								
								
								long add = value - vm.getCpuCap();
								// new cpu core number
								long newNo = 0;
								
								final List<Integer> newCoreIndex = new ArrayList<Integer>();
								
							// Try to scale up on the core that has been already
							// allocated on the VM
							for (CPUCore core : cores) {
								if (core.getVMs().containsKey(vm)) {
									if (add == 0) {
										break;
									}

									long allocated = core.allocate(add);
									if (allocated != 0) {

										if (!core.getVMs().containsKey(vm)) {
											newCoreIndex.add(core
													.getPhysicalID());
											newNo++;
										}
										core.getVMs()
												.put(vm,
														core.getVMs()
																.containsKey(vm) ? core
																.getVMs().get(
																		vm)
																+ allocated
																: allocated);
										core.update(0 - allocated);

									}

									add -= allocated;
								}
							}
								
								// Then try the other cores
								for (CPUCore core : cores) {
									
									if (add == 0) {
										break;
									}
										
									
									long allocated = core.allocate(add);
									if (allocated != 0) {
										
										if (!core.getVMs().containsKey(vm)) {
										     newCoreIndex.add(core.getPhysicalID());
										     newNo ++;
										}
										core.getVMs().put(vm,  core.getVMs().containsKey(vm)?
												core.getVMs().get(vm) + allocated : allocated);
										core.update(0 - allocated);
										
									}
									
									add -= allocated;
								}
								
								if (add > 0) {
									//TODO should trigger scale out.
									System.out.print("Small scale out due to CPU on " + entry.getKey().getAlias() + " \n");
									System.out.print(value + " : " + remainingCPU+ " \n");
									
									horizontalActions.put(entry.getKey().getAlias(), 0);
									hardwareCPData.get(entry.getKey().getAlias()).append("Small scale out due to CPU on " + entry.getKey().getAlias() + ", "+ value + " : " + remainingCPU+  "\n");
									
								}
								
								
								if (newNo != 0) {
									// Set the CPU core number
									cpuNoActuator.execute(entry.getKey().getAlias(), newNo + vm.getCPUNo());
								}
								
								
	
								// If we foucs on the control of max provision, then add here should be always = 0
								// as the decided value is always within the max provision value that meet the capacity of this PM.
								finalValue = add > 0? (value-add) : value;
								// This is only the cap one.
								entry.getKey().triggerActuator(new long[] { finalValue });
								// Do pin cpu
								int start = ((int)vm.getCPUNo());
								for (int index : newCoreIndex) {
									cpuPinActuator.execute(entry.getKey().getAlias(), new long[]{start, index});
									start++;
								}
								 
							}
							
							
							entry.getKey().setProvision(finalValue);
	
						} else if (Type.Memory.equals(entry.getKey().getType())) {
							double v = 0;
							// Scale down
							if (value < entry.getKey().getProvision()) {
								
								if (Double.isNaN(v = entry.getKey().triggerMaxProvisionUpdate(false, value, memoryThreshold))) {
									System.out.print("Scale in due to memory on " + entry.getKey().getAlias() + " \n");
									
									// TODO do scale in and free all resources.
									//return;
									horizontalActions.put(entry.getKey().getAlias(), horizontalActions.containsKey(entry.getKey().getAlias())? 
											horizontalActions.get(entry.getKey().getAlias()) + 1 : 1);
									hardwareCPData.get(entry.getKey().getAlias()).append("Scale in due to memory on " + entry.getKey().getAlias() + "\n");
									v = 0;
								}
								
								remainingMemory += v;
								
							} else if (value > entry.getKey().getProvision()) /*Scale up*/ {
								if (Double.isNaN(v = entry.getKey().triggerMaxProvisionUpdate(true, value, remainingMemory))) {
									
									System.out.print("Small Scale out due to memory on " + entry.getKey().getAlias() + " \n");
									System.out.print(value + " : " + remainingMemory+ " \n");
									// TODO should trigger migration or replication for scale out as
									// the memory is insufficient.
									//return;
									horizontalActions.put(entry.getKey().getAlias(), 0);
									hardwareCPData.get(entry.getKey().getAlias()).append("Small Scale out due to memory on " + entry.getKey().getAlias() + ", " + value + " : " + remainingMemory+  "\n");
									v = 0;
								}
								
								remainingMemory -= v;
							}
							
							
							entry.getKey().setProvision(value);
							entry.getKey()
									.triggerActuator(new long[] { value });
							
						
							
							/*if (remainingMemory >= value) {
								remainingMemory -= value - entry.getKey().getProvision();
								
								entry.getKey().setProvision(value);
								entry.getKey()
										.triggerActuator(new long[] { value });
							} else {
								// TODO should trigger migration or replication for scale out as
								// the memory is insufficient.
								System.out.print("Scale out due to memory on " + entry.getKey().getAlias() + " \n");
							}*/
						}
					
					
				} else {
					
					// Logging, here we assume that any actions of software CP can not be taken placed on Dom0.
					String[] split = entry.getKey().getAlias().split("-");
					
					if (!softwareCPData.containsKey(split[0])) {
						softwareCPData.put(split[0], new StringBuilder());
					}
					softwareCPData.get(split[0]).append(split[1]  + "-" + entry.getKey().getType() + "-" + value + "\n");
					
					// There is usually no threshold for software CP.
					entry.getKey().triggerMaxProvisionUpdate(value > entry.getKey().getProvision(),value, Double.MAX_VALUE);
					entry.getKey().setProvision(value);
					entry.getKey().triggerActuator(new long[]{value});
					
				}	
				
			}
			long count = ControlBus.getInstance().getCurrentSampleCount();
			for (Map.Entry<String, StringBuilder> entry : hardwareCPData.entrySet()) {
				Logger.logExecutionData(entry.getKey(), entry.getValue(), count);
			}
			
			// Send the actions to VMs for certain software control primitives.
			for (Map.Entry<String, StringBuilder> entry : softwareCPData.entrySet()) {
				Logger.logExecutionData(entry.getKey(), entry.getValue(), -1);
				ActuationSender.getInsatnce().send(entry.getKey(), entry.getValue().toString());
			}
			
			// Send the actions for horizontal scaling.
			for (Map.Entry<String, Integer> entry : horizontalActions.entrySet()) {
				
				if (entry.getValue() == 0) {
					ReplicationActuator.getInstance().execute(entry.getKey(), 0);
				// Only scale in if all hardware CP fall below the thresholds.
				} else if (entry.getValue() == noOfHardwareCP) {
					ReplicationActuator.getInstance().execute(entry.getKey(), 1);
				}
				
			}
			
		}
		
		
	}
	
	public static void print(){
		
		for (VM v : Repository.getAllVMs()) {
			System.out.print(v.print() +"\n");	
		}
		
		for (CPUCore core : cores) {
			core.print();
		}
		
		System.out.print("Remaining memory=" + remainingMemory+"\n");
		System.out.print("Remaining CPU=" + remainingCPU+"\n");
	}
	
	private static LinkedHashMap<ControlPrimitive, Double> orderDecision(final LinkedHashMap<ControlPrimitive, Double> decisions){
	
		List<ControlPrimitive> list = new ArrayList<ControlPrimitive>(decisions.keySet());
		Collections.sort(list, new Comparator<ControlPrimitive>(){

			@Override
			public int compare(ControlPrimitive cp1, ControlPrimitive cp2) {
				if (cp1 instanceof SoftwareControlPrimitive && cp2 instanceof HardwareControlPrimitive) {
					return 1;
				}
				
				if (cp1 instanceof HardwareControlPrimitive && cp2 instanceof SoftwareControlPrimitive) {
					return -1;
				}
				
				// else, both should be either software CP or hardware CP.
				double cp1Value = decisions.get(cp1) ;
				double cp2Value = decisions.get(cp2) ;
				if (cp1.getProvision() > cp1Value && cp2.getProvision() < cp2Value) {
					return -1;
				}
				
				
				
				if (cp1.getProvision() < cp1Value && cp2.getProvision() > cp2Value) {
					return 1;
				}
				
				// If both require scale up, then the one that requires less go before.
				if (cp1.getProvision() < cp1Value && cp2.getProvision() < cp2Value) {
					return  cp1Value > cp2Value? 1 : -1;
				}
				
				return 0;
			}

			
			
		});
		
		LinkedHashMap<ControlPrimitive, Double> newDecisions = new LinkedHashMap<ControlPrimitive, Double>();
		for (ControlPrimitive p : list) {
			newDecisions.put(p, decisions.get(p));
		}
		
		return newDecisions;
	}
}
