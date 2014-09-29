package org.ssascaling.sensor;

public class ReliabilitySensor implements Sensor {
	
	
	private long total = 0;
	
	private long violated = 0;

	private double timeout = 30.0;//ms 120

	@Override
	public double recordPriorToTask(Object value) {		
		return System.currentTimeMillis();
	}

	@Override
	public synchronized double recordPostToTask(double value) {
		
		double time = System.currentTimeMillis() - value;
		
		if (time > timeout) {
			violated++;
		}
		
		total++;
		//System.out.println("Reliability: total " + total + ", violated " + violated + ".\n");
		return 0;
	}

	@Override
	public boolean isOutput() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public synchronized double[] runMonitoring() {
		//System.out.println("Reliability: total " + total + ", violated " + violated + ".\n");
		
		//System.out.println((double)(total - violated)/total+"\n");
		return new double[]{total==0? 1 : (double)(total - violated)/total};
	}

	@Override
	public boolean isVMLevel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getName() {
		// TODO Auto-generated method stub
		return new String[]{"Reliability"};
	}

	@Override
	public void destory() {
		// TODO Auto-generated method stub

	}

}
