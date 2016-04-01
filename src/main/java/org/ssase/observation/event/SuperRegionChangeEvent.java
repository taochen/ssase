package org.ssase.observation.event;

import org.ssase.objective.Objective;

public class SuperRegionChangeEvent implements Event{

	// If for swap to another super region, only record the + one.
	private Operation[] operations;
	
	public Operation[] getOperation(){
		return operations;
	}
	
	public class Operation{
		
		private int opeator;
		
		
		private Objective obj;



		public int getOpeator() {
			return opeator;
		}


		public Objective getObj() {
			return obj;
		}


		public Operation(int opeator, Objective obj) {
			this.opeator = opeator;
			this.obj = obj;
		}
		
		
	}
}
