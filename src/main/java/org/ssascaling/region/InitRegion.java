package org.ssascaling.region;

import java.util.LinkedHashMap;
import java.util.Random;

import org.ssascaling.objective.Objective;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.util.Repository;

/**
 * This region is only used to generate some basic data before other experiments.
 * @author tao
 *
 */
public class InitRegion extends Region {

	public LinkedHashMap<ControlPrimitive, Double> optimize() {
		
		Random rand = new Random();
		LinkedHashMap<ControlPrimitive, Double> map = new LinkedHashMap<ControlPrimitive, Double>();
		// Dependency will be corrected later.
		// Only use the first one, as all control primitives are possible control primitives
		// for all objectives.
		double max = 0;
		Primitive minP = null;
		Primitive maxP = null;
		double min = 0;
		Objective obj = objectives.get(0);
			
			for (Primitive p : Repository.getDirectPrimitives(obj)) {
				
				if(p instanceof ControlPrimitive && !map.containsKey(p)) {
					
//					if("cacheMode".equals(p.getName())) {
//						//map.put((ControlPrimitive)p, 1D);
////						int index = rand.nextInt(((ControlPrimitive) p).getValueVector().length-1);
////						if(index != 2) {
////							index++;
////						}
////						map.put((ControlPrimitive)p, ((ControlPrimitive) p).getValueVector()[index]);
//						System.out.print("*********cacheMode number " + ((ControlPrimitive) p).getValueVector().length + "\n");
//						System.out.print("*********cacheMode last number " + ((ControlPrimitive) p).getValueVector()[ ((ControlPrimitive) p).getValueVector().length -1] + "\n");
//						
//						
//						//continue;
//					}
//					
//					if("Compression".equals(p.getName())) {
//						map.put((ControlPrimitive)p, 0D);
//						continue;
//					}
//					
//					if("maxBytesLocalHeap".equals(p.getName())) {
//						continue;
//					}
//					
//					if("maxBytesLocalDisk".equals(p.getName())) {
//						continue;
//					}
					
					if("maxThread".equals(p.getName())) {
						int index = rand.nextInt(((ControlPrimitive) p).getValueVector().length-1);
						max = ((ControlPrimitive) p).getValueVector()[index];
						maxP = p;
						map.put((ControlPrimitive)p, ((ControlPrimitive) p).getValueVector()[index]);
						continue;
					}
					
					if("minSpareThreads".equals(p.getName())) {
						int index = rand.nextInt(((ControlPrimitive) p).getValueVector().length-1);
						min =  ((ControlPrimitive) p).getValueVector()[index];
						minP = p;
						map.put((ControlPrimitive)p, ((ControlPrimitive) p).getValueVector()[index]);
						continue;
					}
					
					int index = rand.nextInt(((ControlPrimitive) p).getValueVector().length-1);
					map.put((ControlPrimitive)p, ((ControlPrimitive) p).getValueVector()[index]);
				}
				
			}
			
			if (min > max) {
				map.put((ControlPrimitive)minP, max);
			}
		
		
		return map;
		
	}
}
