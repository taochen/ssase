package org.ssascaling.sensor;

import java.util.LinkedList;
import java.util.List;

public class AvailabilitySensor implements Sensor {

	final private List<Double> requests = new LinkedList<Double>();
	private long startTime = System.currentTimeMillis();
	private double downTime = 0.0;
	private double timeout = 60.0;//ms 120
	
	public static final int index = 0;

	@Override
	public synchronized double[] runMonitoring() {
		
		double total = 0.0;
		for (double value : requests) {
			total += value;
		}
		int size = requests.size() == 0? 1 : requests.size();
		requests.clear();
		double average = total/size;
		
		if (average > timeout) {
			downTime += SensoringController.getSampleInterval();
		}
		
		return new double[]{1 - downTime/(System.currentTimeMillis() - startTime)};
	}

	@Override
	public double recordPriorToTask(Object value) {
		return System.currentTimeMillis();
	}

	@Override
	public synchronized double recordPostToTask(double value) {
		requests.add((System.currentTimeMillis() - value));
		return 0;
	}

	@Override
	public boolean isOutput() {
		return true;
	}

	@Override
	public boolean isVMLevel() {
		return false;
	}

	@Override
	public String[] getName() {
		return new String[]{"Availability"};
	}

	@Override
	public void destory() {
		
	}

}
