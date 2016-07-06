package edu.rice.rubis.invoker;


import org.ssase.actuator.Invoker;

import edu.rice.rubis.beans.PageCacheManager;

public class CacheModeInvoker implements Invoker {


	public CacheModeInvoker() {
		

	}

	public boolean invoke(String service, long value) {
		PageCacheManager.getInstance().changeCacheMode(value);

		System.out.print("cacheMode set to new value " + value + "\n");
		return true;
	}

}
