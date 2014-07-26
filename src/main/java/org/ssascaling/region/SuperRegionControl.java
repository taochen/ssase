package org.ssascaling.region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

import org.ssascaling.ControlBus;
import org.ssascaling.objective.Objective;
import org.ssascaling.observation.event.SuperRegionChangeEvent;
import org.ssascaling.observation.event.SuperRegionChangeEvent.Operation;
import org.ssascaling.observation.listener.SuperRegionListener;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.util.Repository;
import org.ssascaling.util.SSAScalingThreadPool;
import org.ssascaling.util.Tuple;

/**
 * It only care about the super region of this PM.
 * 
 * TODO setup the global view of service-VM and VM-PM mapping. (for triggering, may be in different class?)
	
 * @author tao
 *
 */
@SuppressWarnings("rawtypes")
public class SuperRegionControl implements SuperRegionListener{

	// Change in the same sync lock 
	// ***************** need to maintance these three ****************
	// No duplicated are allowed.
	//private Map<Objective, RegionControl> objectiveMap;
	

	//private Map<String, RegionControl> idMap;
	
	//private Map<RegionControl, Set<Objective>> regionMap;
	// ***************** need to maintance these threads ****************
	//private Map<RegionControl,Tuple<Objective, Thread>> map;
	// Change in the same sync lock 
	
	private RegionControl control;
	
	private Map<Objective, String> running;
	
	private static SuperRegionControl instance;
	
	
	
	private Object lock = new Object();
	
	static {
		init();
	}
	
	public static SuperRegionControl getInstance(){
		return instance;
	}
	
	private static void init() {
		instance = new SuperRegionControl();
	}
	
	private SuperRegionControl(){
		
	}
	
	
	private SuperRegionControl(Set<Objective> objs){
		running = new HashMap<Objective, String>();
		control =  new RegionControl(objs);
	}
	
	/*private SuperRegionControl(Set<Set<Objective>> set){
		objectiveMap = new HashMap<Objective, RegionControl>();
		//idMap = new HashMap<String, RegionControl>();
		regionMap = new HashMap<RegionControl, Set<Objective>>();
		map = new HashMap<RegionControl,Tuple<Objective, Thread>>();
		for (Set<Objective> s : set) {
			RegionControl rc = new RegionControl(s);
			//idMap.put(rc.id, rc);
			regionMap.put(rc, s);
			
			for (Objective obj : s){
				objectiveMap.put(obj, rc);
			}
		}
	}*/
	
	/**
	 * There is only one super region on each node, but the node needs to maintain information
	 * for all the super regions, however, it only responsible for trigger the objective belongs to
	 * its own super region.
	 * @param obj
	 */
	public LinkedHashMap<ControlPrimitive, Double> optimize(Objective obj, String uuid){
		/*RegionControl superRegion = null;
		synchronized (lock) {
			// The obj here should only belongs to its own super region of the node.
			superRegion = objectiveMap.get(obj);
			// This should not occur, in case there is duplicate restarting due to super region and model
			// change at closed time.
			// However, this might still occur if there are a group of objective to be optimized, and
			// some of them are from the same region.
			if(map.containsKey(superRegion)) {
				return null;
			}
			map.put(superRegion, new Tuple(obj,Thread.currentThread()));
		}*/
		
		synchronized (lock) {
			// Always overwrite the new one, as the old one should have been stop.
			running.put(obj, uuid);
		}
		
		LinkedHashMap<ControlPrimitive, Double> result = control.optimize(obj, uuid);
		
		/*synchronized (lock) {
			// If this region has been replaced due to super region change, and this thread is
			// triggered by region control before such change, then it should be aborted.
			if (!regionMap.containsKey(superRegion)) {
				map.remove(superRegion);
				return null;
			}
			map.remove(superRegion);
		}*/
		
		synchronized (lock) {
			running.remove(obj);
			SSAScalingThreadPool.removeThread(uuid);
		}
		
		
		return result;
	}
	
	/**
	 * Used in the Analyzer, when we reach here, it means the 
	 * QoS modeling has been completed. 
	 * @param result
	 */
	public void filterObjective (List<Objective> result) {
		
		if (control == null) {
			running = new HashMap<Objective, String>();
			control =  new RegionControl(Repository.getAllObjectives());
			
			Repository.setModelListeners(control);
		}
		
		control.filterObjective(result);
		// This would update regions if there is changes informed by model listeners.
		control.updateRegions();
	}
	
	/**
	 * This would be triggered only once per change. Be call outside the MAPE directly
	 */
	public void updateWhenSuperRegionChange(SuperRegionChangeEvent event){
		synchronized (lock) {
			//TODO do re assign of super region based on the global view of service-VM and VM-PM mapping.
			
			/*Operation[] ops = event.getOperation();
			final Set<String> ids = new HashSet<String>();
			for (Operation op : ops) {
				
				if (ids.contains(op.getSuperRegionId())) {
					Set<Objective> objs = regionMap.get(idMap.get(op.getSuperRegionId()));
					
					
					if (op.getOpeator() > 0) {
						objs.add(op.getObj());
						// Overwrite
						objectiveMap.put(op.getObj(), idMap.get(op.getSuperRegionId()));
					} else {
						objs.remove(op.getObj());					
						objectiveMap.remove(op.getObj());
					}
					
				} else {
					RegionControl rc = idMap.get(op.getSuperRegionId());
					RegionControl newRC = rc.copy();
					
					
					Set<Objective> objs = regionMap.get(rc);
					
					//idMap.remove(rc);
					// Overwrite
					idMap.put(op.getSuperRegionId(), newRC);
					regionMap.remove(rc);
					regionMap.put(newRC, objs);
					
					if (op.getOpeator() > 0) {
						objs.add(op.getObj());
						// Overwrite
						objectiveMap.put(op.getObj(), newRC);
					} else {
						objs.remove(op.getObj());					
						objectiveMap.remove(op.getObj());
					}
				}
				
				
				ids.add(op.getSuperRegionId());
			}*/
			
			// TODO update the repository as well!
			// including the direct primitives and the service
			Operation[] ops = event.getOperation();
			for (Operation op : ops) {
				if (op.getOpeator() > 0) {
					//objs.add(op.getObj());
				} else {
					//objs.remove(op.getObj());
				}
			}
		    control.calculateRegions(Repository.getAllObjectives());
			
			restartExistingOptimization();
		}
	}
	
	
	/**
	 * This should be sync on 'lock', at the place where it is called.
	 */
	private void restartExistingOptimization(){
		for (final Map.Entry<Objective, String> entry : running.entrySet()) {
			
			SSAScalingThreadPool.terminate(entry.getValue());
			
			
			// Only trigger if the objective is still violated, should be fine as the Analyzer ensures
			// that the new set of objective always trigger after this method. This is because thie method is
			// called in the same thread as the model training.
			
			if (entry.getKey().isViolate()) {

				final String uuid = UUID.randomUUID().toString();
				Future f = SSAScalingThreadPool.submitJob(new Runnable() {

					@Override
					public void run() {
						ControlBus.doDecisionMaking(entry.getKey(), uuid);
					}

				});

				SSAScalingThreadPool.putThread(uuid, f);
			}
		}
		// This should be fine as it is called with the lock's sync block.
		running.clear();
	}
}
