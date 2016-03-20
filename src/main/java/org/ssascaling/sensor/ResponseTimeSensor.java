package org.ssascaling.sensor;

import java.util.LinkedList;
import java.util.List;

public class ResponseTimeSensor implements Sensor {

	//final private List<Double> requests = new LinkedList<Double>();
	double total = 0.0;
	int requests = 0;
	@Override
	public synchronized double[] runMonitoring() {

		//double total = 0.0;
		/*for (double value : requests) {
			total += value;
		}*/
		int size = requests == 0? 1 : requests;
		//requests.clear();
		double[] result = new double[]{total/size};
		requests = 0;
		total = 0;
		return result;
	}

	@Override
	public double recordPriorToTask(Object value) {
		return System.currentTimeMillis();
	}

	@Override
	public synchronized double recordPostToTask(double value) {
		//requests.add((System.currentTimeMillis() - value) / 1000);
		total += (System.currentTimeMillis() - value) / 1000;
		requests++;
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

	@Override
	public void initInstance(Object object) {
		// TODO Auto-generated method stub
		
	}

}
