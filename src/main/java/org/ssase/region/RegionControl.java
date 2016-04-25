package org.ssase.region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssase.ControlBus;
import org.ssase.Service;
import org.ssase.objective.Objective;
import org.ssase.observation.event.ModelChangeEvent;
import org.ssase.observation.listener.ModelListener;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.util.SSAScalingThreadPool;
import org.ssase.util.Tuple;

/**
 * Create new instance whenever super region distribution change.
 * @author tao
 *
 */
@SuppressWarnings("rawtypes")
public class RegionControl implements ModelListener {
	
	protected static final Logger logger = LoggerFactory
	.getLogger(RegionControl.class);
	
	protected String id;

	private Map<Objective, Region> objectiveMap;
	
	
	//private Set<Primitive> possibleInput;
	
	private Object lock = new Object();
	
	
    // Ensure to trigger re assign region only when all models are up-to-date. 
    // In case where failure occur, this instance is invalid anyway as the optimization thread
    // would be stopped by the SuperRegionControl.
    //private int knownConunter = 0;
    
    private boolean isStop = false;
    
    // Include the decomposition of scaled objective as those objective
    // has more then one notification upon significant changes, but they can be considered as one.
    // Updated with calculateRegions
    //private int totalNumberOfObjective;
    
    // String here is a unqiue thread id.
    private Map<Region,Tuple<Objective, String>> map;
    
    public RegionControl(Set<Objective> objs){
    	objectiveMap = new HashMap<Objective, Region>();
    	map = new HashMap<Region,Tuple<Objective, String>>();
    	id = UUID.randomUUID().toString();
    	calculateRegions(objs);
    }
	
    public RegionControl(String id){
    	this.id = id;
    	objectiveMap = new HashMap<Objective, Region>();
    }
    /**
     * It is possible that, the return solution is invalid, e.g., critical change occur right after
     * this method return. So when doing the actions, we need to validate.
     * @param obj
     */
	public LinkedHashMap<ControlPrimitive, Double> optimize(Objective obj, String uuid){
		Region region = null;
		synchronized (lock) {
			region = objectiveMap.get(obj);
			// This should not occur, but just in case there is duplicate restarting due to super region and model
			// change at closed time.
			
			// This is because it should have been filtered in Analyzer
			if(map.containsKey(region)) {
				return null;
			}
			map.put(region, new Tuple(obj, uuid));
		}
		
		long time = System.currentTimeMillis();
		LinkedHashMap<ControlPrimitive, Double> result = region.optimize();
		org.ssase.util.Logger.logOptimizationTime(null, String.valueOf((System.currentTimeMillis() - time)));
		synchronized (lock) {
			map.remove(region);
		}
		
		return result;
		
	}
	
	/**
	 * Only deal with critical changes, e.g., the primitives selection result change.
	 * 
	 * 
	 * 
	 * 
	 * Unlike non-critical change, which can still be a valid solution even if the dynamic has not been
	 * resolved. Critical change could result in an invalid solution, which needs to be avoided.
	 * 
	 * 
	 */
	@Override
	public void updateWhenModelChange(ModelChangeEvent event) {
		synchronized (lock) {
			if (event.isCritical()) {
				isStop = true;
			}
			
		}
		
		
	}
	
	/**
	 * This should only be called when all models of objective have been updated.
	 */
	public void updateRegions(){
		synchronized (lock) {
			if (isStop) {
				// Making the source un-affect by the clearance of objective map.
				Set<Objective> set = new HashSet<Objective>();
				set.addAll( objectiveMap.keySet());
				calculateRegions(set);
				restartExistingOptimization();
			}
			
			isStop = false;
		}
	}
	
	/**
	 * Used in the Analyzer, ensure that only one objective from a region,
	 * as the region as a whole would be optimized.
	 * 
	 * We can do this as the update of region triggered by QoS sensitivity changes
	 * has already been done during the QoS function training phase. (a sequential process)
	 * 
	 * @param result the list of objectives that to be optimized.
	 */
	public void filterObjective (List<Objective> result) {
		
		final Set<Region> set = new HashSet<Region>();
		List<Objective> removal = new ArrayList<Objective>();
		synchronized (lock) {
			for (Objective obj : result) {
				Region reg = objectiveMap.get(obj);
				if (!set.contains(reg)) {
					set.add(reg);
				} else {
					removal.add(obj);
				}
			}
		}
		
		result.removeAll(removal);
	}
	
