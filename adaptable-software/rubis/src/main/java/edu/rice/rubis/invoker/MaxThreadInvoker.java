package edu.rice.rubis.invoker;

import org.ssase.actuator.Invoker;

import edu.rice.rubis.beans.StandardThreadExecutorWrapper;
import edu.rice.rubis.servlets.Config;

public class MaxThreadInvoker implements Invoker {

	private StandardThreadExecutorWrapper wrapper;
	
	public MaxThreadInvoker(StandardThreadExecutorWrapper wrapper) {
		super();
		this.wrapper = wrapper;
	}

	public boolean invoke(String service, long value) {	
		wrapper.setMaxThreads((int)value);
		System.out.print("maxThread set to new value " + value + "\n");
		return true;
	}

}
