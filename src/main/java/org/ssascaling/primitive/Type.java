package org.ssascaling.primitive;

import org.ssascaling.actuator.Actuator;
import org.ssascaling.actuator.linux.CPUCapActuator;
import org.ssascaling.actuator.linux.MemoryActuator;

public enum Type {
	Thread, CPU, Memory, Workload;
	
	public static Type getTypeByString(String name){
		if (name.equals("CPU")) {
			return CPU;
		} else if (name.equals("Memory")) {
			return Memory;
		} else if (name.equals("Concurrency") || name.equals("Thread")) {
			return Thread;
		} else if (name.equals("Workload")) {
			return Workload;
		}
		
		return null;
	}
	
	public static Actuator getActuatorByString(String name){
		if (name.equals("CPU")) {
			return new CPUCapActuator();
		} else if (name.equals("Memory")) {
			return new MemoryActuator();
		}
		
		
		return null;
	}
}
