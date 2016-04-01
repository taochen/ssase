package org.ssase.sensor.database.mysql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import org.ssase.sensor.Sensor;

public class WorkloadSensor implements Sensor {

	final String command = "/bin/sh /home/tao/monitor/workload_monitor.sh";

	
	private double minus = 0.0;
	
	private long startTime = System.currentTimeMillis();
	
	public static final int index = 3;
	
	
	public WorkloadSensor(){
		minus = execute();
	}

	@Override
	public synchronized double[] runMonitoring() {
		
		long time = System.currentTimeMillis() - startTime;		
		startTime = System.currentTimeMillis();
		
		double result = execute();
		double original = result;
		result = result - minus - 3;
		//System.out.print("Result: " + result + " Minus: " + minus + "\n");
		minus = original;
	
		return new double[] {result * 1000 / time};
	}
	
	private double execute() {
        double result = 0.0;
		
		try {
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			final String line = br.readLine();
			
			result = Double.parseDouble(line);
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return result;
		
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
		return false;
	}
	

	@Override
	public boolean isVMLevel() {
		return true;
	}
	
	@Override
	public String[] getName() {
		return new String[]{"Workload"};
	}
	
	@Override
	public void destory() {
	
	}

	@Override
	public void initInstance(Object object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAlias(String alias) {
		// TODO Auto-generated method stub
		
	}
}
