package org.ssase.sensor.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import org.ssase.sensor.Sensor;

public class EnergySensor implements Sensor {

	final String command = "sudo /bin/sh /root/monitor/energy_monitor.sh";
	private double total = 0.0;
	private double number = 0.0;
	public static final int index = 5;
	private BufferedReader br = null;
	private Process p = null;
	
	public EnergySensor(){
		execute();
	}
	
	@Override
	public double[] runMonitoring() {
		double current = 0.0;
		double no = 0.0;
		synchronized (this) {
		  current = total;
		  no = number;
		  total = 0.0;
		  number = 0.0;
		}
		
		return new double[] {current/no};
	}

	
	private void execute() {
     
       
		try {
			p = Runtime.getRuntime().exec(command);
			
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			while(true) {
			    final String line = br.readLine();
			    String[] result = line.split("=");
			    synchronized (this) {
					total += Double.parseDouble(result[result.length - 1]);
					number ++;
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				p.destroy();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public boolean isVMLevel() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String[] getName() {
		// TODO Auto-generated method stub
		return new String[]{"Energy"};
	}

	@Override
	public void destory() {
		try {
			p.destroy();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void initInstance(Object object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double recordPriorToTask(Object value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double recordPostToTask(double value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isOutput() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void setAlias(String alias) {
		// TODO Auto-generated method stub
		
	}

}
