package org.ssascaling.sensor.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.ssascaling.Interval;
import org.ssascaling.sensor.Sensor;
import org.ssascaling.sensor.database.mysql.WorkloadSensor;
import org.ssascaling.sensor.linux.CpuSensor;
import org.ssascaling.sensor.linux.MemorySensor;

public class LinuxMysqlSensor {
	
	private static Map<String, List<Sensor>> monitors;
	private static List<Interval> intervals;
	// Only for reference, not for feeding model
	private static List<Interval> preIntervals;
	private static final int NUMBER_OF_ORDER = 2;
	private static final int MAX_DATA_RECORD = 10000;
	public static final int SAMPLING_INTERVAL = 30000;
	private static final String prefix = "/home/tao/monitor/";
	
	private static Timer timer;
	
	public static void main (String[] args) {
			init();		
	}
	
	public static void init() {
		intervals = new LinkedList<Interval>();
		preIntervals = new LinkedList<Interval>();
		monitors = new HashMap<String, List<Sensor>> ();
		
		// This is temp implementation
		String[] services = new String[] {
				"database.service",
		};
		
		Sensor cpu = new CpuSensor();
		Sensor memory = new MemorySensor();
		Sensor workload = new WorkloadSensor();
		List<Sensor> list = null;
		for (String service : services) {
			list = new ArrayList<Sensor>();
			list.add(workload);
			list.add(cpu);
			list.add(memory);
			monitors.put(service, list);
		}
		
		
		run();
	}
	
	public static double recordPriorToTask (String service, int index) {
		if (!monitors.containsKey(service)) {
			return 0.0;
		}
		
		List<Sensor> m = monitors.get(service);				
		return m.get(index).recordPriorToTask(0);
	}
		
	public static void recordPostToTask (String service, double value, int index) {

		
		if (!monitors.containsKey(service)) {
			return;
		}
		
		List<Sensor> m = monitors.get(service);
		m.get(index).recordPostToTask(value);
	}
	
	public static Object execute (String service, Object obj, Method method, Object[] args) {
		
		if (!monitors.containsKey(service)) {
			return invoke(obj, method,args);
		}
		
		List<Sensor> m = monitors.get(service);
		List<Double> preResults = new ArrayList<Double>();
		for (Sensor monitor : m) {
			preResults.add(monitor.recordPriorToTask(0));
		}
		
		final Object result = invoke(obj, method,args);	
		
		for (int i = 0; i < preResults.size(); i++) {
			m.get(i).recordPostToTask(preResults.get(i));
		}
		
		return result;
		
	}
	
	public static void destory() {
		timer.cancel();
		for (Map.Entry<String, List<Sensor>> entry : monitors.entrySet()) {
			for (Sensor monitor : entry.getValue()) {
				monitor.destory();
		    }
		}
	}
	
	public static void writeMonitorResult() {
		for (Map.Entry<String, List<Sensor>> entry : monitors.entrySet()) {
			writeMonitorResult(entry.getKey());
		}
	}
	
	public static void writeMonitorResult(String service) {
		if (!monitors.containsKey(service)) {
			return;
		}
		
		//destory();
		write(preIntervals, service);
		write(intervals, service);
	}
	

	private static void write (List<Interval> intervals, String service) {
		final Map<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
		BufferedWriter bw = null;
		File file = null;
		try {
			for (Interval interval : intervals) {
					
				for (Interval.ValuePair vp : interval.getXData(service)) {
					if (!writers.containsKey(vp.getName())) {
						if(!(file = new File(prefix + service + "/")).exists()){
							file.mkdir();
						}
						writers.put(vp.getName(), new BufferedWriter(new FileWriter(
								prefix + service + "/" + vp.getName() + ".rtf" , true)));
					}
					bw = writers.get(vp.getName());
					bw.write(String.valueOf(vp.getValue()));
					bw.newLine();
				}
				
				if (interval.getYData(service) != null) {
								
					
					for (Interval.ValuePair vp : interval.getYData(service)) {
						if (!writers.containsKey(vp.getName())) {
							writers.put(vp.getName(), new BufferedWriter(new FileWriter(
									prefix + service + "/" + vp.getName() + ".rtf", true)));
						}
						bw = writers.get(vp.getName());
						bw.write(String.valueOf(vp.getValue()));
						bw.newLine();
					}
				}
				
			}
			for (Map.Entry<String, BufferedWriter> writer : writers.entrySet()) {
				writer.getValue().close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Object invoke (Object obj, Method method, Object[] args) {
		try {
			 return method.invoke(obj, args);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void run() {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				
				
				
				
				if (MAX_DATA_RECORD == intervals.size()) {
					preIntervals.add(intervals.get(0));
					if (preIntervals.size() > NUMBER_OF_ORDER) {
						preIntervals.remove(0);
					}
					intervals.remove(0);
				}
				intervals.add(collect()); 
				
				if (intervals.size() > 1) {
				
					writeMonitorResult();
					intervals.clear();
					preIntervals.clear();
				}
			}
		}, SAMPLING_INTERVAL, SAMPLING_INTERVAL);

	}

	private static Interval collect () {
		
		Interval interval = new Interval(System.currentTimeMillis());

		final Map<Sensor, double[]> componentResult = new HashMap<Sensor, double[]>();
		List<Sensor> m = null;
		Set<Map.Entry<String, List<Sensor>>> set = monitors.entrySet();
		for (Map.Entry<String, List<Sensor>> entry : set) {
			m = entry.getValue();
			for (Sensor monitor : m) {
			    if (monitor.isOutput()) {
			    	interval.setY(entry.getKey(), monitor.getName(), monitor.runMonitoring());
			    } else {
			    	if (!monitor.isVMLevel()) {
			    	   interval.setX(entry.getKey(), monitor.getName(), monitor.runMonitoring());
			    	} else {
			    		if (!componentResult.containsKey(monitor)) {
			    			componentResult.put(monitor, monitor.runMonitoring());
			    		} 
			    		interval.setX(entry.getKey(), monitor.getName(), componentResult.get(monitor));
			    	}
			    }
		    }
		}
	    //interval.print();
		return interval;
	}
}
