package org.ssascaling.sensor.control;

import org.ssascaling.sensor.Sensor;
import org.ssascaling.sensor.Sensor.Invoker;

public class MemoryCacheSizeSensor implements Sensor {
	
	private double size;
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double recordPostToTask(double value) {
		synchronized (protocol) {
			if (invoker instanceof Invoker) {
				Object obj = ((Invoker) invoker).execute(null);
				
				long v = (Long) obj ;
				if (v > size) {
					size = v;
				}
			} 
			
		}
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
			double current = size;
			if (invoker instanceof Invoker) {
				Object obj = ((Invoker) invoker).execute(null);

				int v = (Integer) obj;
				size = v;

			}
			return new double[] { current };
		}
	}

	@Override
	public boolean isVMLevel() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getName() {
		// TODO Auto-generated method stub
		return new String[]{"maxBytesLocalHeap"};
	}

	@Override
	public void destory() {
		// TODO Auto-generated method stub

	}

}
