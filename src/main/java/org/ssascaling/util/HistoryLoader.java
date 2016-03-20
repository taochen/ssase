package org.ssascaling.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.ssascaling.ControlBus;
import org.ssascaling.Interval;
import org.ssascaling.Service;
import org.ssascaling.executor.VM;
import org.ssascaling.monitor.Monitor;
import org.ssascaling.primitive.ControlPrimitive;
import org.ssascaling.primitive.Primitive;
import org.ssascaling.qos.QualityOfService;
import org.ssascaling.sensor.AvailabilitySensor;
import org.ssascaling.sensor.ReliabilitySensor;
import org.ssascaling.sensor.ResponseTimeSensor;
import org.ssascaling.sensor.Sensor;
import org.ssascaling.sensor.ServabilitySensor;
import org.ssascaling.sensor.ThroughputSensor;
import org.ssascaling.sensor.WorkloadSensor;
import org.ssascaling.sensor.control.ThreadSensor;
import org.ssascaling.util.Repository;
import org.ssascaling.util.Triple;


public class HistoryLoader {
	
	private String[] qosStrings = new String[] {
		"Response Time",
		"Throughput",
		"Availability",
		"Reliability"
	};

	private  String[] notModeledServiceStrings = new String[] {
		
		"edu.rice.rubis.servlets.SearchItemsByCategory",
		"edu.rice.rubis.servlets.PutBid",
		"edu.rice.rubis.servlets.StoreBid",
		"edu.rice.rubis.servlets.BrowseCategories",
		"edu.rice.rubis.servlets.RegisterUser",
		
		
		"edu.rice.rubis.servlets.AboutMe",
		"edu.rice.rubis.servlets.BrowseRegions",
		"edu.rice.rubis.servlets.StoreBuyNow",
		"edu.rice.rubis.servlets.BuyNow",
		"edu.rice.rubis.servlets.BuyNowAuth",
		"edu.rice.rubis.servlets.PutBidAuth",
		"edu.rice.rubis.servlets.PutComment",
		"edu.rice.rubis.servlets.PutCommentAuth",
		"edu.rice.rubis.servlets.RegisterItem",
		"edu.rice.rubis.servlets.SearchItemsByRegion",
		"edu.rice.rubis.servlets.SellItemForm",
		"edu.rice.rubis.servlets.StoreComment",
		"edu.rice.rubis.servlets.ViewBidHistory",
		"edu.rice.rubis.servlets.ViewItem",
		"edu.rice.rubis.servlets.ViewUserInfo"
	};
	
	private  final String prefix = "/home/tao/backup/bak4/";
		//"/Users/tao/research/monitor/";
	
	public  int counterNo = 0;
	
	private  int readFileIndex = 0;
	private  int cap = 88;//340/*342*/;
	private  boolean finished = false;
	private  List<Interval> intervals;
	private  Map<String, List<Sensor>> sensors;
	
	private  AtomicInteger counter = new AtomicInteger(0);
	private  boolean isGo = false;
	
	
	public static void main(String[] args) {
		new HistoryLoader().run();
	}
	
