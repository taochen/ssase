package org.ssascaling.sensor;

/**
 * This may not be very accurate as it may be the case there is enough thread
 * but the monitor collect data before a request reaches its post action.
 * @author tao
 *
 */
public class ServabilitySensor implements Sensor {

	private long total = 0;
	private long processing = 0;
	private long startTime = System.currentTimeMillis();
	private double rejectTime = 0.0;
	
	

	@Override
	public synchronized double[] runMonitoring() {
		
		// If there are requests queuing, then mark this period
		// of time as not servable.
		if (processing != total) {
			rejectTime += SensoringController.getSampleInterval();
		}
		//System.out.println("Servability: total " + total + ", processing " + processing + ".\n");
		
		
		return new double[]{1 - rejectTime/(System.currentTimeMillis() - startTime)};
	}

	@Override
	public synchronized double recordPriorToTask(Object value) {
		total++;
		//System.out.println("Servability: total " + total);
		return 0;
	}

	@Override
	public synchronized double recordPostToTask(double value) {
		processing++;
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
		return new String[]{"Servability"};
	}

	@Override
	public void destory() {
		
	}

}
