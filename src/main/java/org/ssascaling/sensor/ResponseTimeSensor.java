package org.ssascaling.sensor;

import java.util.LinkedList;
import java.util.List;

public class ResponseTimeSensor implements Sensor {

	final private List<Double> requests = new LinkedList<Double>();
	public static final int index = 1;

	@Override
	public synchronized double[] runMonitoring() {

		double total = 0.0;
		for (double value : requests) {
			total += value;
		}
		int size = requests.size() == 0? 1 : requests.size();
		requests.clear();
		return new double[]{total/size};
	}

	@Override
	public double recordPriorToTask(Object value) {
		return System.currentTimeMillis();
	}

	@Override
	public synchronized double recordPostToTask(double value) {
		requests.add((System.currentTimeMillis() - value) / 1000);
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
		return new String[]{"Response Time"};
	}
	
	@Override
	public void destory() {
		
	}

}
