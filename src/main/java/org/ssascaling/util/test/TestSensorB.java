package org.ssascaling.util.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.ssascaling.Interval;
import org.ssascaling.network.Sender;
import org.ssascaling.sensor.*;
import org.ssascaling.sensor.linux.CpuSensor;

/**
 * This is running on the DomU.
 * 
 * 
 * 
 * 
 * @author tao
 *
 */
public class TestSensorB {
	
	private static String VM_ID = UUID.randomUUID().toString(); /*can be replaced to e.g., jeos*/
	
	private static Map<String, List<Sensor>> sensors;
	// If the data is sent to Dom0 gradually (one per sample) or sent as batch.
	private static final boolean isWriteOnce = false;
	
	
	private static long totalNumberOfSenceToTriggerSend = 2;
	private static long numberOfSenceToTriggerSend = 0;
	
	private static List<Interval> intervals;
	// Only for reference, not for feeding model 
	// For recording the data for ARMAX model
	private static List<Interval> preIntervals;
	// order of ARMAX model, this should be dynamic
	// so the declaration here is actually useless
	private static final int NUMBER_OF_ORDER = 2;
	private static final int MAX_DATA_RECORD = 10000;
	// This should be set properly usually around 30 - 120 secs
	private static int SAMPLING_INTERVAL = 1000;//30000
	private static final String prefix = //"/home/tao/monitor/";
		"/Users/tao/research/monitor/";
	
	private static Timer timer;
	
	private static Sender sender;
	
	private static StringBuilder simpleData = null;
	
	private static int readFileIndex = 0;
	
	public static void setVMID(String VM_ID) {
		TestSensorB.VM_ID = VM_ID;
	}
	
	public static void setSampleInterval(long sample) {
		totalNumberOfSenceToTriggerSend = sample;
	}
	
