package org.ssase.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@SuppressWarnings("rawtypes")
public class SSAScalingThreadPool {

	private static ExecutorService pool= Executors.newCachedThreadPool();
	// This is used by the region and super region.

	private static Map<String, Future> threadFutureMap = new ConcurrentHashMap<String, Future>(); 
	
	public static void putThread(String t, Future f){
		threadFutureMap.put(t, f);
	}
	
	public static void removeThread(String t){
		threadFutureMap.remove(t);
	}
	
	public static void terminate(String t){
		threadFutureMap.get(t).cancel(true);
		threadFutureMap.remove(t);
	}
	
	
	public static Future submitJob(Runnable task){
		return pool.submit(task);
	}
	
	public static void executeJob(Runnable task){
		pool.execute(task);
	}
}
