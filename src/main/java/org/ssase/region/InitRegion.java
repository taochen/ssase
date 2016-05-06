package org.ssase.region;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jmetal.core.Variable;
import jmetal.encodings.variable.Int;
import jmetal.problems.SASSolution;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import org.ssase.objective.Objective;
import org.ssase.objective.optimization.femosaa.FEMOSAASolution;
import org.ssase.objective.optimization.femosaa.FEMOSAASolutionAdaptor;
import org.ssase.primitive.ControlPrimitive;
import org.ssase.primitive.Primitive;
import org.ssase.util.Repository;

/**
 * This region is only used to generate some basic data before other experiments.
 * @author tao
 *
 */
public class InitRegion extends Region {
	
	boolean init = false;
	
	
	
	public LinkedHashMap<ControlPrimitive, Double> optimize() {
		if (!init) {
			FEMOSAASolutionAdaptor.getInstance().convertInitialLimits(
					objectives.get(0));
			init = true;
		}

		List<ControlPrimitive> list = Repository
				.getSortedControlPrimitives(objectives.get(0));

		FEMOSAASolution dummy = new FEMOSAASolution();
		dummy.init(objectives, null);
		Variable[] variables = new Variable[list.size()];
		for (int i = 0; i < list.size(); i++) {
			variables[i] = new Int(0, list.get(i).getValueVector().length - 1);
		}

		dummy.setDecisionVariables(variables);

		for (int i = 0; i < dummy.getDecisionVariables().length; i++) {
			try {
				((SASSolution) dummy).mutateWithDependency(i, true);
			} catch (JMException e) {
				e.printStackTrace();
			}
		}

		
		LinkedHashMap<ControlPrimitive, Double> result = FEMOSAASolutionAdaptor.getInstance().convertSolution(dummy,
				objectives.get(0));

	
		print(result);
		
		return result;
		
	}

	public LinkedHashMap<ControlPrimitive, Double> backupOptimize() {
		
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
						int index = rand.nextInt(((ControlPrimitive) p).getValueVector().length);
						max = ((ControlPrimitive) p).getValueVector()[index];
						maxP = p;
						map.put((ControlPrimitive)p, ((ControlPrimitive) p).getValueVector()[index]);
						continue;
					}
					
					if("minSpareThreads".equals(p.getName())) {
						int index = rand.nextInt(((ControlPrimitive) p).getValueVector().length);
						min =  ((ControlPrimitive) p).getValueVector()[index];
						minP = p;
						map.put((ControlPrimitive)p, ((ControlPrimitive) p).getValueVector()[index]);
						continue;
					}
					
					int index = rand.nextInt(((ControlPrimitive) p).getValueVector().length);
					map.put((ControlPrimitive)p, ((ControlPrimitive) p).getValueVector()[index]);
				}
				
			}
			
			if (min > max) {
				map.put((ControlPrimitive)minP, max);
			}
		
		
		return map;
		
	}
}