	/**
	 * @param args
	 */
	public void run() {
		//Ssascaling.main(new String[]{"0"});
		init();
		
		System.out.print("Start the pre-loading\n");
		long time = System.currentTimeMillis();
		for (int i = 0; i < (cap+1)/*343*/; i ++) {
			
			
			
			System.out.print("The " + i + " run \n");
			
			simulateSendAndReceive("jeos");
			simulateSendAndReceive("kitty");
			simulateSendAndReceive("miku");
		
			
			if (finished) {
				break;
			}
			
			
			
			for (Service s : Repository.getAllServices() ) {
				for (Primitive p : s.getPrimitives()) {
					p.addValue(i+1);
				}
			}
			
			for (VM v : Repository.getAllVMs()) {
				for (Primitive p : v.getAllPrimitives()){
					p.addValue(i+1);
				}
			}
			
			for (final QualityOfService qos : Repository.getQoSSet()) {
				qos.doAddValue(i+1);
			}
			
			ControlBus.getInstance().increaseCurrentSampleCount();
			Monitor.outputCurrentSample();
			
		
			
			
			for (final QualityOfService qos : Repository.getQoSSet()) {
				
				
				
				new Thread(new Runnable(){

					@Override
					public void run() {
						
						//QualityOfService qos = (QualityOfService)Repository.getService(name.split("=")[0]).getObjective(name.split("=")[1]);
						train(qos);
					}
					
				}).start();
			}
			
			//train((QualityOfService)Repository.getService("jeos-"+Configurator.service).getObjective("Response Time"));
			
			synchronized(counter) {
					while (!isGo) {
						try {
							counter.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						
					}
					
					counter.set(0);
					isGo = false;
				
			}
			
			
			readFileIndex++;
			
		}
		
		time = System.currentTimeMillis() - time;
		System.out.print("Number of QoS: " + Repository.getQoSSet().size() + " \n");
		System.out.print("Used time for training: " + time + " ms \n");
		System.out.print("Average used time for training: " + (time/Repository.getQoSSet().size()) + " ms \n");
		
		intervals.clear();
		sensors.clear();
		intervals = null;
		sensors = null;
		ControlPrimitive.isPreLoad = false;
		
		System.gc();
	
	}
	
	
	private  void train (QualityOfService qos) {
		
	
		qos.doTraining();
		
		
		synchronized (counter){
			counter.incrementAndGet();
			if (counter.get() == Repository.getQoSSet().size()) {
				isGo = true;
				counter.notifyAll();
			}
		}
	}
	
	
	
	
	public  void simulateSendAndReceive(String VM_ID){
		intervals.add(collectFromFiles(VM_ID));
		
		if (finished) {
			return;
		}
		
		Monitor.write(1);
		
		StringBuilder data = new StringBuilder(VM_ID+"=1\n");
		for (Map.Entry<String, List<Sensor>> entry : sensors.entrySet()) {
			convert(intervals, entry.getKey(), data, VM_ID+"=1\n");
		}

		intervals.clear();
		
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(data.toString().getBytes()));
		write(in);
		
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
      public  void init() {
		
    	intervals = new LinkedList<Interval>();
		
		sensors = new HashMap<String, List<Sensor>> ();
		
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
			list.add(new ThreadSensor());
			//list.add(cpu);
			//list.add(memory);
			list.add(new ReliabilitySensor());
			list.add(new ServabilitySensor());
			sensors.put(service, list);
		}
		
		
	}
	
	
    private  Interval collectFromFiles (String VM_ID) {
		
		Interval interval = new Interval(System.currentTimeMillis());
		//System.out.print("Start collect\n");
		//File root = new File(prefix +"adaptive/"+VM_ID+"/bak_3/");
		File root = new File(prefix +VM_ID+"/");
		
		
		Map<String, List<String>> services = new HashMap<String, List<String>>();
		Map<String, List<String>> notModeledService = new HashMap<String, List<String>>();
	
		
		
		services.put("jeos", new ArrayList<String>());
		services.put("kitty", new ArrayList<String>());
		services.put("miku", new ArrayList<String>());
		
		notModeledService.put("jeos", new ArrayList<String>());
		notModeledService.put("kitty", new ArrayList<String>());
		notModeledService.put("miku", new ArrayList<String>());
		
		
		for (String s : notModeledServiceStrings) {
			notModeledService.get("jeos").add(s);
			notModeledService.get("kitty").add(s);
			notModeledService.get("miku").add(s);
			
			/*services.get("jeos").add(s);
			services.get("kitty").add(s);
			services.get("miku").add(s);*/
		}
		
		
		services.get("jeos").add("edu.rice.rubis.servlets.SearchItemsByCategory");
		services.get("jeos").add("edu.rice.rubis.servlets.BrowseCategories");
		notModeledService.get("jeos").remove("edu.rice.rubis.servlets.SearchItemsByCategory");
		notModeledService.get("jeos").remove("edu.rice.rubis.servlets.BrowseCategories");
		
		services.get("kitty").add("edu.rice.rubis.servlets.SearchItemsByRegion");
		services.get("kitty").add("edu.rice.rubis.servlets.BrowseCategories");
		notModeledService.get("kitty").remove("edu.rice.rubis.servlets.SearchItemsByRegion");
		notModeledService.get("kitty").remove("edu.rice.rubis.servlets.BrowseCategories");
		
		services.get("miku").add("edu.rice.rubis.servlets.BrowseRegions");
		services.get("miku").add("edu.rice.rubis.servlets.SearchItemsByCategory");
		notModeledService.get("miku").remove("edu.rice.rubis.servlets.BrowseRegions");
		notModeledService.get("miku").remove("edu.rice.rubis.servlets.SearchItemsByCategory");
		
		
		List<String> qos = new ArrayList<String>();
		for (String s : qosStrings) {
			qos.add(s);
		}
		try {
		for (File file : root.listFiles()) {
			
			
			
			
			if (!file.isDirectory()) {
				
				if (file.getName().equals(".DS_Store")) {
					continue;
				}
				
				if (file.getName().equals("Executions.rtf")) {
					continue;
				}
			
				
				//System.out.print("Read " + file.getAbsolutePath() + "\n");
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String line = null;
					String name = null;
					if ("CPU.rtf".equals(file.getName())) {
						name = "CPU";
					} else if ("Memory.rtf".equals(file.getName())) {
						name = "Memory";
					}
					int k = 0;
					int j = 0;
					while((line = reader.readLine()) != null) {
						

						/*if (j <= 87) {
							j++;
							continue;
						}*/
						
						
						if (k == readFileIndex) {
						
							
							interval.setVMX(new String[]{ name}, new double[]{Double.parseDouble(line)} );
						
							//System.out.print(name + " ***** "+line+"\n");
							
							break;
						}
						
						k++;
					}
					
					if (readFileIndex>k || readFileIndex==cap) {
						finished = true;
						reader.close();
						return null;
					}
					
					reader.close();
			
			} else {
				
				// Services
				for (File subFile : file.listFiles()) {
					
					String line = null;
					String name = null;
					
					boolean isY = true;
					
					// If the subfile is not workload and the service is not being modeled
					if (notModeledService.get(VM_ID).contains(file.getName()) && !("Workload.rtf".equals(subFile.getName()))) {
						continue;
					}
					
					//System.out.print(file.getName()+"-"+subFile.getName()+"\n");
					if ("Concurrency.rtf".equals(subFile.getName())) {
						name = "Concurrency";
						isY = false;
					} else if ("Workload.rtf".equals(subFile.getName())) {
						name = "Workload";
						isY = false;
					}else if ("Response Time.rtf".equals(subFile.getName())) {
					
						name = "Response Time";
						isY = true;
						if(!qos.contains(name)) {
							continue;
						}
					}else if ("Throughput.rtf".equals(subFile.getName())) {
						name = "Throughput";
						isY = true;
						if(!qos.contains(name)) {
							continue;
						}
					}else if ("Availability.rtf".equals(subFile.getName())) {
						name = "Availability";
						isY = true;
						if(!qos.contains(name)) {
							continue;
						}
					}else if ("Reliability.rtf".equals(subFile.getName())) {
						name = "Reliability";
						isY = true;
						if(!qos.contains(name)) {
							continue;
						}
					} else {
						continue;
					}
					//System.out.print("Read " + subFile.getAbsolutePath() + "\n");
					BufferedReader reader = new BufferedReader(new FileReader(subFile));
					int k = 0;
					int j = 0;
					while((line = reader.readLine()) != null) {

						/*if (j <= 87) {
							j++;
							continue;
						}*/
						
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
	private  void convert (List<Interval> intervals, String service,StringBuilder builder, String init){		
		boolean isRecordVMX = false;
		if (builder.toString().equals(init)) {
			isRecordVMX = true;
		}
		
		builder.append(".\n");
		builder.append(service + "\n");
		
		for (Interval interval : intervals) {
			if (interval.getXData(service) != null) {
				builder.append("1\n");
				for (Interval.ValuePair vp : interval.getXData(service)) {			
					builder.append(vp.getName() + "=" + String.valueOf(vp.getValue()) + "\n");
				}
			}
			
			if (interval.getYData(service) != null) {
				builder.append("2\n");
				for (Interval.ValuePair vp : interval.getYData(service)) {
					builder.append(vp.getName() + "=" + String.valueOf(vp.getValue()) + "\n");
				}
			}
			// Only record this under the first service
			if (isRecordVMX) {
				if (interval.getVMXData() != null) {
					builder.append("3\n");
					for (Interval.ValuePair vp : interval.getVMXData()) {
						builder.append(vp.getName() + "=" + String.valueOf(vp.getValue()) + "\n");
					}
				}
			}
		}
		
		isRecordVMX = false;
		
	}
	
	private  Triple<List<String>, Interval, String> convert (DataInputStream is){
		Interval interval = new Interval(System.currentTimeMillis());
		List<String> services = new ArrayList<String>();
		int type = 0;
		boolean isService = false;
		String currentService = null;
		String line = null;
		String VM_ID = null;
		try {
			VM_ID = is.readLine();
			while ((line = is.readLine()) != null) {
				//System.out.print(line + "\n");
				if (line.equals(".")) {
					isService = true;
					continue;
				} else if (line.equals("1")) {
					type = 1;
					continue;
				} else if (line.equals("2")) {
					// It is Y
					type = 2;
					continue;
				} else if (line.equals("3")) {
					// It is VM X
					type = 3;
					continue;
				}
				
				

				
				
				if (isService) {
					services.add(line);
					currentService = line;
					isService = false;
					continue;
				}
				
				// e.g., workload=34.5
				String[] args = line.split("="); 
				
				switch (type) {
				case 1:
					interval.setX(currentService, new String[]{args[0]}, new double[]{Double.parseDouble(args[1]) });
					break;
				case 2:
					interval.setY(currentService, new String[]{args[0]}, new double[]{Double.parseDouble(args[1]) });
					break;
				case 3:
					interval.setVMX(new String[]{args[0]}, new double[]{Double.parseDouble(args[1]) });
					break;
				}
				
				
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new Triple<List<String>, Interval, String>(services, interval, VM_ID);
	}
	
	public  boolean write(DataInputStream is){
		
		Triple<List<String>, Interval, String> triple = convert(is);
		final String VM_ID = triple.getVal3().split("=")[0];
		writeVM(triple.getVal2(), VM_ID);
		for (String service : triple.getVal1()) {
			// key here is VM ID + '-' + service name
			write(triple.getVal2(), service, VM_ID);
		}
		

	
		
		return false;
	}
	
	
	private  void writeVM (Interval interval, String VM_ID) {
		//final Map<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
		// The newly measured values.
		final Map<String, List<Double>> values = new HashMap<String,  List<Double>>();
		BufferedWriter bw = null;
		File file = null;
		try {
			
			if ( interval.getVMXData() != null) {
			
			for (Interval.ValuePair vp : interval.getVMXData()) {
				
				
				if (!values.containsKey(vp.getName())) {
					values.put(vp.getName(), new ArrayList<Double>());
				}
				
				values.get(vp.getName()).add(vp.getValue());
			}
			}
			
				for (Map.Entry<String, List<Double>> entry : values.entrySet()) {
					
					double[] value = new double[entry.getValue().size()];
					for (int k = 0; k < value.length; k++) {
						value[k] = entry.getValue().get(k);
					}
					prepareToAddValueForHardwareCP(VM_ID, entry.getKey(), value);
				}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private  void write(Interval interval, String service, String VM_ID) {

		//final Map<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
		// The newly measured values.
		final Map<String, List<Double>> xValues = new HashMap<String, List<Double>>();
		final Map<String, List<Double>> yValues = new HashMap<String, List<Double>>();
		BufferedWriter bw = null;
		File file = null;
		try {

			if ( interval.getXData(service) != null) {
			for (Interval.ValuePair vp : interval.getXData(service)) {
				

				if (!xValues.containsKey(vp.getName())) {
					xValues.put(vp.getName(), new ArrayList<Double>());
				}

				xValues.get(vp.getName()).add(vp.getValue());
			}
			
			}

			// For Y
			if ( interval.getYData(service) != null) {
			for (Interval.ValuePair vp : interval.getYData(service)) {
				

				if (!yValues.containsKey(vp.getName())) {
					yValues.put(vp.getName(), new ArrayList<Double>());
				}

				yValues.get(vp.getName()).add(vp.getValue());
			}

			}
			for (Map.Entry<String, List<Double>> entry : xValues.entrySet()) {

				double[] value = new double[entry.getValue().size()];
				for (int k = 0; k < value.length; k++) {
					value[k] = entry.getValue().get(k);
				}
				prepareToAddValueForPrimitive(VM_ID, service, entry.getKey(), value);
			}

			for (Map.Entry<String, List<Double>> entry : yValues.entrySet()) {

				double[] value = new double[entry.getValue().size()];
				for (int k = 0; k < value.length; k++) {
					value[k] = entry.getValue().get(k);
				}
				prepareToAddValueForQoS(VM_ID, service, entry.getKey(), value);
			}

			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private  void prepareToAddValueForHardwareCP(String VM_ID, String name, double... values){
		Repository.prepareToUpdateHardwareControlPrimitive(VM_ID, name, values);
	}
	
	private  void prepareToAddValueForQoS(String VM_ID, String service, String name, double... values) {
		Repository.prepareToAddValueForQoS(VM_ID+"-"+service, name, values);
	}
	
	private  void prepareToAddValueForPrimitive(String VM_ID, String service, String name, double... values){
		Repository.prepareToAddValueForPrimitive(VM_ID+"-"+service, name, values);
	}

}
