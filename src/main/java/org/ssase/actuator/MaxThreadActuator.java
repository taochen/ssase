package org.ssase.actuator;

import org.ssase.executor.Executor;
import org.ssase.primitive.Type;

/**
 * This is not used anymore.
 * @author tao
 *
 */
public class MaxThreadActuator implements Actuator {
	
	private Invoker invoker;
	
	
	
	public MaxThreadActuator(Invoker invoker) {
		super();
		this.invoker = invoker;
	}



	@Override
	public boolean execute(String alias, long... value) {	
		if (Executor.isTest) {
			System.out.print("Setting " + alias + " with thread " + value[0] + "\n");
			return true;
		}
		return true;
	}

}
