package org.ssase.primitive;

import org.ssase.actuator.Actuator;
import org.ssase.actuator.linux.CPUCapActuator;
import org.ssase.actuator.linux.MemoryActuator;

public enum Type {
	cacheMode, Compression, Connection, maxBytesLocalDisk, maxBytesLocalHeap, 
	query_cache_size, minSpareThreads, maxThread, CPU, Memory, Workload;
	
	public static Type getTypeByString(String name){
		if (name.equals("CPU")) {
			return CPU;
		} else if (name.equals("Memory")) {
			return Memory;
		} else if (name.equals("cacheMode")) {
				return cacheMode;
		} else if (name.equals("Compression")) {
			return Compression;
		} else if (name.equals("Connection")) {
			return Connection;
		} else if (name.equals("maxBytesLocalDisk")) {
			return maxBytesLocalDisk;
		} else if (name.equals("maxBytesLocalHeap")) {
			return maxBytesLocalHeap;
		} else if (name.equals("query_cache_size")) {
			return query_cache_size;
		} else if (name.equals("minSpareThreads")) {
			return minSpareThreads;
		} else if (name.equals("Concurrency") || name.equals("maxThread")) {
			return maxThread;
		// There may be more than one EP, which all start with Workload
		} else if (name.equals("Workload") || name.startsWith("Workload")) {
			return Workload;
		}
		
		return null;
	}
	
	// This is only used by hardware CP. software CP  would be triggered
	// by ActuationSender
	public static Actuator getActuatorByString(String name){
		if (name.equals("CPU")) {
			return new CPUCapActuator();
		} else if (name.equals("Memory")) {
			return new MemoryActuator();
		} 
		
		
		return null;
	}
}
