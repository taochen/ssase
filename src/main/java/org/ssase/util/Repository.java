package org.ssase.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import org.ssase.Service;
import org.ssase.executor.VM;
import org.ssase.objective.Cost;
import org.ssase.objective.Objective;
import org.ssase.objective.QualityOfService;
import org.ssase.objective.optimization.femosaa.variability.fm.FeatureModel;
import org.ssase.observation.listener.ModelListener;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.HardwareControlPrimitive;
import org.ssase.primitive.Primitive;

/**
 * Only the collections here and the ones in Service class need to be maintained.
 * 
 * The collections here are read-only unless there is a deployment changes, which should not be
 * happen very often.
 * 
 * We assume that these collection can be updated via GMS when adding/removing VMs and service-instances.
 * but of course need to have lock.
 * @author tao
 *
 */
public class Repository {
	
	
	// For each software, the objectives use the same FM.
	private static Map<Objective, FeatureModel> fms
	 = new ConcurrentHashMap<Objective, FeatureModel>();

	// ************** These are the collections contain unique instance of QoS and primitives, 
	// which need to be updated when adding newly measured data ***********
	
	// String name - services
	// We assume that the name here is unique even across different VMs.
	// VM_ID + '-' +  service name
	private static Map<String, Service> services
	 = new ConcurrentHashMap<String, Service>();
	
	// String VM_ID - VM
	private static Map<String, VM> vms  
		 = new ConcurrentHashMap<String, VM>();
	
	
	// Used to detect under-provisioning
	// need concurrent set
	private static Set<QualityOfService> qoss = new HashSet<QualityOfService>();
	// Used to detect over-provisioning
	private static Set<Cost> cost = new HashSet<Cost>();
	
	// ************** These are the collections contain unique instance of QoS and primitives, 
	// which need to be updated when adding newly measured data ***********
	
	// These are the objectives that managed by this super region control.
	// This is because any given objective need to have at least one direct primitive.
	// So this is basically a collection of all objectives.
	// Mainly for QoS
	
	// This can be configure the same time as possible primitives for services.
	// objectives include both cost and QoS.
	private static Map<Objective, Set<Primitive>> directPrimitives = new ConcurrentHashMap<Objective, Set<Primitive>>();
	
	public static void clear(){
		services.clear();
		vms.clear();
		qoss.clear();
		cost.clear();
		directPrimitives.clear();
	}
	
	public static Set<Objective> getAllObjectives(){
		return directPrimitives.keySet();
	}
	
	public static Collection<Service> getAllServices(){
		return services.values();
	}
	
	public static Set<QualityOfService> getQoSSet(){
		return qoss;
	}
	
	public static void setQoS(QualityOfService qos){
		qoss.add(qos);
	}
	
	public static void setCost(Cost co){
		cost.add(co);
	}
	
	public static Set<Cost> getCostSet(){
		return cost;
	}
	
	public static void setVM (String VM_ID, VM vm) {
		vms.put(VM_ID, vm);
	}
	
	public static VM getVM (String VM_ID) {
		return vms.get(VM_ID);
	}
	
	public static Collection<VM> getAllVMs () {
		return vms.values();
	}
	
    public static boolean isContainVM (String VM_ID) {
		return vms.containsKey(VM_ID);
	}
	
	public static void setService (String name, Service service) {
		services.put(name, service);
	}
	
	public static Service getService (String name) {
		return services.get(name);
	}
	
    public static boolean isContainService (String name) {
		return services.containsKey(name);
	}
	
	public static void prepareToUpdateHardwareControlPrimitive(String VM_ID, String name, double... values) {
		
		for (double v : values){
			if(vms.get(VM_ID).getHardwareControlPrimitive(name) != null)
			vms.get(VM_ID).getHardwareControlPrimitive(name).prepareToAddValue(v);
			// Update the shared software primitives.
			if(vms.get(VM_ID).getSoftwareControlPrimitive(name) != null)
			vms.get(VM_ID).getSoftwareControlPrimitive(name).prepareToAddValue(v);
		}
		
		
	}
	
	public static void prepareToAddValueForQoS(String service, String name, double... values){
		services.get(service).prepareToUpdateQoSValue(name, values);
	}
	
	public static void prepareToAddValueForPrimitive(String service, String name, double... values){
		
		services.get(service).prepareToUpdatePrimitiveValue(name, values);
	}
	
	public static void setDirectForAnObjective  (Objective obj, Primitive p) {
		if (!directPrimitives.containsKey(obj)) {
			directPrimitives.put(obj, new HashSet<Primitive>());
		}
		
		directPrimitives.get(obj).add(p);
	}
	
	public static boolean isDirectForAnObjective (Objective obj, Primitive p) {
		return directPrimitives.containsKey(obj)?  directPrimitives.get(obj).contains(p) : false;
	}
	
	public static Set<Primitive> getDirectPrimitives (Objective obj) {
		return directPrimitives.get(obj);
	}
	
	public static int countDirectForAnObjective (Objective obj) {
		return directPrimitives.containsKey(obj)? directPrimitives.get(obj).size() : 0;
	}
	
	public static void setModelListeners (ModelListener listener) {
		for (QualityOfService q : qoss) {
			q.addListener(listener);
		}
	}
	
	public static List<ControlPrimitive> getSortedControlPrimitives (Objective obj) {
		return fms.get(obj).getSortedControlPrimitives();
	}
	
	public static void setSortedControlPrimitives(Objective obj, FeatureModel fm){
		fms.put(obj, fm);
	}
}
