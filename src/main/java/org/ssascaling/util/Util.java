package org.ssascaling.util;

import java.io.File;

import org.ssascaling.monitor.Monitor;

public class Util {

	
	public static double calculateEuclideanDistance(double[] input, double[] another) {
		// The two array needs to be as the same dimension.
		double result = 0;
		for (int i = 0; i < input.length; i++) {
			  result += Math.pow( (input[i] - another[i]), 2);
		}
		
		return Math.sqrt(result);
	}
	
	
	public static void readMeasuredData(){
	
		// We assume that historical data have been stored in memory.
		// TODO it may be possible that to remove the in-memory data after each training.
		
		
		File root = new File(Monitor.prefix);
		for (File folder : root.listFiles()) {
			
			// Means it is a hardware primitive
			if (!folder.isDirectory()) {
				
			} else {
				
				// Could have many services
				for (File file : folder.listFiles()) {
					
				}
			}
		}
	}
}