	public RegionControl copy(){
		RegionControl sr = new RegionControl(id);
		return sr;
	}
	
	/**
	 * Should be used within lock, whether it is from regioncontrol or superregioncontrol
	 * @param objs
	 */
	public void calculateRegions(Set<Objective> objs){
		logger.debug("******** start regioning **************\n");
		objectiveMap.clear();
		//totalNumberOfObjective = 0;
		for (Objective obj : objs) {
			//totalNumberOfObjective += obj.countObjective();
			for (Objective subObj : objs) {
				
				// If both have been assigned.
				if (objectiveMap.containsKey(obj) && objectiveMap.containsKey(subObj) 
						&& objectiveMap.get(obj).equals(objectiveMap.get(subObj)))  {
					continue;
				}
				
				if (obj.equals(subObj) ) {
			   	     continue;
			    }
				
				
				if (obj.isSensitiveToTheSamePrimitive(subObj, null)) {
					//logger.debug(obj.getName() + " and " + subObj.getName() + " does sensitive\n");
					
					if (objectiveMap.containsKey(obj) && objectiveMap.containsKey(subObj) ) {
						//logger.debug("******here!\n");
						// Change the region!
						Region target = objectiveMap.get(obj);
						Region source = objectiveMap.get(subObj);
						
						for (Objective o : objectiveMap.keySet()) {
							
							if (source.equals(objectiveMap.get(o))) {
								target.addObjective(o);
								objectiveMap.put(o, target);
							}
							
						}
						
					
					} else if (objectiveMap.containsKey(obj)){
							Region region = objectiveMap.get(obj);
							region.addObjective(subObj);
							objectiveMap.put(subObj, region);
					} else if (objectiveMap.containsKey(subObj)) {
							Region region = objectiveMap.get(subObj);
							region.addObjective(obj);
							objectiveMap.put(obj, region);
					} else {
						Region region = Region.getNewRegionInstanceByType(Region.selected);
						region.addObjective(obj);
						region.addObjective(subObj);
						objectiveMap.put(obj, region);
						objectiveMap.put(subObj, region);
					}
				}
			}
			
		}
		
		// Assign the regions that have only single objective.
		if (objectiveMap.size() != objs.size()) {
			//logger.debug("single:\n");
			for (Objective obj : objs) {
				if (!objectiveMap.containsKey(obj)) {
					//logger.debug("single: " + obj.getName()+"\n");
					Region region = Region.getNewRegionInstanceByType(Region.selected);
					region.addObjective(obj);;
					objectiveMap.put(obj, region);
				}
			}
		}
		print();
	}
	
	
	/**
	 * This should be sync on 'lock', at the place where it is called.
	 */
	private void restartExistingOptimization(){
		for (final Map.Entry<Region,Tuple<Objective, String>> entry : map.entrySet()) {
			
			SSAScalingThreadPool.terminate(entry.getValue().getVal2());
			
			// Only trigger if the objective is still violated, should be fine as the Analyzer ensures
			// that the new set of objective always trigger after this method. This is because thie method is
			// called in the same thread as the model training.
			
			if (entry.getValue().getVal1().isViolate()) {
				final String uuid = UUID.randomUUID().toString();
				Future f = SSAScalingThreadPool.submitJob(new Runnable() {

					@Override
					public void run() {
						ControlBus.getInstance().doDecisionMaking(entry.getValue().getVal1(), uuid);
					}

				});
				
				SSAScalingThreadPool.putThread(uuid, f);

			}
		}
		// This should be fine as it is called with the lock's sync block.
		map.clear();
	}
	
	private void print(){
		logger.debug("******** region results **************\n");
		Set<Region> re = new HashSet<Region>();
		for (Map.Entry<Objective, Region> entry : objectiveMap.entrySet()) {
			//logger.debug("Objective: " + entry.getKey().getName() + ", region: " + entry.getValue() + "\n");
			if (re.contains(entry.getValue())) {
				continue;
			}
			re.add(entry.getValue());
			logger.debug("\n"+entry.getValue() + "====================\n");
			entry.getValue().print();
			//entry.getValue().print();
		}
		logger.debug("******** region results **************\n");
		logger.debug("******** total " + re.size() + " regions **************\n");
	}
}
