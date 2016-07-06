package edu.rice.rubis.invoker;

import org.ssase.actuator.Invoker;

public class QueryCacheSizeInvoker implements Invoker {

	final String command = "/bin/sh /root/actuator/query_cache_size_actuator";
	
	public boolean invoke(String service, long value) {

		
		System.out.print("querycachesize set to new value " + value + "\n");
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(command + " " + value);
			p.waitFor();
			} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			p.destroy();
		}

		return true;
	}

}
