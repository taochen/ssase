package org.ssascaling.sensor;

public class ThroughputSensor implements Sensor {

	private double completed = 0.0;
	private long startTime = System.currentTimeMillis();

	public static final int index = 2;
	@Override
	public synchronized double[] runMonitoring() {
		long time = System.currentTimeMillis() - startTime;		
		startTime = System.currentTimeMillis();
		double current = completed;
		completed = 0.0;
		return new double[]{current * 1000 / time};
	}

	@Override
	public double recordPriorToTask(Object value) {
		return 0;
	}

	@Override
	public synchronized double recordPostToTask(double value) {
		completed++;
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
		return new String[]{"Throughput"};
	}
	
	@Override
	public void destory() {
		
	}
}
