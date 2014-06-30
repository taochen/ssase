package org.ssascaling.actuator;

import org.ssascaling.executor.Executor;


public class ThreadActuator implements Actuator {
	
	private Invoker invoker;
	
	
	
	public ThreadActuator(Invoker invoker) {
		super();
		this.invoker = invoker;
	}



	@Override
	public boolean execute(String alias, long... value) {	
		if (Executor.isTest) {
			System.out.print("Setting " + alias + " with thread " + value[0] + "\n");
			return true;
		}
		return invoker.invoke(alias, value[0]);
	}

}
