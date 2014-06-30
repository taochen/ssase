package org.ssascaling.primitive;

public enum Type {
	Thread, CPU, Memory, Workload;
	
	public static Type getTypeByString(String name){
		if (name.equals("CPU")) {
			return CPU;
		} else if (name.equals("Memory")) {
			return Memory;
		}
		
		return null;
	}
}
