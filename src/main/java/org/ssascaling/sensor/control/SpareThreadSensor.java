package org.ssascaling.sensor.control;

import org.ssascaling.sensor.Sensor;

public class SpareThreadSensor implements Sensor {

	private double concurency = 0.0;
	private double maxConcurency = 0.0;
	
	// If less than maxSpare, then this sensor should be the same
	// as maxThread, but it can not exceed maxSpare.
	private double maxSpare = 0.0;
	
	@Override
	public double recordPriorToTask(Object value) {
		synchronized (this) {
			maxSpare = (Double)value;
			concurency++;
			if (maxConcurency < concurency && concurency <= maxSpare) {
				maxConcurency = concurency;
			}
		}
		return 0;
	}

	@Override
	public double recordPostToTask(double value) {
		synchronized (this) {
			concurency--;
		}
		return 0;
	}

	@Override
	public boolean isOutput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public synchronized double[] runMonitoring() {
		
		if(maxConcurency > maxSpare) {
			maxConcurency = maxSpare;
		}
		
		double current = maxConcurency;
		maxConcurency = concurency > maxSpare? maxSpare : concurency;
		return new double[]{current};
	}

	@Override
	public boolean isVMLevel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getName() {
		return new String[]{"minSpareThreads"};
	}

	@Override
	public void destory() {
		// TODO Auto-generated method stub

	}
	
	public void updateSpare(double maxSpare) {
		synchronized (this) {
		    this.maxSpare = maxSpare;
		}
	}

	@Override
	public void initInstance(Object object) {
		// TODO Auto-generated method stub
		
	}

}
