package org.ssase.actuator.linux;

import org.ssase.actuator.Actuator;
import org.ssase.executor.Executor;

public class MemoryActuator implements Actuator {

	final String command = "/bin/sh /home/tao/actuator/memory_actuator";

	@Override
	public boolean execute(String alias, long... value) {
		
		
		if (Executor.isTest) {
			System.out.print("Setting " + alias + " with memory " + value[0] + "\n");
			return true;
		}
		
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(command + " " + alias + " " + value[0]);
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
