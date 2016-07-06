package edu.rice.rubis.invoker;

import org.ssase.actuator.Invoker;

import edu.rice.rubis.beans.PageCacheManager;

public class DiskCacheSizeInvoker implements Invoker {

	public boolean invoke(String service, long value) {

		PageCacheManager.getInstance().changeMaxDiskSize(value);
		System.out.print("diskcachesize set to new value " + value + "\n");
		return true;
	}

}
