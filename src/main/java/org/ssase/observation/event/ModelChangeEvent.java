package org.ssase.observation.event;

import org.ssase.objective.Objective;

public class ModelChangeEvent implements Event{

	
	private boolean isCritical = false;
	private Objective objective;

	public ModelChangeEvent(boolean isCritical,  Objective objective) {
		super();
		this.isCritical = isCritical;
		this.objective = objective.getMainObjective();
	}

	public boolean isCritical() {
		return isCritical;
	}
	
	public Objective getObjective(){
		return objective;
	}
	
	
	
}
