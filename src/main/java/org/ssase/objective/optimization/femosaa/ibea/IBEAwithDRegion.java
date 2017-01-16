package org.ssase.objective.optimization.femosaa.ibea;

import org.ssase.objective.optimization.femosaa.FEMOSAASolutionAdaptor;

public class IBEAwithDRegion extends IBEARegion{

	protected void init(){
		if(vars == null) {
			vars = FEMOSAASolutionAdaptor.getInstance().convertInitialLimits(objectives.get(0));
			
		}
	}
}
