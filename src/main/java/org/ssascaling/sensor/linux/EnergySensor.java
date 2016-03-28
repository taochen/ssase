package org.ssascaling.sensor.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import org.ssascaling.sensor.Sensor;

public class EnergySensor implements Sensor {

	final String command = null;//"/bin/sh /root/monitor/cpu_monitor.sh";
	private double total = 0.0;
	private double number = 0.0;
	private static final int SAMPLING_INTERVAL = 3000;
	public static final int index = 5;
	private Timer timer;
	
	public EnergySensor(){
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				execute();
			}
		}, 100, SAMPLING_INTERVAL);
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
        double result = 0.0;
        BufferedReader br = null;
        Process p = null;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			final String line = br.readLine();
			result = Double.parseDouble(line);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				p.destroy();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		synchronized (this) {
			total += result;
			number ++;
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
		// TODO Auto-generated method stub

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
