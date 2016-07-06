package edu.rice.rubis.invoker;

import org.ssase.actuator.Invoker;

import edu.rice.rubis.beans.PageCacheManager;

public class MemoryCacheSizeInvoker implements Invoker {

	public boolean invoke(String service, long value) {

		PageCacheManager.getInstance().changeMaxMemorySize(value);
		System.out.print("memoerycachesize set to new value " + value + "\n");
		return true;
	}

}
