package org.ssascaling.sensor;

public class WorkloadSensor implements Sensor {

	private double workload = 0.0;
	private double complete = 0.0;
	private long startTime = System.currentTimeMillis();

	public static final int index = 3;
	@Override
	public synchronized double[] runMonitoring() {
		long time = System.currentTimeMillis() - startTime;		
		startTime = System.currentTimeMillis();
		
		//double current = complete==0? 0 : workload;
		double current = workload;
		workload = workload - complete;
		complete = 0.0;
		return new double[]{current * 1000 / time};
	}

	@Override
	public double recordPriorToTask(Object value) {
		synchronized (this) {
			workload++;
		}
		return 0;
	}

	@Override
	public double recordPostToTask(double value) {
		synchronized (this) {
			complete++;
		}
		return 0;
	}


	@Override
	public boolean isOutput() {
		return false;
	}


	@Override
	public boolean isVMLevel() {
		return false;
	}
	
	@Override
	public String[] getName() {
		return new String[]{"Workload"};
	}
	
	@Override
	public void destory() {
		
	}
}
