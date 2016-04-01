package org.ssase.sensor.control;

import org.ssase.sensor.Sensor;

public class CompressionSensor implements Sensor {
	

	private Object invoker = null;
	private Object protocol = null;
	@Override
	public void initInstance(Object object) {
		Object[] objects = (Object[])object;
		protocol = objects[0];
		invoker = objects[1];		
	}
	
	@Override
	public double recordPriorToTask(Object value) {
		return 0;
	}

	@Override
	public double recordPostToTask(double value) {
		
		return 0;
	}

	@Override
	public boolean isOutput() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double[] runMonitoring() {
		synchronized (protocol) {
			if (invoker instanceof Invoker) {
				Object obj = ((Invoker) invoker).execute(null);
				// 0 = off, 1 = on.
				return new double[] { (Double) obj };
			}
		}
		return new double[]{0};
	}

	@Override
	public boolean isVMLevel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getName() {
		// TODO Auto-generated method stub
		return new String[]{"Compression"};
	}

	@Override
	public void destory() {
		// TODO Auto-generated method stub

	}
	

	public static double getDoubleByString(String s) {
		return "off".equals(s)? 0 : 1;
	}

	@Override
	public void setAlias(String alias) {
		// TODO Auto-generated method stub
		
	}


	
	
}
