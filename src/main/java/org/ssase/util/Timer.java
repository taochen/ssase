package org.ssase.util;

/**
 * Sync should be ensured by the ControlBus, this is objective specific.
 * @author tao
 *
 */
public class Timer {

	// Number of sampling interval 
	private int violationTimer = 0;
	
	
	private static int violationThreshold = 1;
	
	public void increaseTimer(){
		violationTimer++;
	}
	
	
	public boolean isValidViolation (){
		boolean result = violationTimer >= violationThreshold;
		if (result) {
			violationTimer = 0;
		}
		return result;
	}
	
	public static void setThreshold (int no) {
		violationThreshold = no;
	}
}
