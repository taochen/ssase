package org.ssascaling.sensor;


public interface Sensor {

	public double recordPriorToTask (Object value);
	
	public double recordPostToTask (double value);
	
	public boolean isOutput();
	
	public double[] runMonitoring();
	
	public boolean isVMLevel();
	
	public String[] getName();
	
	public void destory();
	
}
