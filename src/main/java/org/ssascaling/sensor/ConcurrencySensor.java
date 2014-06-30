package org.ssascaling.sensor;

public class ConcurrencySensor implements Sensor {

	private double concurency = 0.0;
	private double maxConcurency = 0.0;
	
	public static final int index = 4;
	@Override
	public double recordPriorToTask(Object value) {
		synchronized (this) {
			concurency++;
			if (maxConcurency < concurency) {
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
		double current = maxConcurency;
		maxConcurency = concurency;
		return new double[]{current};
	}

	@Override
	public boolean isVMLevel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getName() {
		return new String[]{"Concurrency"};
	}

	@Override
	public void destory() {
		// TODO Auto-generated method stub

	}

}