	public static void main (String[] a) {
		intervals = new LinkedList<Interval>();
		preIntervals = new LinkedList<Interval>();
		sensors = new HashMap<String, List<Sensor>> ();
		sender = new Sender();
		
		// This is temp implementation
		String[] services = new String[] {
				"edu.rice.rubis.servlets.AboutMe",
				"edu.rice.rubis.servlets.BrowseCategories"
		};
		
		
		
		
		List<Sensor> list = null;

		for (int i = 0; i < 4; i++) {

			try {
				Thread.sleep((long) 2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Interval interval = new Interval(System.currentTimeMillis());
			for (String service : services) {
				list = new ArrayList<Sensor>();
				list.add(new ResponseTimeSensor());
				interval.setY(service, new ResponseTimeSensor().getName(),
						new double[] { new Random().nextInt() });
				list.add(new WorkloadSensor());
				interval.setX(service, new WorkloadSensor().getName(),
						new double[] { new Random().nextInt() });
				list.add(new ConcurrencySensor());
				interval.setX(service, new ConcurrencySensor().getName(),
						new double[] { new Random().nextInt() });

				sensors.put(service, list);
			}

			
			interval.setVMX(new String[]{"CPU Usage"},
					new double[] { new Random().nextInt() });
			intervals.add(interval);

			if (!isWriteOnce) {
				/*
				 * for (Map.Entry<String, List<Sensor>> entry :
				 * monitors.entrySet()) { write(intervals, entry.getKey()); }
				 */

				StringBuilder data = new StringBuilder(VM_ID + "=1\n");
				for (Map.Entry<String, List<Sensor>> entry : sensors
						.entrySet()) {
					convert(intervals, entry.getKey(), data, VM_ID + "=1\n");
				}
				System.out.print(data);
				sender.send(data.toString());
				intervals.clear();
			} else {
				numberOfSenceToTriggerSend++;

				if (simpleData == null) {
					simpleData = new StringBuilder(VM_ID + "="
							+ totalNumberOfSenceToTriggerSend + "\n");
				}

			

				if (numberOfSenceToTriggerSend == totalNumberOfSenceToTriggerSend) {
					for (Map.Entry<String, List<Sensor>> entry : sensors
							.entrySet()) {
						convert(intervals, entry.getKey(), simpleData, VM_ID + "="
								+ totalNumberOfSenceToTriggerSend + "\n");
					}
					System.out.print(simpleData);
					sender.send(simpleData.toString());
					intervals.clear();
					numberOfSenceToTriggerSend = 0;
					simpleData = null;
				}
			}

		}
	}
	
	/**
	 * This should be triggered by a unicast from Dom0 using GMS.
	 * Also sending the delay for starting monitoring, in order to get
	 * on the same peacse with other VM.
	 */
	public static void init(long delay) {
		
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		intervals = new LinkedList<Interval>();
		preIntervals = new LinkedList<Interval>();
		sensors = new HashMap<String, List<Sensor>> ();
		sender = new Sender();
		
		// This is temp implementation
		String[] services = new String[] {
				"edu.rice.rubis.servlets.AboutMe",
				"edu.rice.rubis.servlets.BrowseCategories",
				"edu.rice.rubis.servlets.BrowseRegions",
				"edu.rice.rubis.servlets.StoreBuyNow",
				"edu.rice.rubis.servlets.BuyNow",
				"edu.rice.rubis.servlets.BuyNowAuth",
				"edu.rice.rubis.servlets.PutBid",
				"edu.rice.rubis.servlets.PutBidAuth",
				"edu.rice.rubis.servlets.PutComment",
				"edu.rice.rubis.servlets.PutCommentAuth",
				"edu.rice.rubis.servlets.RegisterItem",
				"edu.rice.rubis.servlets.RegisterUser",
				"edu.rice.rubis.servlets.SearchItemsByCategory",
				"edu.rice.rubis.servlets.SearchItemsByRegion",
				"edu.rice.rubis.servlets.SellItemForm",
				"edu.rice.rubis.servlets.StoreBid",
				"edu.rice.rubis.servlets.StoreComment",
				"edu.rice.rubis.servlets.ViewBidHistory",
				"edu.rice.rubis.servlets.ViewItem",
				"edu.rice.rubis.servlets.ViewUserInfo"
		};
		// Remember to uncomment this.
		Sensor cpu;// = new org.closlaes.sensor.linux.CpuSensor();
		Sensor memory;// = new  org.closlaes.sensor.linux.MemorySensor();
		
		List<Sensor> list = null;
		for (String service : services) {
			list = new ArrayList<Sensor>();
			list.add(new AvailabilitySensor());
			list.add(new ResponseTimeSensor());
			list.add(new ThroughputSensor());
			list.add(new WorkloadSensor());
			list.add(new ConcurrencySensor());
			//list.add(cpu);
			//list.add(memory);
			list.add(new ReliabilitySensor());
			list.add(new ServabilitySensor());
			sensors.put(service, list);
		}
		
		
		run();
	}
	
	public static double recordPriorToTask (String service, int index) {
		if (!sensors.containsKey(service)) {
			return 0.0;
		}
		
		List<Sensor> m = sensors.get(service);				
		return m.get(index).recordPriorToTask(0);
	}
		
	public static void recordPostToTask (String service, double value, int index) {

		
		if (!sensors.containsKey(service)) {
			return;
		}
		
		List<Sensor> m = sensors.get(service);
		m.get(index).recordPostToTask(value);
	}
	
	public static Object execute (String service, Object obj, Method method, Object[] args) {
		
		if (!sensors.containsKey(service)) {
			return invoke(obj, method,args);
		}
		
		List<Sensor> m = sensors.get(service);
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
		for (Map.Entry<String, List<Sensor>> entry : sensors.entrySet()) {
			for (Sensor monitor : entry.getValue()) {
				monitor.destory();
		    }
		}
	}
	
	public static void writeMonitorResult() {
		for (Map.Entry<String, List<Sensor>> entry : sensors.entrySet()) {
			writeMonitorResult(entry.getKey());
		}
	}
	
	public static void writeMonitorResult(String service) {
		if (!sensors.containsKey(service)) {
			return;
		}
		
		destory();
		if (isWriteOnce) {
		   /*write(preIntervals, service);
		   write(intervals, service);*/
		   // TODO send to Dom0
		}
	}
	
	public static void setSampleInterval(int sample) {
		SAMPLING_INTERVAL = sample;
	}
	
	public static int getSampleInterval(){
		return SAMPLING_INTERVAL;
	}

	/*private static void write (List<Interval> intervals, String service) {
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
			
			for (Map.Entry<String, BufferedWriter> writer : writers.entrySet()) {
				writer.getValue().close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
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
	
		System.out.print("Start loop\n");
		timer.schedule(new TimerTask() {
			@SuppressWarnings("unused")
			public void run() {
				if (MAX_DATA_RECORD == intervals.size() && isWriteOnce) {
					preIntervals.add(intervals.get(0));
					if (preIntervals.size() > NUMBER_OF_ORDER) {
						preIntervals.remove(0);
					}
					intervals.remove(0);
				}
				intervals.add(collectFromFiles()); 
				
				if (!isWriteOnce) {			
					/*for (Map.Entry<String, List<Sensor>> entry : monitors.entrySet()) {
						write(intervals, entry.getKey());
					}*/
					
					StringBuilder data = new StringBuilder(VM_ID + "=1\n");
					for (Map.Entry<String, List<Sensor>> entry : sensors.entrySet()) {
						convert(intervals, entry.getKey(), data, VM_ID + "=1\n");
					}
					sender.send(data.toString());
					intervals.clear();
				} else {
					numberOfSenceToTriggerSend++;
				
					
					if (simpleData == null) {
						simpleData = new StringBuilder(VM_ID + "=" + totalNumberOfSenceToTriggerSend + "\n");
					}
				
					if (numberOfSenceToTriggerSend == totalNumberOfSenceToTriggerSend) {
						
						
						for (Map.Entry<String, List<Sensor>> entry : sensors.entrySet()) {
							convert(intervals, entry.getKey(), simpleData,VM_ID + "=" + totalNumberOfSenceToTriggerSend + "\n");
						}
						
						
						sender.send(simpleData.toString());
						intervals.clear();
						numberOfSenceToTriggerSend = 0;
						simpleData = null;
					}
				}
				readFileIndex++;
			}
		}, SAMPLING_INTERVAL, SAMPLING_INTERVAL);

	}

	private static Interval collectFromSensors () {
		
		Interval interval = new Interval(System.currentTimeMillis());

		final Map<Sensor, double[]> vmResult = new HashMap<Sensor, double[]>();
		List<Sensor> m = null;
		Set<Map.Entry<String, List<Sensor>>> set = sensors.entrySet();
		for (Map.Entry<String, List<Sensor>> entry : set) {
			m = entry.getValue();
			for (Sensor monitor : m) {
				
			    if (monitor.isOutput()) {
			    	interval.setY(entry.getKey(), monitor.getName(), monitor.runMonitoring());
			    } else {
			    	if (!monitor.isVMLevel()) {
			    	   interval.setX(entry.getKey(), monitor.getName(), monitor.runMonitoring());
			    	} else {
			    		// To ensure that VM level CP's sensoring is run only once.
			    		if (!vmResult.containsKey(monitor)) {
			    			vmResult.put(monitor, monitor.runMonitoring());
			    		} 
			    		//interval.setX(entry.getKey(), monitor.getName(), componentResult.get(monitor));
			    		interval.setVMX( monitor.getName(), vmResult.get(monitor));
			    	}
			    }
		    }
		}
	    //interval.print();
		return interval;
	}
	
	private static Interval collectFromFiles () {
		
		Interval interval = new Interval(System.currentTimeMillis());
		System.out.print("Start collect\n");
		File root = new File(prefix +"adaptive/"+VM_ID+"/bak_3/");
		try {
		for (File file : root.listFiles()) {
			
			if (!file.isDirectory()) {
				if (file.getName().equals(".DS_Store")) {
					continue;
				}
				System.out.print("Read " + file.getAbsolutePath() + "\n");
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String line = null;
					String name = null;
					if ("CPU Usage.rtf".equals(file.getName())) {
						name = "CPU";
					} else if ("Memory Usage.rtf".equals(file.getName())) {
						name = "Memory";
					}
					int k = 0;
					while((line = reader.readLine()) != null) {
				
						
						if (k == readFileIndex) {
						
							
							interval.setVMX(new String[]{ name}, new double[]{Double.parseDouble(line)} );
						
							break;
						}
						
						k++;
					}
					
					reader.close();
			
			} else {
				
				// Services
				for (File subFile : file.listFiles()) {
					
					String line = null;
					String name = null;
					
					boolean isY = true;
					
					
					if ("Concurrency.rtf".equals(subFile.getName())) {
						name = "Concurrency";
						isY = false;
					} else if ("Workload.rtf".equals(subFile.getName())) {
						name = "Workload";
						isY = false;
					}else if ("Response Time.rtf".equals(subFile.getName())) {
						name = "Response Time";
						isY = true;
					}else if ("Throughput.rtf".equals(subFile.getName())) {
						name = "Throughput";
						isY = true;
					}else if ("Availability.rtf".equals(subFile.getName())) {
						name = "Availability";
						isY = true;
					}else if ("Reliability.rtf".equals(subFile.getName())) {
						name = "Reliability";
						isY = true;
					} else {
						continue;
					}
					System.out.print("Read " + subFile.getAbsolutePath() + "\n");
					BufferedReader reader = new BufferedReader(new FileReader(subFile));
					int k = 0;
					while((line = reader.readLine()) != null) {
				
						
						if (k == readFileIndex) {
						
							if (isY) {
							  interval.setY(file.getName(), new String[]{ name}, new double[]{Double.parseDouble(line)} );
									
							} else {
							  interval.setX(file.getName(), new String[]{ name}, new double[]{Double.parseDouble(line)} );
							}
							break;
						}
						
						k++;
					}
					
					reader.close();
				}
			}
			
		}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return interval;
	}
	

	// Convert Interval instance to low level protocol data. . = service,  1 = X, 2 = Y, 3 = VM X
	private static void convert (List<Interval> intervals, String service,StringBuilder builder, String init){		
		boolean isRecordVMX = false;
		if (builder.toString().equals(init)) {
			isRecordVMX = true;
		}
		
		builder.append(".\n");
		builder.append(service + "\n");
		
		for (Interval interval : intervals) {
			builder.append("1\n");
			for (Interval.ValuePair vp : interval.getXData(service)) {			
				builder.append(vp.getName() + "=" + String.valueOf(vp.getValue()) + "\n");
			}
			builder.append("2\n");
			for (Interval.ValuePair vp : interval.getYData(service)) {
				builder.append(vp.getName() + "=" + String.valueOf(vp.getValue()) + "\n");
			}
			
			// Only record this under the first service
			if (isRecordVMX) {
				builder.append("3\n");
				for (Interval.ValuePair vp : interval.getVMXData()) {
					builder.append(vp.getName() + "=" + String.valueOf(vp.getValue()) + "\n");
				}
			}
		}
		
		isRecordVMX = false;
		
	}
}
