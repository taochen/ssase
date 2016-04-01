package org.ssase.actuator.linux;

import org.ssase.actuator.Actuator;
import org.ssase.executor.Executor;

public class ReplicationActuator implements Actuator {

	final String command = "/bin/sh /home/tao/actuator/replication_actuator";
	
	final private static ReplicationActuator instance = new ReplicationActuator();
	
	private ReplicationActuator(){
		
	}
	
	public static ReplicationActuator getInstance(){
		return instance;
	}
	
	@Override
	public boolean execute(String alias, long... value) {
		return true;
	}
	
	/**
	 * Value: 0 = scale out, 1 = scale in
	 */
	
	public boolean oldExecute(String alias, long... value) {
		
		if (Executor.isTest) {
			System.out.print("Setting " + alias + " for " + (value[0]==0?"scale out" : "scale in") + "\n");
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
