package org.ssase.objective.optimization.femosaa.nsgaii;

import org.ssase.objective.optimization.femosaa.FEMOSAASolutionAdaptor;

public class NSGAIIwithDRegion extends NSGAIIRegion{

	protected void init(){
		if(vars == null) {
			vars = FEMOSAASolutionAdaptor.getInstance().convertInitialLimits(objectives.get(0));
			
		}
	}
}
